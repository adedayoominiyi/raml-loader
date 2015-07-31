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

import java.io.InputStream;

/**
 *
 */
public class CompositeLoader implements Loader {
    private final Loader[] loaders;

    public CompositeLoader(Loader... loaders) {
        this.loaders = loaders;
    }

    @Override
    public InputStream fetchResource(String name, long ifModifiedSince) {
        for (final Loader loader : loaders) {
            try {
                return loader.fetchResource(name, ifModifiedSince);
            } catch (ResourceNotFoundException e) {
                //ignore
            }
        }
        throw new ResourceNotFoundException(name);
    }

    @Override
    public String config() {
        String s = "composite";
        for (final Loader loader : loaders) {
            s += "-" + loader.config();
        }
        return s;
    }
}
