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
import guru.nidi.loader.ResourceNotFoundException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class GithubLoader extends UrlLoader {
    private final String token;
    private final String user;
    private final String project;
    private final String resourceBase;
    private final String ref;
    private final CloseableHttpClient httpClient;

    GithubLoader(final String token, String user, String project, String resourceBase, String ref, CloseableHttpClient httpClient) {
        super("https://api.github.com/repos/" + user + "/" + project + "/contents" + (ref == null ? "" : "?ref=" + ref),
                new SimpleUrlFetcher() {
                    @Override
                    protected HttpGet postProcessGet(HttpGet get) {
                        if (token != null) {
                            get.addHeader("Authorization", "token " + token);
                        }
                        return get;
                    }
                }, httpClient);
        this.token = token;
        this.user = user;
        this.project = project;
        this.resourceBase = resourceBase;
        this.ref = ref;
        this.httpClient = httpClient;
    }

    public static GithubLoader forPublic(String user, String project) {
        return new GithubLoader(null, user, project, null, null, null);
    }

    public static GithubLoader forPrivate(String token, String user, String project) {
        return new GithubLoader(token, user, project, null, null, null);
    }

    public GithubLoader resourceBase(String resourceBase) {
        return new GithubLoader(token, user, project, resourceBase, ref, httpClient);
    }

    public GithubLoader ref(String ref) {
        return new GithubLoader(token, user, project, resourceBase, ref, httpClient);
    }

    public GithubLoader commit(String commit) {
        return new GithubLoader(token, user, project, resourceBase, commit, httpClient);
    }

    public GithubLoader branch(String branch) {
        return new GithubLoader(token, user, project, resourceBase, branch, httpClient);
    }

    public GithubLoader tag(String tag) {
        return new GithubLoader(token, user, project, resourceBase, tag, httpClient);
    }

    @Override
    public InputStream fetchResource(String name, long ifModifiedSince) {
        final String res = (resourceBase == null || resourceBase.length() == 0) ? "" : (resourceBase + "/");
        try (final InputStream raw = fetcher.fetchFromUrl(client, base, res + name, ifModifiedSince)) {
            if (raw == null) {
                return null;
            }
            @SuppressWarnings("unchecked")
            final Map<String, String> desc = new ObjectMapper().readValue(raw, Map.class);
            return fetcher.fetchFromUrl(client, desc.get("download_url"), "", ifModifiedSince);
        } catch (IOException e) {
            throw new ResourceNotFoundException(res + name, e);
        }
    }

    public static class Factory implements LoaderFactory {
        @Override
        public String supportedProtocol() {
            return "github";
        }

        @Override
        public Loader getLoader(String base, String username, String password) {
            final int queryPos = base.indexOf('?');
            final String path = queryPos < 0 ? base : base.substring(0, queryPos);
            final String query = queryPos < 0 ? "" : base.substring(queryPos + 1);

            final Map<String, String> params = parseQuery(query);
            final int firstSlash = path.indexOf('/');
            final int secondSlash = path.indexOf('/', firstSlash + 1);
            final String user = path.substring(0, firstSlash);
            final String project, resourceBase;
            if (secondSlash > 0) {
                project = path.substring(firstSlash + 1, secondSlash);
                resourceBase = path.substring(secondSlash + 1);
            } else {
                project = path.substring(firstSlash + 1);
                resourceBase = null;
            }
            return new GithubLoader(username, user, project, resourceBase, params.get("ref"), null);
        }

        private Map<String, String> parseQuery(String query) {
            final Map<String, String> params = new HashMap<>();
            for (final String param : query.split("&")) {
                final int pos = param.indexOf('=');
                params.put(pos < 0 ? param : param.substring(0, pos), pos < 0 ? null : param.substring(pos + 1));
            }
            return params;
        }
    }
}
