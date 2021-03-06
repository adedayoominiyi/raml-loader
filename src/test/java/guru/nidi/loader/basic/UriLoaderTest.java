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

import guru.nidi.loader.util.ServerTest;
import guru.nidi.loader.util.TestUtils;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class UriLoaderTest extends ServerTest {
    @Test
    public void uriRegex() {
        final Pattern ABSOLUTE_URI_PATTERN = Pattern.compile("([^:]+)://(.+)/([^/]+)");
        final Matcher matcher = ABSOLUTE_URI_PATTERN.matcher("http://a/b/c");
        assertTrue(matcher.matches());
        assertEquals("http", matcher.group(1));
        assertEquals("a/b", matcher.group(2));
        assertEquals("c", matcher.group(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void absoluteWithoutProtocol() {
        new UriLoader().fetchResource("relative.raml", -1);
    }

    @Test
    public void file() {
        assertNotNull(new UriLoader().fetchResource("file://" + getClass().getResource("sub.raml").getFile(), -1));
    }

    @Test
    public void classpathRoot() {
        assertNotNull(new UriLoader().fetchResource("classpath://simple.raml", -1));
    }

    @Test
    public void classpath() {
        assertNotNull(new UriLoader().fetchResource("classpath://guru/nidi/loader/simple.raml", -1));
    }

    @Test
    public void url() {
        assertNotNull(new UriLoader().fetchResource("http://localhost:" + port() + "/deliver/sub.raml", -1));
    }

    @Test
    @Ignore
    public void apiPortal() {
        assertNotNull(new UriLoader().fetchResource(TestUtils.getEnv("API_PORTAL_USER") + ":" + TestUtils.getEnv("API_PORTAL_PASS") + "@apiportal://test.raml", -1));
    }

    @Test
    @Ignore
    public void apiDesigner() {
        assertNotNull(new UriLoader().fetchResource("apidesigner://todo", -1));
    }

    @Test
    public void ramlWithAbsoluteIncludes() {
        assertNotNull(new UriLoader().fetchResource("http://localhost:" + port() + "/deliver/sub.raml", -1));
    }

    private static class FileDeliveringServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            final URL in = getClass().getResource(req.getPathInfo().substring(1));
            if (in == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                Files.copy(new File(in.getFile()).toPath(), resp.getOutputStream());
            }
        }
    }

    @Override
    protected int port() {
        return 8085;
    }

    @Override
    protected void init(Context ctx) {
        Tomcat.addServlet(ctx, "app", new FileDeliveringServlet());
        ctx.addServletMapping("/deliver/*", "app");
    }
}
