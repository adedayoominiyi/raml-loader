package guru.nidi.loader.basic;

import java.io.InputStream;

/**
 *
 */
public interface LoaderInterceptor {
    InputStream loaded(String name, InputStream result);
}
