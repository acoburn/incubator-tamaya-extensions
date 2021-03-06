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
package org.apache.tamaya.osgi.injection;

import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.propertysource.BasePropertySource;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a Tamaya PropertySource, which internally wraps the OSGI ConfigAdmin service, preconfigured
 * for a PID and (optionally) location.
 */
public class OSGIConfigAdminPropertySource extends BasePropertySource{

    private static final Logger LOG = Logger.getLogger(OSGIConfigAdminPropertySource.class.getName());
    private ConfigurationAdmin configurationAdmin;
    private String pid;
    private String location;

    public OSGIConfigAdminPropertySource(ConfigurationAdmin configurationAdmin, String pid){
        this.configurationAdmin = Objects.requireNonNull(configurationAdmin);
        this.pid = Objects.requireNonNull(pid);
    }

    public OSGIConfigAdminPropertySource(ConfigurationAdmin configurationAdmin, String pid, String location){
        this.configurationAdmin = Objects.requireNonNull(configurationAdmin);
        this.pid = Objects.requireNonNull(pid);
        this.location = location;
    }

    /**
     * Get the configured OSGI service PID.
     * @return the pid, nnever null.
     */
    public String getPid() {
        return pid;
    }

    /**
     * Get the configured OSGI config location, may be null.
     * @return the location, or null.
     */
    public String getLocation() {
        return location;
    }

    @Override
    public PropertyValue get(String key) {
        try {
            Configuration osgiConfig = configurationAdmin.getConfiguration(pid, location);
            Dictionary<String,Object> props = osgiConfig.getProperties();
            if(props!=null){
                Object value = props.get(key);
                if(value!=null) {
                    return PropertyValue.createValue(key, String.valueOf(value))
                            .setMeta("source", "OSGI ConfigAdmin: " + pid);
                }
            }
        } catch (IOException e) {
            LOG.log(Level.FINEST,  e, () -> "No config for PID: " + pid);
        }
        return null;
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        try {
            Configuration osgiConfig = configurationAdmin.getConfiguration(pid);
            Dictionary<String,Object> props = osgiConfig.getProperties();
            if(props!=null){
                Map<String, PropertyValue> result = new HashMap<>();
                Enumeration<String> keys = props.keys();
                while(keys.hasMoreElements()){
                    String key = keys.nextElement();
                    Object value = props.get(key);
                    result.put(key, PropertyValue.createValue(key, String.valueOf(value)).setMeta("source", "OSGI ConfigAdmin: " + pid));
                }
                return result;
            }
        } catch (IOException e) {
            LOG.log(Level.FINEST,  e, () -> "No config for PID: " + pid);
        }
        return Collections.emptyMap();
    }
}


