package guru.nidi.loader;

import guru.nidi.codeassert.config.For;
import guru.nidi.codeassert.jacoco.CoverageCollector;
import guru.nidi.codeassert.jacoco.JacocoAnalyzer;
import org.junit.Test;

import static guru.nidi.codeassert.jacoco.CoverageType.*;
import static guru.nidi.codeassert.junit.CodeAssertMatchers.hasEnoughCoverage;
import static guru.nidi.loader.util.TestUtils.assumeMyLocalMachine;
import static org.junit.Assert.assertThat;

public class CodeCoverage {
    @Test
    public void coverage() {
        assumeMyLocalMachine();

        //TODO not really good! target is 75,75,75
        final JacocoAnalyzer analyzer = new JacocoAnalyzer(new CoverageCollector(BRANCH, LINE, METHOD)
                .just(For.global().setMinima(25, 40, 40))
                .just(For.allPackages().setMinima(40, 40, 40))
                .just(For.packge("*.apidesigner").setMinima(75, 10, 10))
                .just(For.packge("*.repository").setMinima(0, 40, 60))
                .just(For.packge("*.raml").setMinima(0, 10, 20))
                .just(For.packge("*.basic").setMinima(20, 50, 50))
        );
        assertThat("enough code coverage", analyzer.analyze(), hasEnoughCoverage());
    }
}
