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
package guru.nidi.loader.util;

import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.junit.AfterClass;
import org.junit.Before;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public abstract class ServerTest {
    private static Tomcat tomcat;
    private static Set<Class<?>> inited = new HashSet<>();
    private final static JarScanner NO_SCAN = new JarScanner() {
        @Override
        public void scan(ServletContext context, ClassLoader classloader, JarScannerCallback callback, Set<String> jarsToSkip) {
        }
    };

    @Before
    public void initImpl() throws LifecycleException, ServletException {
        if (!inited.contains(getClass())) {
            inited.add(getClass());
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            tomcat = new Tomcat();
            tomcat.setPort(port());
            tomcat.setBaseDir(".");
            Context ctx = tomcat.addWebapp("/", "src/test");
            ctx.setJarScanner(NO_SCAN);
            ((Host) ctx.getParent()).setAppBase("");

            init(ctx);

            tomcat.start();
            Server server = tomcat.getServer();
            server.start();
        }
    }

    protected abstract int port();

    protected void init(Context ctx) {
    }

    protected String url(String path) {
        return "http://localhost:" + port() + "/" + path;
    }

    @AfterClass
    public static void stopTomcat() throws LifecycleException {
        if (tomcat.getServer() != null && tomcat.getServer().getState() != LifecycleState.DESTROYED) {
            if (tomcat.getServer().getState() != LifecycleState.STOPPED) {
                tomcat.stop();
            }
            tomcat.destroy();
        }
    }
}
