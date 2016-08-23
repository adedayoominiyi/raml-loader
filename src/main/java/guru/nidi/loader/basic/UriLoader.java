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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles resources with absolute URIs. Handling of relative URIs are delegated to another RamlLoader.
 * Loaders are registered in META-INF/services/guru.nidi.ramltester.loader.RamlLoaderFactory
 */
public class UriLoader implements Loader {
    private static final Pattern ABSOLUTE_URI_PATTERN = Pattern.compile("(([^:]+):?([^@]*)@)?([^:]+)://(.+)");
    private static final int
            GROUP_USER = 2,
            GROUP_PASSWORD = 3,
            GROUP_PROTOCOL = 4,
            GROUP_PATH = 5;

    private static final Map<String, LoaderFactory> FACTORIES = new HashMap<>();

    static {
        final ServiceLoader<LoaderFactory> loader = ServiceLoader.load(LoaderFactory.class);
        for (final LoaderFactory factory : loader) {
            FACTORIES.put(factory.supportedProtocol(), factory);
        }
    }

    private final Loader relativeLoader;

    public UriLoader() {
        this(null);
    }

    public UriLoader(Loader relativeLoader) {
        this.relativeLoader = relativeLoader;
    }

    @Override
    public InputStream fetchResource(String name, long ifModifiedSince) {
        final String normalized = normalizeResourceName(name);
        final Matcher matcher = ABSOLUTE_URI_PATTERN.matcher(normalized);
        if (matcher.matches()) {
            String path = matcher.group(GROUP_PATH);
            String res = "";
            final int lastSlash = path.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < path.length() - 1) {
                res = path.substring(lastSlash + 1);
                path = path.substring(0, lastSlash);
            }
            return absoluteLoader(matcher.group(GROUP_PROTOCOL), path, matcher.group(GROUP_USER), matcher.group(GROUP_PASSWORD))
                    .fetchResource(res, ifModifiedSince);
        }
        if (relativeLoader == null) {
            throw new IllegalArgumentException("Expected absolute uri '[username:password@]protocol://base[/file]', but got '" + name + "'");
        }
        return relativeLoader.fetchResource(normalized, ifModifiedSince);
    }

    @Override
    public String config() {
        return "uri-" + (relativeLoader == null ? "" : relativeLoader.config());
    }

    //raml parser does its own absolute/relative handling (org.raml.parser.tagresolver.ContextPath#resolveAbsolutePath)
    // -> hack to undo this
    private String normalizeResourceName(String name) {
        if (name.startsWith("//")) {
            return "classpath:" + name;
        }
        final int firstProtocol = name.indexOf("://");
        final int secondProtocol = name.indexOf("://", firstProtocol + 1);
        final int protocol = secondProtocol < 0 ? firstProtocol : secondProtocol;
        final int endOfFirst = name.lastIndexOf("/", protocol);
        if (endOfFirst >= 0) {
            return name.substring(endOfFirst + 1);
        }
        return name;
    }

    private Loader absoluteLoader(String protocol, String base, String username, String password) {
        final LoaderFactory factory = FACTORIES.get(protocol);
        if (factory == null) {
            throw new IllegalArgumentException("Unsupported protocol " + protocol);
        }
        return factory.getLoader(base, username, password);
    }
}
