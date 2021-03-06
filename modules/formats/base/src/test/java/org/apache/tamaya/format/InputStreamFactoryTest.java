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
package org.apache.tamaya.format;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SuppressWarnings("resource")
public class InputStreamFactoryTest {

	@Test(expected = NullPointerException.class)
    public void ctorEnforcesNonNullOriginal() throws IOException {
        new InputStreamFactory(null);
    }

    @Test
    public void givenStreamIsClosedInTryWithResourcesConstruct() throws Exception {
        InputStream stream = mock(InputStream.class);
        doReturn(34).when(stream).read();

        InputStreamFactory factory = new InputStreamFactory(stream);
        verify(stream).close();
        for (int i = 0; i < 100; i++) {
            try (InputStream in = factory.createInputStream()) {
                in.read();
            }
        }
        verify(stream).close();
    }

    @Test
    public void callToReadIsNotForwardedCallToWrapped() throws IOException {
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        InputStreamFactory closer = new InputStreamFactory(stream);
        byte[] byteArray = new byte[4];
        for (int i = 0; i < 100; i++) {
            InputStream is = closer.createInputStream();
            assertThat(is.read(byteArray)).isEqualTo(4);
        }
    }


    @Test
    public void callToSkipIsForwardedToWrapped() throws IOException {
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        InputStreamFactory closer = new InputStreamFactory(stream);
        for (int i = 0; i < 100; i++) {
            InputStream is = closer.createInputStream();
            assertThat(is.skip(2L)).isEqualTo(2L);
        }
    }


    @Test
    public void callToAvailableIsNotForwardedToWrapped() throws IOException {
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        InputStreamFactory closer = new InputStreamFactory(stream);
        for (int i = 0; i < 100; i++) {
            InputStream is = closer.createInputStream();
            assertThat(is.available()).isEqualTo(4);
        }
    }

    @Test
    public void callToCloseIsNotForwardedToWrapped() throws IOException {
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        InputStreamFactory closer = new InputStreamFactory(stream);
        for (int i = 0; i < 100; i++) {
            InputStream is = closer.createInputStream();
            is.close();
        }
    }

    @Test
    public void callToMarkIsNotForwardedToWrapped() throws IOException {
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        InputStreamFactory closer = new InputStreamFactory(stream);
        for (int i = 0; i < 100; i++) {
            InputStream is = closer.createInputStream();
            is.mark(2);
        }
    }


    @Test
    public void callToResetIsNotForwardedToWrapped() throws IOException {
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        InputStreamFactory closer = new InputStreamFactory(stream);
        for (int i = 0; i < 100; i++) {
            InputStream is = closer.createInputStream();
            is.reset();
        }
    }

    @Test
    public void callToMarkSupportedIsNotForwardedToWrapped() throws IOException {
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        InputStreamFactory closer = new InputStreamFactory(stream);
        for (int i = 0; i < 100; i++) {
            InputStream is = closer.createInputStream();
            assertThat(is.markSupported()).isTrue();
        }
    }

    @Test
    public void callToReadIsForwardedToWrapped() throws IOException {
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        InputStreamFactory closer = new InputStreamFactory(stream);
        for (int i = 0; i < 100; i++) {
            InputStream is = closer.createInputStream();
            assertThat(is.read()).isEqualTo(1);
            assertThat(is.read()).isEqualTo(2);
            assertThat(is.read()).isEqualTo(3);
            assertThat(is.read()).isEqualTo(4);
        }
    }

}
