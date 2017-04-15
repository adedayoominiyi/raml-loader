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
package guru.nidi.loader.apidesigner;

import guru.nidi.loader.Loader;
import guru.nidi.loader.LoaderFactory;
import guru.nidi.loader.repository.RepositoryLoader;
import guru.nidi.loader.url.FormLoginUrlFetcher;
import guru.nidi.loader.url.SimpleUrlFetcher;
import guru.nidi.loader.url.UrlLoader;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;

public class ApiLoader extends RepositoryLoader {
    public ApiLoader(String user, String password) {
        this(null, user, password);
    }

    public ApiLoader(String base, String user, String password) {
        super(new UrlLoader("http://api-portal.anypoint.mulesoft.com",
                new FormLoginUrlFetcher("rest/raml/v1", "ajax/apihub/login-register/form?section=login", user, password, "name", "pass") {
                    @Override
                    protected void postProcessLoginParameters(List<NameValuePair> parameters) {
                        parameters.add(new BasicNameValuePair("form_id", "user_login"));
                    }
                }
        ), base, "files", ApiPortalFilesResponse.class);
    }

    public ApiLoader(String baseUrl) {
        super(new UrlLoader(baseUrl, new SimpleUrlFetcher()), null, "files", ApiDesignerFilesResponse.class);
    }

    public static class PortalFactory implements LoaderFactory {
        @Override
        public String supportedProtocol() {
            return "apiportal";
        }

        @Override
        public Loader getLoader(String base, String username, String password) {
            return new ApiLoader(base, username, password);
        }
    }

    public static class DesignerFactory implements LoaderFactory {
        @Override
        public String supportedProtocol() {
            return "apidesigner";
        }

        @Override
        public Loader getLoader(String base, String username, String password) {
            return new ApiLoader(base);
        }
    }
}
