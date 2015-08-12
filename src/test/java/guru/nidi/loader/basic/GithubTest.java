package guru.nidi.loader.basic;

import guru.nidi.loader.url.GithubLoader;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static guru.nidi.loader.basic.LoaderTest.assertStreamStart;
import static guru.nidi.loader.util.TestUtils.getEnv;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;

/**
 *
 */
public class GithubTest {

    @BeforeClass
    public static void init() {
        assumeTrue("/Users/nidi".equals(System.getenv("HOME")));
    }

    @Test
    public void publicGithub() throws IOException {
        final InputStream in = new GithubLoader("nidi3/raml-loader").fetchResource("src/test/resources/guru/nidi/loader/simple.raml", -1);
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void publicGithubNotModified() throws IOException {
        assertNull(new GithubLoader("nidi3/raml-tester").fetchResource("src/test/resources/guru/nidi/ramltester/simple.raml", new Date(130, 0, 1).getTime()));
    }

    @Test
    public void publicGithubModified() throws IOException {
        final InputStream in = new GithubLoader("nidi3/raml-tester").fetchResource("src/test/resources/guru/nidi/ramltester/simple.raml", new Date(100, 0, 1).getTime());
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void privateGithub() throws IOException {
        final InputStream in = new GithubLoader(getEnv("GITHUB_TOKEN"), "nidi3/blog").fetchResource("README.md", -1);
        assertStreamStart(in, "blog");
    }
}