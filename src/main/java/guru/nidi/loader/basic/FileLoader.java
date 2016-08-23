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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 *
 */
public class FileLoader implements Loader {
    private final File base;

    public FileLoader(File base) {
        this.base = base;
    }

    @Override
    public InputStream fetchResource(String name, long ifModifiedSince) {
        try {
            final File file = new File(base, name);
            return file.lastModified() > ifModifiedSince
                    ? new FileInputStream(file) : null;
        } catch (FileNotFoundException e) {
            throw new ResourceNotFoundException(name, e);
        }
    }

    @Override
    public String config() {
        return "file-" + base.getAbsolutePath();
    }

    public static class Factory implements LoaderFactory {
        @Override
        public String supportedProtocol() {
            return "file";
        }

        @Override
        public Loader getLoader(String base, String username, String password) {
            return new FileLoader(new File(base));
        }
    }

}
