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

import guru.nidi.loader.Loader;
import guru.nidi.loader.url.GithubLoader;
import guru.nidi.loader.url.UrlLoader;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.Date;

import static guru.nidi.loader.util.TestUtils.getEnv;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 *
 */
public class LoaderTest {
    @Test
    public void includeHandlerNotClosingStream() throws IOException {
        final InputStream in = new UrlLoader("http://deadleg.github.io/bugs").fetchResource("test.raml", -1);
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void classPathOk() throws IOException {
        final InputStream in = new ClassPathLoader("guru/nidi/loader").fetchResource("simple.raml", -1);
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void classPathWithEndSlash() throws IOException {
        final InputStream in = new ClassPathLoader("guru/nidi/loader/").fetchResource("simple.raml", -1);
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void emptyBaseClassPath() throws IOException {
        final InputStream in = new ClassPathLoader().fetchResource("guru/nidi/loader/simple.raml", -1);
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void jarInClassPath() {
        assertNotNull(new ClassPathLoader("org/junit").fetchResource("Test.class", -1));
    }

    @Test
    public void jarInClassPathNotModified() {
        assertNull(new ClassPathLoader("org/junit").fetchResource("Test.class", new Date(130, 0, 0).getTime() - 1));
    }

    @Test
    public void fileInClassPathNotModified() throws IOException {
        final long mod = new File("target/test-classes/guru/nidi/loader/simple.raml").lastModified();
        assertNull(new ClassPathLoader("guru/nidi/loader").fetchResource("simple.raml", mod + 1));
    }

    @Test(expected = Loader.ResourceNotFoundException.class)
    public void classPathNok() {
        new ClassPathLoader("guru/nidi/raml/loader").fetchResource("bla", -1);
    }

    @Test
    public void fileOk() throws IOException {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource("guru/nidi/loader");
        assertEquals("file", resource.getProtocol());
        final InputStream in = new FileLoader(new File(resource.getPath())).fetchResource("simple.raml", -1);
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void fileNotModified() throws IOException {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource("guru/nidi/loader");
        assertEquals("file", resource.getProtocol());
        final long mod = new File(resource.getPath()).lastModified();
        assertNull(new FileLoader(new File(resource.getPath())).fetchResource("simple.raml", mod + 1));
    }

    @Test(expected = Loader.ResourceNotFoundException.class)
    public void fileNok() {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource("guru/nidi/loader");
        assertEquals("file", resource.getProtocol());
        new FileLoader(new File(resource.getPath())).fetchResource("bla", -1);
    }

    @Test
    public void urlOk() throws IOException {
        final InputStream in = new UrlLoader("http://en.wikipedia.org/wiki").fetchResource("Short", -1);
        assertStreamStart(in, "<!DOCTYPE html>");
    }


    @Test(expected = Loader.ResourceNotFoundException.class)
    public void urlNok() {
        new UrlLoader("http://en.wikipedia.org").fetchResource("dfkjsdfhfs", -1);
    }

    @Test
    public void loadFile() throws IOException {
        final InputStream in = new FileLoader(new File("src/test/resources/guru/nidi/loader")).fetchResource("simple.raml", -1);
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test(expected = Loader.ResourceNotFoundException.class)
    public void loadFileWithUnfindableReference() {
        new FileLoader(new File("src/test/resources/guru/nidi/ramltester/sub")).fetchResource("simple.raml", -1);
    }


    @Test
    public void publicGithub() throws IOException {
        final InputStream in = new GithubLoader("nidi3/raml-loader").fetchResource("src/test/resources/guru/nidi/loader/simple.raml", -1);
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void publicGithubNotModified() throws IOException {
        assertNull(new GithubLoader("nidi3/raml-tester").fetchResource("src/test/resources/guru/nidi/ramltester/simple.raml", new Date(130, 0, 1).getTime()));
    }

    @Test
    public void publicGithubModified() throws IOException {
        final InputStream in = new GithubLoader("nidi3/raml-tester").fetchResource("src/test/resources/guru/nidi/ramltester/simple.raml", new Date(100, 0, 1).getTime());
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void privateGithub() throws IOException {
        final InputStream in = new GithubLoader(getEnv("GITHUB_TOKEN"), "nidi3/blog").fetchResource("README.md", -1);
        assertStreamStart(in, "blog");
    }

    @Test
    public void loadFileWithSecondLoader() throws IOException {
        final InputStream in = new CompositeLoader(
                new FileLoader(new File("src/test/resources/guru/nidi/loader/sub")),
                new ClassPathLoader("guru/nidi/loader"))
                .fetchResource("simple.raml", -1);
        assertStreamStart(in, "#%RAML 0.8");
    }

    private void assertStreamStart(InputStream in, String s) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            assertThat(reader.readLine(), equalTo(s));
        }
    }
}

