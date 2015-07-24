package guru.nidi.raml.loader.util;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assume.assumeThat;

/**
 *
 */
public class TestUtils {
    private TestUtils() {
    }

    public static String getEnv(String name) {
        final String env = System.getenv(name);
        assumeThat("Environment variable " + name + " is not set, skipping test", env, notNullValue());
        return env;
    }

}
