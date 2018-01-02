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
package annottext;

import org.apache.tamaya.inject.api.ConfigFallbackKeys;
import org.apache.tamaya.inject.api.DynamicValue;
import org.apache.tamaya.inject.api.NoConfig;

import javax.config.inject.ConfigProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * An example showing some basic annotations, using an interface to be proxied by the
 * configuration system, nevertheless extending the overall Configuration interface.
 * Created by Anatole on 15.02.14.
 */
public class AnnotatedConfigBean {

    @ConfigProperty(name="foo.bar.myprop", defaultValue = "ET")
    @ConfigFallbackKeys({"mp", "common.testdata.myProperty"})
    public String myParameter;

    @ConfigProperty(name="simple_value")
    public String simpleValue;

    @ConfigProperty
    String anotherValue;

    @ConfigProperty(name="host.name")
    private String hostName;

    @ConfigProperty(name="host.name")
    private DynamicValue<String> dynamicHostname;

    @NoConfig
    public String javaVersion;

    public String getAnotherValue(){
        return anotherValue;
    }

    public String getHostName(){
        return hostName;
    }

    public DynamicValue<String> getDynamicValue(){
        return dynamicHostname;
    }

    @NoConfig
    private List<String> events = new ArrayList<>();

    // verify we don't try to inject final fields
    public static final String CONSTANT = "a constant";


    @ConfigProperty(name="java.version")
    void setJavaVersion(String version){
        this.javaVersion = version;
    }

}
