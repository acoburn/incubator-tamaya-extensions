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
package org.apache.tamaya.osgi.updater;

import org.apache.tamaya.events.ConfigEvent;
import org.apache.tamaya.events.ConfigChangeBuilder;
import org.apache.tamaya.osgi.commands.TamayaConfigService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Created by atsti on 30.09.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class EventListenerTest extends AbstractOSGITest{

    private EventListener eventListener;

    @Before
    public void setupEL() throws Exception {
        eventListener = new EventListener(bundleContext);
    }

    @Test
    public void testEventWithNoDataDoesNotTriggerTamayaServices() throws Exception {
        ConfigEvent evt = ConfigChangeBuilder.of().addChange("a", "b").build();
        eventListener.onConfigEvent(evt);
        verify(bundleContext, never()).getServiceReference(TamayaConfigService.class);
    }

    @Test
    public void testEventForPIDDoesTriggerTamayaServices() throws Exception {
        ConfigEvent evt = ConfigChangeBuilder.of().addChange("[PID.foo]a", "b").build();
        eventListener.onConfigEvent(evt);
        verify(bundleContext).getServiceReference(TamayaConfigService.class);
        verify(tamayaConfigService).updateConfig("PID.foo");
    }

}