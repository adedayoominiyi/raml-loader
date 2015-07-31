package guru.nidi.loader.basic;

import guru.nidi.loader.Loader;

import java.io.InputStream;

/**
 *
 */
public class InterceptingLoader implements Loader {
    private final Loader delegate;
    private final LoaderInterceptor interceptor;

    public InterceptingLoader(Loader delegate, LoaderInterceptor interceptor) {
        this.delegate = delegate;
        this.interceptor = interceptor;
    }

    @Override
    public InputStream fetchResource(String name, long ifModifiedSince) {
        final InputStream in = delegate.fetchResource(name, ifModifiedSince);
        return interceptor.loaded(name, in);
    }

    @Override
    public String config() {
        return delegate.config();
    }
}
