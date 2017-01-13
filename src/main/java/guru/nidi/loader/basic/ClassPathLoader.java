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
import guru.nidi.loader.LoaderFactory;
import guru.nidi.loader.ResourceNotFoundException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ClassPathLoader implements Loader {
    private static final String FILE_COLON = "file:";
    private final String base;

    public ClassPathLoader() {
        this("");
    }

    public ClassPathLoader(String base) {
        if (base == null || base.length() == 0) {
            this.base = "";
        } else {
            this.base = base.endsWith("/") ? base : base + "/";
        }
    }

    @Override
    public InputStream fetchResource(String name, long ifModifiedSince) {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(normalize(name));
        if (url == null) {
            throw new ResourceNotFoundException(name);
        }
        try {
            final String path = url.getPath();
            switch (url.getProtocol()) {
                case "file":
                    final File file = new File(path);
                    return file.lastModified() > ifModifiedSince ? url.openStream() : null;
                case "jar":
                    if (path.startsWith(FILE_COLON)) {
                        final int pos = path.indexOf('!');
                        final File jar = new File(path.substring(FILE_COLON.length(), pos));
                        return jar.lastModified() > ifModifiedSince ? url.openStream() : null;
                    }
                    return url.openStream();
                default:
                    return url.openStream();
            }
        } catch (IOException e) {
            throw new ResourceNotFoundException(name, e);
        }
    }

    private String normalize(String path) {
        String res = base + (path.startsWith("/") ? path.substring(1) : path);
        res = res.replace("/./", "/");
        int pos;
        while ((pos = res.indexOf("../")) >= 0) {
            final int before = res.lastIndexOf('/', pos - 2);
            if (before >= 0) {
                res = res.substring(0, before) + res.substring(pos + 2);
            } else {
                if (pos <= 1) {
                    throw new IllegalArgumentException("Invalid path '" + path + "'");
                }
                res = res.substring(pos + 3);
            }
        }
        return res;
    }

    @Override
    public String config() {
        return "classpath-" + base;
    }

    public static class Factory implements LoaderFactory {
        @Override
        public String supportedProtocol() {
            return "classpath";
        }

        @Override
        public Loader getLoader(String base, String username, String password) {
            return new ClassPathLoader(base);
        }
    }

}
