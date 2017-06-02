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
package guru.nidi.loader.use.raml;

import guru.nidi.loader.Loader;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;

public class RamlLoad {
    private final Loader loader;
    private final boolean caching;

    public RamlLoad(Loader loader) {
        this(loader, false);
    }

    public RamlLoad(Loader loader, boolean caching) {
        this.loader = loader;
        this.caching = caching;
    }

    public RamlModelResult load(String name) {
        return caching
                ? new RamlCache(loader).loadRaml(name)
                : new RamlModelBuilder(new LoaderRamlResourceLoader(loader)).buildApi(name);
    }
}
