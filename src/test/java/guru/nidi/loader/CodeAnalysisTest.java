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

import edu.umd.cs.findbugs.Priorities;
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.In;
import guru.nidi.codeassert.dependency.DependencyRule;
import guru.nidi.codeassert.dependency.DependencyRuler;
import guru.nidi.codeassert.dependency.DependencyRules;
import guru.nidi.codeassert.findbugs.BugCollector;
import guru.nidi.codeassert.findbugs.FindBugsAnalyzer;
import guru.nidi.codeassert.findbugs.FindBugsResult;
import guru.nidi.codeassert.junit.CodeAssertTest;
import guru.nidi.codeassert.model.ModelAnalyzer;
import guru.nidi.codeassert.model.ModelResult;
import guru.nidi.codeassert.pmd.*;
import guru.nidi.loader.basic.LoaderTest;
import guru.nidi.loader.basic.UriLoader;
import guru.nidi.loader.basic.UriLoaderTest;
import guru.nidi.loader.use.raml.RamlCache;
import guru.nidi.loader.use.xml.LoaderLSResourceResolver;
import guru.nidi.loader.util.ServerTest;
import net.sourceforge.pmd.RulePriority;
import org.junit.Test;

import static guru.nidi.codeassert.junit.CodeAssertMatchers.packagesMatchExactly;
import static guru.nidi.codeassert.pmd.Rulesets.*;
import static org.junit.Assert.assertThat;

public class CodeAnalysisTest extends CodeAssertTest {
    @Test
    public void dependencies() {
        class OrgRamlV2 extends DependencyRuler {
            DependencyRule api, apiLoader;
        }
        final OrgRamlV2 orgRamlV2 = new OrgRamlV2();

        class GuruNidiLoader extends DependencyRuler {
            DependencyRule _, $self, apidesigner, repository, url, useRaml;

            @Override
            public void defineRules() {
                $self.mayBeUsedBy(_);
                apidesigner.mayUse(repository, url);
                useRaml.mayUse(orgRamlV2.api, orgRamlV2.apiLoader);
            }
        }

        final DependencyRules rules = DependencyRules.denyAll()
                .withExternals("java*", "com*", "org.apache*", "org.w3c*")
                .withRelativeRules(orgRamlV2, new GuruNidiLoader());
        assertThat(modelResult(), packagesMatchExactly(rules));
    }

    @Override
    protected ModelResult analyzeModel() {
        return new ModelAnalyzer(AnalyzerConfig.maven().main()).analyze();
    }

    @Override
    protected FindBugsResult analyzeFindBugs() {
        final BugCollector collector = new BugCollector().minPriority(Priorities.NORMAL_PRIORITY)
                .just(In.clazz(RamlCache.class)
                        //TODO is this a problem?
                        .ignore("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", "DMI_NONSERIALIZABLE_OBJECT_WRITTEN"))
                .because("It's a test",
                        In.clazz(CodeAnalysisTest.class).ignore("UUF_UNUSED_FIELD", "UWF_UNWRITTEN_FIELD"),
                        In.clazz(ServerTest.class).ignore("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
                );
        return new FindBugsAnalyzer(AnalyzerConfig.maven().mainAndTest(), collector).analyze();
    }

    @Override
    protected PmdResult analyzePmd() {
        final ViolationCollector collector = new ViolationCollector().minPriority(RulePriority.MEDIUM)
                .just(In.clazz(LoaderTest.class)
                        .ignore("JUnitTestContainsTooManyAsserts", "JUnitTestsShouldIncludeAssert"))
                .just(In.clazz(CodeAnalysisTest.class)
                        .ignore("SuspiciousConstantFieldName", "VariableNamingConventions", "AvoidDollarSigns"))
                .because("It's a test", In.loc("*Test").ignore("AvoidDuplicateLiterals"))
                .because("It's ok",
                        In.clazz(RamlCache.class).ignore("ArrayIsStoredDirectly"),
                        In.clazz(UriLoaderTest.class).ignore("JUnitTestContainsTooManyAsserts"),
                        In.clazz(LoaderLSResourceResolver.class).ignore("AvoidCatchingGenericException"),
                        In.clazz(UriLoader.class).ignore("UseStringBufferForStringAppends"),
                        In.loc("CodeCoverage").ignore("NoPackage"))
                .because("It's Jackson mapping", In.loc("ApiPortalFile")
                        .ignore("MethodNamingConventions", "VariableNamingConventions"))
                .because("I don't agree", In.everywhere()
                        .ignore("JUnitAssertionsShouldIncludeMessage", "MethodArgumentCouldBeFinal",
                                "AbstractNaming", "EmptyMethodInAbstractClassShouldBeAbstract", "SimplifyStartsWith",
                                "AvoidFieldNameMatchingMethodName"));

        return new PmdAnalyzer(AnalyzerConfig.maven().mainAndTest(), collector)
                .withRuleSets(basic(), braces(), design(), exceptions(), imports(), junit(),
                        optimizations(), strings(), sunSecure(), typeResolution(), unnecessary(), unused(),
                        codesize().tooManyMethods(35),
                        empty().allowCommentedEmptyCatch(true),
                        naming().variableLen(1, 25))
                .analyze();
    }

    @Override
    protected CpdResult analyzeCpd() {
        final MatchCollector collector = new MatchCollector()
                .because("It's Jackson mapping", In.loc("ApiPortal*").ignoreAll());

        return new CpdAnalyzer(AnalyzerConfig.maven().main(), 30, collector).analyze();
    }
}
