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
package guru.nidi.raml.loader.url;

import guru.nidi.raml.loader.model.RamlLoader;
import guru.nidi.raml.loader.model.RamlLoaderFactory;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class UrlRamlLoader implements RamlLoader {
    protected final String base;
    protected final CloseableHttpClient client;
    protected final UrlFetcher fetcher;

    public UrlRamlLoader(String base, UrlFetcher fetcher, CloseableHttpClient httpClient) {
        this.base = base;
        this.fetcher = fetcher;
        this.client = httpClient == null
                ? HttpClientBuilder.create().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build()
                : httpClient;
    }

    public UrlRamlLoader(String base, UrlFetcher fetcher) {
        this(base, fetcher, null);
    }

    public UrlRamlLoader(String baseUrl) {
        this(baseUrl, new SimpleUrlFetcher(), null);
    }

    @Override
    public InputStream fetchResource(String name, long ifModifiedSince) {
        try {
            // remove AutoCloseInputStream as soon as
            // https://github.com/raml-org/raml-java-parser/issues/72 is fixed
            return new AutoCloseInputStream(fetcher.fetchFromUrl(client, base, name, ifModifiedSince));
        } catch (IOException e) {
            throw new ResourceNotFoundException(name, e);
        }
    }

    @Override
    public String config() {
        return "url-" + base;
    }

    public static class HttpFactory implements RamlLoaderFactory {
        @Override
        public String supportedProtocol() {
            return "http";
        }

        @Override
        public RamlLoader getRamlLoader(String base, String username, String password) {
            return username == null
                    ? new UrlRamlLoader("http://" + base)
                    : new BasicAuthUrlRamlLoader("http://" + base, username, password);
        }
    }

    public static class HttpsFactory implements RamlLoaderFactory {
        @Override
        public String supportedProtocol() {
            return "https";
        }

        @Override
        public RamlLoader getRamlLoader(String base, String username, String password) {
            return username == null
                    ? new UrlRamlLoader("https://" + base)
                    : new BasicAuthUrlRamlLoader("https://" + base, username, password);
        }
    }
}
