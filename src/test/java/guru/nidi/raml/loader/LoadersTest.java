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
package guru.nidi.raml.loader;

import guru.nidi.raml.loader.std.ClassPathRamlLoader;
import guru.nidi.raml.loader.std.CompositeRamlLoader;
import guru.nidi.raml.loader.std.FileRamlLoader;
import guru.nidi.raml.loader.model.RamlLoader;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static guru.nidi.raml.loader.util.TestUtils.getEnv;

/**
 *
 */
public class LoadersTest {
    @Test
    @Ignore
    public void apiPortalReferenced() throws IOException {
        final RamlLoaders ramlLoader = RamlLoaders.fromApiPortal(getEnv("API_PORTAL_USER"), getEnv("API_PORTAL_PASS"));
//        final RamlDefinition ramlDefinition = ramlLoader.load("test.raml");
//        assertNoViolations(ramlDefinition, get("/test"), jsonResponse(200, "\"hula\""));
    }

    @Test(expected = RamlLoader.ResourceNotFoundException.class)
    public void loadFileWithUnfindableReference() {
        RamlLoaders.fromFile(new File("src/test/resources/guru/nidi/ramltester/sub")).load("simple.raml");
    }

    @Test
    public void loadFileWithSecondLoader() {
        RamlLoaders.using(
                new CompositeRamlLoader(
                        new FileRamlLoader(new File("src/test/resources/guru/nidi/raml/loader/sub")),
                        new ClassPathRamlLoader("guru/nidi/raml/loader")))
                .load("simple.raml");
    }
}
