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
package guru.nidi.loader.basic;

import guru.nidi.loader.url.GithubLoader;
import guru.nidi.loader.util.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static guru.nidi.loader.basic.LoaderTest.assertStreamStart;
import static guru.nidi.loader.util.TestUtils.getEnv;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;

/**
 *
 */
public class GithubTest {

    @BeforeClass
    public static void init() {
        assumeTrue("/Users/nidi".equals(System.getenv("HOME")));
    }

    @Test
    public void publicGithub() throws IOException {
        final InputStream in = GithubLoader.forPublic("nidi3", "raml-loader").fetchResource("src/test/resources/guru/nidi/loader/simple.raml", -1);
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void publicGithubWithBasePath() throws IOException {
        final InputStream in = GithubLoader.forPublic("nidi3", "raml-loader", "src/test/resources").fetchResource("guru/nidi/loader/simple.raml", -1);
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void publicGithubNotModified() throws IOException {
        assertNull(GithubLoader.forPublic("nidi3", "raml-tester").fetchResource("src/test/resources/guru/nidi/ramltester/simple.raml", new Date(130, 0, 1).getTime()));
    }

    @Test
    public void publicGithubModified() throws IOException {
        final InputStream in = GithubLoader.forPublic("nidi3", "raml-tester").fetchResource("src/test/resources/guru/nidi/ramltester/simple.raml", new Date(100, 0, 1).getTime());
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void privateGithub() throws IOException {
        final InputStream in = GithubLoader.forPrivate(getEnv("GITHUB_TOKEN"), "nidi3", "blog").fetchResource("README.md", -1);
        assertStreamStart(in, "blog");
    }

    @Test
    public void publicGithubWithUri() {
        assertNotNull(new UriLoader().fetchResource("github://nidi3/raml-loader/src/test/resources/guru/nidi/loader/simple.raml", -1));
    }

    @Test
    public void privateGithubWithUri() throws IOException {
        assertNotNull(new UriLoader().fetchResource(TestUtils.getEnv("GITHUB_TOKEN") + "@github://nidi3/blog/simple.raml", -1));
    }

}