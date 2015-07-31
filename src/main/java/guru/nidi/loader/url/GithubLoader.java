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
package guru.nidi.loader.url;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.nidi.loader.Loader;
import guru.nidi.loader.LoaderFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 *
 */
public class GithubLoader extends UrlLoader {
    private final String resourceBase;

    public GithubLoader(final String token, String project, String resourceBase, CloseableHttpClient httpClient) {
        super("https://api.github.com/repos/" + project + "/contents", new SimpleUrlFetcher() {
            @Override
            protected HttpGet postProcessGet(HttpGet get) {
                if (token != null) {
                    get.addHeader("Authorization", "token " + token);
                }
                return get;
            }
        }, httpClient);
        this.resourceBase = (resourceBase == null || resourceBase.length() == 0) ? "" : (resourceBase + "/");
    }

    public GithubLoader(String token, String project) {
        this(token, project, null, null);
    }

    public GithubLoader(String project) {
        this(null, project, null, null);
    }

    @Override
    public InputStream fetchResource(String name, long ifModifiedSince) {
        try {
            final InputStream raw = fetcher.fetchFromUrl(client, base, resourceBase + name, ifModifiedSince);
            if (raw == null) {
                return null;
            }
            @SuppressWarnings("unchecked")
            final Map<String, String> desc = new ObjectMapper().readValue(raw, Map.class);
            return fetcher.fetchFromUrl(client, desc.get("download_url"), "", ifModifiedSince);
        } catch (IOException e) {
            throw new Loader.ResourceNotFoundException(resourceBase + name, e);
        }
    }

    public static class Factory implements LoaderFactory {
        @Override
        public String supportedProtocol() {
            return "github";
        }

        @Override
        public Loader getLoader(String base, String username, String password) {
            final int firstSlash = base.indexOf('/');
            final int secondSlash = base.indexOf('/', firstSlash + 1);
            final String project, resourceBase;
            if (secondSlash > 0) {
                project = base.substring(0, secondSlash);
                resourceBase = base.substring(secondSlash + 1);
            } else {
                project = base;
                resourceBase = null;
            }
            return new GithubLoader(username, project, resourceBase, null);
        }
    }
}
