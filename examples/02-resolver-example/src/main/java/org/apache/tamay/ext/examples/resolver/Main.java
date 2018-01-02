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
package org.apache.tamay.ext.examples.resolver;

import javax.config.Config;
import javax.config.ConfigProvider;
import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * Example illustrating the usage of value resolver expressions (placeholders).
 */
public class Main {
    /*
     * Turns off all logging.
     */
    static {
        LogManager.getLogManager().reset();
        Logger globalLogger = Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
        globalLogger.setLevel(java.util.logging.Level.OFF);
    }


    private Main() {
    }

    public static void main(String[] args){
        Config cfg = ConfigProvider.getConfig();

        System.out.println("****************************************************");
        System.out.println("Resolver Example");
        System.out.println("****************************************************");
        System.out.println();
        System.out.println("Example Metadata:");
        System.out.println("\tType        :  " + cfg.getValue("example.type", String.class));
        System.out.println("\tName        :  " + cfg.getValue("example.name", String.class));
        System.out.println("\tDescription :  " + cfg.getValue("example.description", String.class));
        System.out.println("\tVersion     :  " + cfg.getValue("example.version", String.class));
        System.out.println("\tAuthor      :  " + cfg.getValue("example.author", String.class));
        System.out.println();
        System.out.println("Resolved Data:");
        System.out.println("\tFullName     :  " + cfg.getValue("example.fullName", String.class));
        System.out.println("\tFullVersion  :  " + cfg.getValue("example.fullVersion", String.class));
        System.out.println();
        
        dump(cfg.getPropertyNames(), System.out, cfg);
    }

    private static void dump(Iterable<String> properties, PrintStream stream, Config config) {
        stream.println("FULL DUMP:\n\n");

        for (String en : properties) {
            stream.println(format("\t%s = %s", en, config.getValue(en, String.class)));
        }
    }
}
