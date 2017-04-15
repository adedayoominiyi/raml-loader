/*
 * Copyright © 2015 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.loader.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

public final class TestUtils {
    private static final String RAML_0_8 = "#%RAML 0.8";

    private TestUtils() {
    }

    public static String getEnv(String name) {
        final String env = System.getenv(name);
        assumeThat("Environment variable " + name + " is not set, skipping test", env, notNullValue());
        return env;
    }

    public static void assumeMyLocalMachine(){
        assumeTrue("/Users/nidi".equals(System.getenv("HOME")));
    }

    public static void assertStreamStart(InputStream in, String s) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"))) {
            assertThat(reader.readLine(), equalTo(s));
        }
    }

    public static void assertRamlStart(InputStream in) throws IOException {
        assertStreamStart(in, RAML_0_8);
    }
}
