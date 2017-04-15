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
package guru.nidi.loader.basic;

import guru.nidi.loader.LoadingException;

import java.io.*;

public abstract class CachingLoaderInterceptor implements LoaderInterceptor {
    @Override
    public InputStream loaded(String name, InputStream result) {
        try (final InputStream in = new BufferedInputStream(result);
             final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final byte[] buf = new byte[10000];
            int read;
            while ((read = in.read(buf)) > 0) {
                out.write(buf, 0, read);
            }
            final byte[] data = out.toByteArray();
            processLoaded(name, data);
            return new ByteArrayInputStream(data);
        } catch (IOException e) {
            throw new LoadingException("Problem reading from input '" + name + "'", e);
        }
    }

    protected abstract void processLoaded(String name, byte[] data);
}
