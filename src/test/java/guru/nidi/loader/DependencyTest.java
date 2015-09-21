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
package guru.nidi.loader;

import jdepend.framework.DependencyDefiner;
import jdepend.framework.JDepend;
import jdepend.framework.JavaPackage;
import jdepend.framework.PackageFilter;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static jdepend.framework.DependencyMatchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class DependencyTest {
    private static JDepend depend;

    @BeforeClass
    public static void init() throws IOException {
        final List<String> ramlParserPackages = Arrays.asList("org.raml.model", "org.raml.parser");
        depend = new JDepend(PackageFilter.empty().excluding("org.", "java.", "com.", "javax.", "org.raml.parser.").including(ramlParserPackages));
        depend.addDirectory("target/classes");
        for (final String rpp : ramlParserPackages) {
            depend.addPackage(rpp);
        }
        depend.analyze();
    }

    @Test
    public void dependencies() throws IOException {
        class OrgRaml {
            JavaPackage model, parser;
        }
        final OrgRaml orgRaml = new OrgRaml();

        class GuruNidiLoader implements DependencyDefiner {
            JavaPackage self, apidesigner, basic, repository, url, useJsonschema, useRaml, useXml;

            @Override
            public void dependUpon() {
                apidesigner.dependsUpon(repository, url, self);
                basic.dependsUpon(self);
                useJsonschema.dependsUpon(self);
                useRaml.dependsUpon(orgRaml.model, self);
                useXml.dependsUpon(self);
                repository.dependsUpon(self);
                url.dependsUpon(self);
            }
        }

        assertThat(depend, matchesPackages(orgRaml, new GuruNidiLoader()));
    }

    @Test
    public void noCircularDependencies() throws IOException {
        assertThat(depend, hasNoCycles());
    }

    @Test
    public void maxDistance() throws IOException {
        System.out.println(distances(depend, "guru."));
        assertThat(depend, hasMaxDistance("guru.", .5));
    }

}
