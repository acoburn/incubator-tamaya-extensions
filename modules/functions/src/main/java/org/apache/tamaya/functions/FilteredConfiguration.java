/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tamaya.functions;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationSnapshot;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spisupport.DefaultConfigurationSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration that filters part of the entries defined by a matcher predicate.
 */
class FilteredConfiguration implements Configuration {

    private final Configuration baseConfiguration;
    private final PropertyMatcher matcher;
    private final String filterType;

    FilteredConfiguration(Configuration baseConfiguration, PropertyMatcher matcher, String filterType) {
        this.baseConfiguration = Objects.requireNonNull(baseConfiguration);
        this.matcher = Objects.requireNonNull(matcher);
        this.filterType = filterType!=null?filterType:this.matcher.toString();
    }

    @Override
    public String get(String key) {
        return get(key, String.class);
    }

    @Override
    public String getOrDefault(String key, String defaultValue) {
        String val = get(key);
        if(val==null){
            return defaultValue;
        }
        return val;
    }

    @Override
    public <T> T getOrDefault(String key, Class<T> type, T defaultValue) {
        T val = get(key, type);
        if(val==null){
            return defaultValue;
        }
        return val;
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        return (T)get(key, TypeLiteral.of(type));
    }

    @Override
    public <T> T get(String key, TypeLiteral<T> type) {
        String value = baseConfiguration.get(key);
        if (matcher.test(key, value)) {
            return baseConfiguration.get(key, type);
        }
        return null;
    }

    @Override
    public <T> T getOrDefault(String key, TypeLiteral<T> type, T defaultValue) {
        T val = get(key, type);
        if(val==null){
            return defaultValue;
        }
        return val;
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> result = new HashMap<>();
        for(Map.Entry<String,String> en:baseConfiguration.getProperties().entrySet()){
            if(matcher.test(en.getKey(), en.getValue())){
                result.put(en.getKey(), en.getValue());
            }
        }
        return result;
    }

    @Override
    public ConfigurationContext getContext() {
        return baseConfiguration.getContext();
    }

    @Override
    public ConfigurationSnapshot getSnapshot(Iterable<String> keys) {
        return new DefaultConfigurationSnapshot(this, keys);
    }

    @Override
    public String toString() {
        return "FilteredConfiguration{" +
                "baseConfiguration=" + baseConfiguration +
                ", matcher=" + filterType +
                '}';
    }

}
