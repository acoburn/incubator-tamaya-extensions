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
package org.apache.tamaya.format.formats;

import org.apache.tamaya.format.ConfigurationData;
import org.apache.tamaya.format.ConfigurationFormat;
import org.apache.tamaya.spi.PropertyValue;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Implementation of a {@link org.apache.tamaya.format.ConfigurationFormat} for xml property
 * files.
 *
 * @see java.util.Properties#loadFromXML(java.io.InputStream)
 */
@Component
public class PropertiesXmlFormat implements ConfigurationFormat {

    @Override
    public String getName() {
        return "xml-properties";
    }

    @Override
    public boolean accepts(URL url) {
        String fileName = url.getFile();
        return fileName.endsWith(".xml") || fileName.endsWith(".XML");
    }

    @SuppressWarnings("unchecked")
    @Override
    public ConfigurationData readConfiguration(String resource, InputStream inputStream)
    throws IOException{
        final Properties p = new Properties();
        p.loadFromXML(inputStream);
        Set<PropertyValue> data = new HashSet<>();
        for(Map.Entry en:p.entrySet()) {
            PropertyValue pv = PropertyValue.createValue(en.getKey().toString(), en.getValue().toString())
                    .setMeta("source", resource)
                    .setMeta(ConfigurationFormat.class.getName(), this);
            data.add(pv);
        }
        return new ConfigurationData(resource, this, data);
    }
}
