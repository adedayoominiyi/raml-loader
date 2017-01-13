/*
 * Copyright (C) 2015 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.loader.apidesigner;

import guru.nidi.loader.ResourceNotFoundException;
import guru.nidi.loader.use.raml.RamlLoad;
import guru.nidi.loader.util.TestUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class ApiLoaderTest {
    private ApiLoader loader;

    @Before
    public void setUp() throws Exception {
        loader = new ApiLoader(TestUtils.getEnv("API_PORTAL_USER"), TestUtils.getEnv("API_PORTAL_PASS"));
    }

    @Test
    @Ignore
    public void fromApiPortalOk() throws IOException {
        assertNotNull(new RamlLoad(loader).load("test.raml"));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void fromApiPortalUnknownFile() throws IOException {
        new RamlLoad(loader).load("huhuhuhuhu.raml");
    }

    @Test(expected = ResourceNotFoundException.class)
    public void fromApiPortalUnknownUser() throws IOException {
        new RamlLoad(new ApiLoader("wwwwww", "blalbulbi")).load("test.raml");
    }
}
