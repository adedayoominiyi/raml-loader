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
package guru.nidi.loader.basic;

import guru.nidi.loader.ResourceNotFoundException;
import guru.nidi.loader.url.UrlLoader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import static guru.nidi.loader.util.TestUtils.assertRamlStart;
import static guru.nidi.loader.util.TestUtils.assertStreamStart;
import static org.junit.Assert.*;

public class LoaderTest {
    @Test
    public void includeHandlerNotClosingStream() throws IOException {
        final InputStream in = new UrlLoader("http://deadleg.github.io/bugs").fetchResource("test.raml", -1);
        assertRamlStart(in);
    }

    @Test
    public void classPathOk() throws IOException {
        final InputStream in = new ClassPathLoader("guru/nidi/loader").fetchResource("simple.raml", -1);
        assertRamlStart(in);
    }

    @Test
    public void classPathWithEndSlash() throws IOException {
        final InputStream in = new ClassPathLoader("guru/nidi/loader/").fetchResource("simple.raml", -1);
        assertRamlStart(in);
    }

    @Test
    public void emptyBaseClassPath() throws IOException {
        final InputStream in = new ClassPathLoader().fetchResource("guru/nidi/loader/simple.raml", -1);
        assertRamlStart(in);
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

    @Test(expected = ResourceNotFoundException.class)
    public void classPathNok() {
        new ClassPathLoader("guru/nidi/loader").fetchResource("bla", -1);
    }

    @Test
    public void classPathWithDotDot() {
        new ClassPathLoader("guru/nidi/loader/sub").fetchResource("/../simple.raml", -1);
    }

    @Test
    public void classPathWithSecondDotDot1() {
        new ClassPathLoader("guru/../guru/nidi/loader").fetchResource("simple.raml", -1);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void classPathWithSecondDotDot2() {
        new ClassPathLoader("/guru/../guru/nidi/loader").fetchResource("simple.raml", -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void classPathWithStartingDotDot1() {
        new ClassPathLoader("/../nidi/loader").fetchResource("simple.raml", -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void classPathWithStartingDotDot2() {
        new ClassPathLoader("../nidi/loader").fetchResource("simple.raml", -1);
    }

    @Test
    public void classPathWithDot() {
        new ClassPathLoader("guru/nidi/loader").fetchResource("./simple.raml", -1);
    }

    @Test
    public void fileOk() throws IOException {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource("guru/nidi/loader");
        assertEquals("file", resource.getProtocol());
        final InputStream in = new FileLoader(new File(resource.getPath())).fetchResource("simple.raml", -1);
        assertRamlStart(in);
    }

    @Test
    public void fileNotModified() throws IOException {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource("guru/nidi/loader");
        assertEquals("file", resource.getProtocol());
        final long mod = new File(resource.getPath(), "simple.raml").lastModified();
        assertNull(new FileLoader(new File(resource.getPath())).fetchResource("simple.raml", mod + 1));
    }

    @Test(expected = ResourceNotFoundException.class)
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

    @Test(expected = ResourceNotFoundException.class)
    public void urlNok() {
        new UrlLoader("http://en.wikipedia.org").fetchResource("dfkjsdfhfs", -1);
    }

    @Test
    public void loadFile() throws IOException {
        final InputStream in = new FileLoader(new File("src/test/resources/guru/nidi/loader")).fetchResource("simple.raml", -1);
        assertRamlStart(in);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void loadFileWithUnfindableReference() {
        new FileLoader(new File("src/test/resources/guru/nidi/ramltester/sub")).fetchResource("simple.raml", -1);
    }

    @Test
    public void loadFileWithSecondLoader() throws IOException {
        final InputStream in = new CompositeLoader(
                new FileLoader(new File("src/test/resources/guru/nidi/loader/sub")),
                new ClassPathLoader("guru/nidi/loader"))
                .fetchResource("simple.raml", -1);
        assertRamlStart(in);
    }

    @Test
    public void cachingLoaderInterceptor() throws IOException {
        class TestLoaderInterceptor extends CachingLoaderInterceptor {
            private byte[] data;

            @Override
            protected void processLoaded(String name, byte[] data) {
                this.data = data;
            }
        }
        final TestLoaderInterceptor tli = new TestLoaderInterceptor();
        final InputStream in = new InterceptingLoader(new ClassPathLoader("guru/nidi/loader"), tli)
                .fetchResource("simple.raml", -1);
        assertRamlStart(in);
        assertRamlStart(new ByteArrayInputStream(tli.data));
    }


}

