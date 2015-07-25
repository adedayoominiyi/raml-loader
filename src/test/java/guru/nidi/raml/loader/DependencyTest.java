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

import jdepend.framework.DependencyConstraint;
import jdepend.framework.JDepend;
import jdepend.framework.JavaPackage;
import jdepend.framework.PackageFilter;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 *
 */
public class DependencyTest {
    private static final String BASE = "guru.nidi.raml.loader";
    private static JDepend depend;

    @BeforeClass
    public static void init() throws IOException {
        depend = new JDepend(new PackageFilter(Arrays.asList("org.", "java.", "com.", "javax.")));
        depend.addDirectory("target/classes");
        depend.analyze();
    }

    @Test
    public void dependencies() throws IOException {
        DependencyConstraint constraint = new DependencyConstraint();

        final JavaPackage
                base = constraint.addPackage(BASE),
                apidesigner = constraint.addPackage(BASE + ".apidesigner"),
                model = constraint.addPackage(BASE + ".model"),
                repo = constraint.addPackage(BASE + ".repo"),
                std = constraint.addPackage(BASE + ".std"),
                url = constraint.addPackage(BASE + ".url");

        base.dependsUpon(model);
        base.dependsUpon(apidesigner);
        base.dependsUpon(url);
        base.dependsUpon(std);

        apidesigner.dependsUpon(repo);
        apidesigner.dependsUpon(url);
        apidesigner.dependsUpon(model);

        repo.dependsUpon(model);

        std.dependsUpon(model);

        url.dependsUpon(model);

        assertTrue("Dependency mismatch", depend.dependencyMatch(constraint));
    }

    @Test
    public void noCircularDependencies() throws IOException {
        assertFalse("Cyclic dependencies", depend.containsCycles());
    }

    @Test
    public void maxDistance() throws IOException {
        @SuppressWarnings("unchecked")
        final Collection<JavaPackage> packages = depend.getPackages();

        System.out.println("Name                                      abst  inst  dist");
        System.out.println("----------------------------------------------------------");
        for (JavaPackage pack : packages) {
            if (pack.getName().startsWith("guru.")) {
                System.out.printf("%-40s: %-1.2f  %-1.2f  %-1.2f%n", pack.getName(), pack.abstractness(), pack.instability(), pack.distance());
                assertEquals("Distance exceeded: " + pack.getName(), 0, pack.distance(), .60f);
            }
        }
    }

}
