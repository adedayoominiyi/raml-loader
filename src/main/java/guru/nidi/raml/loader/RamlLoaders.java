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
package guru.nidi.raml.loader;

import guru.nidi.raml.loader.apidesigner.ApiRamlLoader;
import guru.nidi.raml.loader.impl.*;
import org.raml.model.Raml;
import org.raml.parser.visitor.RamlDocumentBuilder;

import java.io.File;

/**
 *
 */
public class RamlLoaders {
    private final RamlLoader loader;
    private final boolean caching;

    private RamlLoaders(RamlLoader loader, boolean caching) {
        this.loader = loader;
        this.caching = caching;
    }

    private static RamlLoader classpathLoader(Class<?> basePackage) {
        return classpathLoader(basePackage.getPackage().getName().replace('.', '/'));
    }

    private static RamlLoader classpathLoader(String basePackage) {
        return new ClassPathRamlLoader(basePackage);
    }

    private static RamlLoader fileLoader(File baseDirectory) {
        return new FileRamlLoader(baseDirectory);
    }

    private static RamlLoader urlLoader(String baseUrl) {
        return new UrlRamlLoader(baseUrl);
    }

    private static RamlLoader githubLoader(String token, String project) {
        return new GithubRamlLoader(token, project);
    }

    private static RamlLoader apiPortalLoader(String user, String password) {
        return new ApiRamlLoader(user, password);
    }

    private static RamlLoader apiDesignerLoader(String url) {
        return new ApiRamlLoader(url);
    }

    public static RamlLoaders fromClasspath(Class<?> basePackage) {
        return using(classpathLoader(basePackage));
    }

    public static RamlLoaders fromClasspath(String basePackage) {
        return using(classpathLoader(basePackage));
    }

    public static RamlLoaders fromClasspath() {
        return using(classpathLoader(""));
    }

    public static RamlLoaders fromFile(File baseDirectory) {
        return using(fileLoader(baseDirectory));
    }

    public static RamlLoaders fromUrl(String baseUrl) {
        return using(urlLoader(baseUrl));
    }

    public static RamlLoaders fromGithub(String project) {
        return fromGithub(null, project);
    }

    public static RamlLoaders fromGithub(String token, String project) {
        return using(githubLoader(token, project));
    }

    public static RamlLoaders fromApiPortal(String user, String password) {
        return using(apiPortalLoader(user, password));
    }

    public static RamlLoaders fromApiDesigner(String url) {
        return using(apiDesignerLoader(url));
    }

    /**
     * These URI schemas are supported:
     * <pre>
     * - http://google.com/file.raml
     * - user:password@https://google.com/file.raml
     * - file:///tmp/temp.raml
     * - classpath://guru/nidi/ramltester/simple.raml
     * - token@github://nidi3/raml-tester/src/test/resources/simple.raml
     * - user:password@apiportal://test.raml
     * </pre>
     * TODO - apidesigner://
     *
     * @return
     */
    public static RamlLoaders absolutely() {
        return using(null);
    }

    public static RamlLoaders using(RamlLoader loader) {
        return new RamlLoaders(loader, false);
    }

    public RamlLoaders caching(boolean caching) {
        return new RamlLoaders(loader, caching);
    }

    public Raml load(String name) {
        final RamlLoader decorated = new UriRamlLoader(loader);
        return caching
                ? new CachingRamlLoader(decorated).loadRaml(name)
                : new RamlDocumentBuilder(new RamlLoaderRamlParserResourceLoader(decorated)).build(name);
    }
}
