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
package guru.nidi.loader;

import java.io.InputStream;

public interface Loader {

    /**
     * Fetch the resource with the given name.
     * If it has NOT been modified since the given point in time, null should be returned.
     *
     * @param name Resource to be loaded
     * @param ifModifiedSince -1 or max age of resource in millis since 1.1.1970
     * @return Stream to the source or null
     * @throws ResourceNotFoundException if resource has not been found
     */
    InputStream fetchResource(String name, long ifModifiedSince);

    String config();
}
