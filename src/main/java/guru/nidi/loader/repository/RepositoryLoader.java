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
package guru.nidi.loader.repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.nidi.loader.Loader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 *
 */
public class RepositoryLoader implements Loader {
    private final Loader loader;
    private final String defaultResourceName;
    private final String responseName;
    private final Class<? extends RepositoryResponse> responseClass;
    private RepositoryResponse response;

    public RepositoryLoader(Loader loader, String defaultResourceName, String responseName, Class<? extends RepositoryResponse> responseClass) {
        this.loader = loader;
        this.defaultResourceName = defaultResourceName;
        this.responseName = responseName;
        this.responseClass = responseClass;
    }

    @Override
    public InputStream fetchResource(String resourceName, long ifModifiedSince) {
        if (response == null) {
            response = load();
        }
        final String name = resourceName != null ? resourceName : defaultResourceName;
        final RepositoryEntry entry = findEntry(name);
        if (entry == null) {
            throw new ResourceNotFoundException(name);
        }
        return new ByteArrayInputStream(entry.getContent().getBytes(Charset.forName("utf-8")));
    }

    @Override
    public String config() {
        return "repository-" + defaultResourceName + "-" + loader.config();
    }

    protected RepositoryResponse load() {
        final ObjectMapper mapper = createMapper();
        final InputStream files = loader.fetchResource(responseName, -1);
        //TODO when empty, files is an empty array, not object!?
        try {
            return mapper.readValue(files, responseClass);
        } catch (IOException e) {
            throw new ResourceNotFoundException(responseName, e);
        }
    }

    private ObjectMapper createMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    private RepositoryEntry findEntry(String name) {
        for (final RepositoryEntry file : response.getFiles()) {
            if (name.equals(file.getName()) || name.equals(file.getPath())) {
                return file;
            }
        }
        return null;
    }

}
