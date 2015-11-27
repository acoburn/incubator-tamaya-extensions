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
package org.apache.tamaya.integration.osgi.base;

import org.apache.log4j.Priority;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.functions.ConfigurationFunctions;
import org.apache.tamaya.spi.ServiceContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class TestConfigIntegration{

    private static final String TAMAYA_VERSION = "0.2-incubating-SNAPSHOT";

    static{
        System.setProperty("[bundle:tamaya]systemTestKey", "foo");
    }


    @ArquillianResource BundleContext context;

    //////////////////////////////////////////////////////// Test setup //////////////////////////////////

    @Deployment
    public static JavaArchive createdeployment() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "tamaya-osgi-general.jar");
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages("org.junit", "org.apache.tamaya", "org.apache.tamaya.spi",
                        "org.apache.tamaya.core", "org.osgi.service.cm");
                builder.addImportPackages(ServiceContext.class, ConfigurationProvider.class, ConfigurationAdmin.class,
                        ServiceTracker.class, ConfigurationFunctions.class);
                builder.addBundleActivator(Activator.class);
                return builder.openStream();
            }
        });
        archive.addClasses(Test.class, TestConfigIntegration.class, Priority.class, Activator.class,
                            TamayaConfigAdminImpl.class, TamayaConfigurationImpl.class, OSGIConfigRootMapper.class);
        URL config = ClassLoader.getSystemClassLoader().getResource("META-INF/javaconfiguration.properties");
        archive.addAsResource(config, "META-INF/javaconfiguration.properties");
        return archive;
    }

    @Deployment(name="felix.main",order=10)
    public static JavaArchive deployMain() {
        return ShrinkWrap.create(ZipImporter.class, "felix.main-5.4.0.jar")
                .importFrom(new File("../test-bundles/org.apache.felix.main-5.4.0.jar"))
                .as(JavaArchive.class);
    }

    @Deployment(name="osgi.config",order=10)
    public static JavaArchive deployOSGIConfig() {
        return ShrinkWrap.create(ZipImporter.class, "felix.configadmin-1.8.8.jar")
                .importFrom(new File("../test-bundles/org.apache.felix.configadmin-1.8.8.jar"))
                .as(JavaArchive.class);
    }

    @Deployment(name="tamaya-api",order=1)
    public static JavaArchive deployTamayaAPI() {
        return ShrinkWrap.create(ZipImporter.class, "tamaya-api.jar")
                .importFrom(getBundleFile("org.apache.tamaya:tamaya-api:"+TAMAYA_VERSION))
                .as(JavaArchive.class);
    }

    @Deployment(name="tamaya-core",order=2)
    public static JavaArchive deployTamayaCore() {
        return ShrinkWrap.create(ZipImporter.class, "tamaya-core.jar")
                .importFrom(getBundleFile("org.apache.tamaya:tamaya-core:"+TAMAYA_VERSION))
                .as(JavaArchive.class);
    }

    @Deployment(name="tamaya-functions",order=2)
    public static JavaArchive deployTamayaFunctions() {
        return ShrinkWrap.create(ZipImporter.class, "tamaya-functions.jar")
                .importFrom(getBundleFile("org.apache.tamaya.ext:tamaya-functions:"+TAMAYA_VERSION))
                .as(JavaArchive.class);
    }

    @Deployment(name="javax.annotation",order=0)
    public static JavaArchive deployJavaxAnnotation() {
        return ShrinkWrap.create(ZipImporter.class, "javax.annotation.jar")
                .importFrom(Maven.configureResolver()
                        .withMavenCentralRepo(false)
                        .withClassPathResolution(true)
                        .resolve("org.apache.geronimo.specs:geronimo-annotation_1.2_spec:1.0-alpha-1")
                        .withoutTransitivity().asSingleFile())
                .as(JavaArchive.class);
    }

    private static File getBundleFile(String artifactId) {
        // Check
        return Maven.configureResolver()
                .withMavenCentralRepo(false)
                .withClassPathResolution(true)
                .resolve(artifactId).withoutTransitivity().asSingleFile();
    }


    //////////////////////////////////////////////////////// Tests //////////////////////////////////


    @Before
    public void startBundles(){
        for(Bundle bundle:context.getBundles()) {
            try {
                bundle.start();
            } catch (BundleException e) {
                e.printStackTrace();
            }
        }
    }

    @OperateOnDeployment("tamaya-core")
    @Test
    public void testTamayaAvailable() throws Exception {
        assertNotNull(ConfigurationProvider.getConfiguration());
    }

    @Test
    public void testTamayaConfigAdminAvailable() throws Exception {
        ServiceReference<ConfigurationAdmin> ref = context.getServiceReference(ConfigurationAdmin.class);
        assertNotNull("OSGI ConfigAdmin not loaded.", ref);
        ConfigurationAdmin osgiConfig = context.getService(ref);
        assertNotNull("No config available from Tamaya through OSGI ConfigAdmin.", osgiConfig);
        assertEquals("Override of OSGI ConfigAdmin with Tamaya did not work.", osgiConfig.getClass().getName(), TamayaConfigAdminImpl.class.getName());
    }

    @Test
    public void testLoadTamayaConfigFromConfigAdmin() throws Exception {
        ServiceReference<ConfigurationAdmin> ref = context.getServiceReference(ConfigurationAdmin.class);
        assertNotNull("OSGI ConfigAdmin not loaded.", ref);
        ConfigurationAdmin admin = context.getService(ref);
        Configuration osgiConfig = admin.getConfiguration("tamaya");
        assertNotNull("No config available from Tamaya through OSGI ConfigAdmin.", osgiConfig);
        Dictionary<String,Object> config = osgiConfig.getProperties();
        assertNotNull("No config entries loaded from Tamaya.", config);
        assertEquals("Property 'testKey' not loaded from Tamaya.", "foo", config.get("systemTestKey"));
        // TODO: Think on Resource loading in OSGI:
        // this should work with normal resource loading but does not work with OSGI, since this resource is not
        // visible by default, when we have added resource loading as abstraction to the ServiceContext, too.
        // Reason: Resource Loading in OSGI works differently!
//        assertEquals("Property 'testKey' not loaded from Tamaya.", "success!", config.get("my.testProperty"));
    }

}