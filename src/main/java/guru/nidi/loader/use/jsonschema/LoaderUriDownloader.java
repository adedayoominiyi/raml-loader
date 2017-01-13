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
package guru.nidi.loader.use.jsonschema;

import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfigurationBuilder;
import com.github.fge.jsonschema.core.load.download.URIDownloader;
import com.github.fge.jsonschema.core.load.uri.URITranslatorConfiguration;
import com.github.fge.jsonschema.core.report.ReportProvider;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonSchemaFactoryBuilder;
import guru.nidi.loader.Loader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class LoaderUriDownloader implements URIDownloader {
    private final Loader delegate;

    public LoaderUriDownloader(Loader delegate) {
        this.delegate = delegate;
    }

    public static JsonSchemaFactory createJsonSchemaFactory(Loader loader) {
        return createJsonSchemaFactory(loader, null, null, null);
    }

    public static JsonSchemaFactory createJsonSchemaFactory(Loader loader, LoadingConfiguration loadingConfiguration, ReportProvider reportProvider, ValidationConfiguration validationConfiguration) {
        final JsonSchemaFactoryBuilder builder = JsonSchemaFactory.newBuilder();
        final String scheme = loader.getClass().getSimpleName();
        final LoadingConfigurationBuilder lcb = (loadingConfiguration == null ? LoadingConfiguration.byDefault() : loadingConfiguration).thaw()
                .addScheme(scheme, new LoaderUriDownloader(loader))
                .setURITranslatorConfiguration(URITranslatorConfiguration.newBuilder().setNamespace(scheme + ":///").freeze());
        builder.setLoadingConfiguration(lcb.freeze());
        if (reportProvider != null) {
            builder.setReportProvider(reportProvider);
        }
        if (validationConfiguration != null) {
            builder.setValidationConfiguration(validationConfiguration);
        }
        return builder.freeze();
    }

    @Override
    public InputStream fetch(URI source) throws IOException {
        return delegate.fetchResource(source.getPath(), -1);
    }
}
