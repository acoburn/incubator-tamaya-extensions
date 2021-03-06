/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tamaya.cdi;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.inject.api.Config;
import org.apache.tamaya.inject.api.ConfigSection;
import org.apache.tamaya.inject.api.DynamicValue;
import org.apache.tamaya.inject.api.WithConfigOperator;
import org.apache.tamaya.inject.api.WithPropertyConverter;
import org.apache.tamaya.spi.ConfigurationBuilder;
import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spi.ConversionContext;
import org.apache.tamaya.spi.PropertyConverter;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Provider;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Producer bean for configuration properties.
 */
@ApplicationScoped
public class ConfigurationProducer {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationProducer.class.getName());

    private DynamicValue createDynamicValue(final InjectionPoint injectionPoint) {
        Member member = injectionPoint.getMember();
        if (member instanceof Field) {
            return DefaultDynamicValue.of(injectionPoint.getBean(), (Field) member, Configuration.current());
        } else if (member instanceof Method) {
            return DefaultDynamicValue.of(injectionPoint.getBean(), (Method) member, Configuration.current());
        }
        return null;
    }

    @Produces
    @Config
    public Object resolveAndConvert(final InjectionPoint injectionPoint) {
        if (DynamicValue.class.equals(injectionPoint.getAnnotated().getBaseType())) {
            return createDynamicValue(injectionPoint);
        }
        final Config annotation = injectionPoint.getAnnotated().getAnnotation(Config.class);
        final ConfigSection typeAnnot = injectionPoint.getMember().getDeclaringClass().getAnnotation(ConfigSection.class);
        final List<String> keys = TamayaCDIInjectionExtension.evaluateKeys(injectionPoint.getMember().getName(),
                annotation != null ? annotation.key() : null,
                annotation != null ? annotation.alternateKeys() : null,
                typeAnnot != null ? typeAnnot.value() : null);

        final WithConfigOperator withOperatorAnnot = injectionPoint.getAnnotated().getAnnotation(WithConfigOperator.class);
        UnaryOperator<Configuration> operator = null;
        if (withOperatorAnnot != null) {
            operator = TamayaCDIInjectionExtension.CUSTOM_OPERATORS.get(withOperatorAnnot.value());
        }
        PropertyConverter customConverter = null;
        final WithPropertyConverter withConverterAnnot = injectionPoint.getAnnotated().getAnnotation(WithPropertyConverter.class);
        if (withConverterAnnot != null) {
            customConverter = TamayaCDIInjectionExtension.CUSTOM_CONVERTERS.get(withConverterAnnot.value());
        }

        // unless the extension is not installed, this should never happen because the extension
        // enforces the resolvability of the config

        String defaultTextValue = null;
        if(annotation!=null && !annotation.defaultValue().equals(Config.UNCONFIGURED_VALUE)){
            defaultTextValue = annotation.defaultValue();
        }
        boolean required = annotation.required();
        String textValue = null;
        Configuration config = Configuration.current();
        if(operator!=null) {
            config = config.map(operator);
        }
        String keyFound = null;
        for(String key:keys) {
            textValue = config.getOrDefault(key, null);
            if(textValue!=null) {
                keyFound = key;
                break;
            }
        }
        if(textValue==null) {
            LOGGER.info("Using default value: '" + defaultTextValue + "' for IP: " + injectionPoint );
            textValue = defaultTextValue;
        }
        ConversionContext conversionContext = createConversionContext(keyFound, keys, injectionPoint);
        Object value = convertValue(textValue, conversionContext, injectionPoint, customConverter);
        if (value == null) {
            if (required){
                throw new ConfigException(String.format(
                    "Can't resolve any of the possible config keys: %s to the required target type: %s, supported formats: %s",
                    keys, conversionContext.getTargetType(), conversionContext.getSupportedFormats().toString()));
            }
        }
        LOGGER.finest(String.format("Injecting %s for key %s in class %s", keyFound, value, injectionPoint.toString()));
        return value;
    }

    static ConversionContext createConversionContext(String key, List<String> keys, InjectionPoint injectionPoint) {
        final Type targetType = injectionPoint.getAnnotated().getBaseType();
        Configuration config = Configuration.current();
        ConversionContext.Builder builder = new ConversionContext.Builder(config, key, TypeLiteral.of(targetType));
        // builder.setKeys(keys);
        if(targetType instanceof ParameterizedType){
            ParameterizedType pt = (ParameterizedType)targetType;
            if(pt.getRawType().equals(Provider.class)) {
                builder.setTargetType(
                        TypeLiteral.of(pt.getActualTypeArguments()[0]));
            }
        }
        if (injectionPoint.getMember() instanceof Field) {
            Field annotated = (Field)injectionPoint.getMember();
            if(annotated.isAnnotationPresent(Config.class)) {
                builder.setAnnotatedElement(annotated);
            }
        }else if(injectionPoint.getMember() instanceof Method){
            Method method = (Method)injectionPoint.getMember();
            for(Type type:method.getParameterTypes()){
                if(type instanceof AnnotatedElement){
                    AnnotatedElement annotated = (AnnotatedElement)type;
                    if(annotated.isAnnotationPresent(Config.class)) {
                        builder.setAnnotatedElement(annotated);
                    }
                }
            }
        }
        return builder.build();
    }

    static Object convertValue(String textValue, ConversionContext conversionContext, InjectionPoint injectionPoint,
                               PropertyConverter customConverter) {
        Object value = null;
        if (customConverter != null) {
            return customConverter.convert(textValue, conversionContext);
        }
        if (String.class.equals(conversionContext.getTargetType().getRawType())) {
            return textValue;
        }
        ParameterizedType pt = null;
        Type toType = injectionPoint.getAnnotated().getBaseType();
        if (toType instanceof ParameterizedType) {
            pt = (ParameterizedType) toType;
            if (Provider.class.equals(pt.getRawType()) || Instance.class.equals(pt.getRawType())
                    || Optional.class.equals(pt.getRawType())) {
                toType = pt.getActualTypeArguments()[0];
            }
            if (toType.equals(String.class)) {
                value = textValue;
            }
        }
        List<PropertyConverter<Object>> converters = Configuration.current().getContext()
                .getPropertyConverters(TypeLiteral.of(toType));
        for (PropertyConverter<Object> converter : converters) {
            try {
                value = converter.convert(textValue, conversionContext);
                if (value != null) {
                    LOGGER.log(Level.INFO, "Parsed value from '" + textValue + "' into " +
                            injectionPoint);
                    break;
                }
            } catch (Exception e) {
                LOGGER.log(Level.INFO, "Failed to convert value '" + textValue + "' for " +
                        injectionPoint, e);
            }
        }
        if (pt != null && Optional.class.equals(pt.getRawType())) {
            return Optional.ofNullable(value);
        }
        return value;
    }

    @Produces
    public Configuration getConfiguration(){
        return Configuration.current();
    }

    @Produces
    public ConfigurationContext getConfigurationContext(){
        return Configuration.current().getContext();
    }

    @Produces
    public ConfigurationBuilder getConfigurationBuilder(){
        return Configuration.createConfigurationBuilder();
    }

}
