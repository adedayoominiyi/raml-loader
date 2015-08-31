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

import jdepend.framework.DependencyConstraint;
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
    private static final String BASE = "guru.nidi.loader";
    private static JDepend depend;

    @BeforeClass
    public static void init() throws IOException {
        final List<String> ramlParserPackages = Arrays.asList("org.raml.model", "org.raml.parser");
        depend = new JDepend(new PackageFilter(Arrays.asList("org.", "java.", "com.", "javax.")) {
            @Override
            public boolean accept(String packageName) {
                return ramlParserPackages.contains(packageName) || super.accept(packageName);
            }
        });
        depend.addDirectory("target/classes");
        for (final String rpp : ramlParserPackages) {
            depend.addPackage(rpp);
        }
        depend.analyze();
    }

    @Test
    public void dependencies() throws IOException {
        DependencyConstraint constraint = new DependencyConstraint();

        final JavaPackage
                apidesigner = constraint.addPackage(BASE + ".apidesigner"),
                basic = constraint.addPackage(BASE + ".basic"),
                repo = constraint.addPackage(BASE + ".repository"),
                url = constraint.addPackage(BASE + ".url"),
                jsonschema = constraint.addPackage(BASE + ".use.jsonschema"),
                raml = constraint.addPackage(BASE + ".use.raml"),
                xml = constraint.addPackage(BASE + ".use.xml"),
                base = constraint.addPackage(BASE),
                parserModel = constraint.addPackage("org.raml.model"),
                parserParser = constraint.addPackage("org.raml.parser");

        apidesigner.dependsUpon(repo);
        apidesigner.dependsUpon(url);
        apidesigner.dependsUpon(base);

        basic.dependsUpon(base);

        jsonschema.dependsUpon(base);

        raml.dependsUpon(parserModel);
        //jdepend is not clever enough to find this dependency
//        raml.dependsUpon(parserParser);
        raml.dependsUpon(base);

        xml.dependsUpon(base);

        repo.dependsUpon(base);

        url.dependsUpon(base);

        assertThat(depend, matches(constraint));
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
