package guru.nidi.loader.basic;

import java.io.*;

/**
 *
 */
public abstract class CachingLoaderInterceptor implements LoaderInterceptor {
    @Override
    public InputStream loaded(String name, InputStream result) {
        try (final BufferedInputStream in = new BufferedInputStream(result);
             final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final byte[] buf = new byte[10000];
            int read;
            while ((read = in.read(buf)) > 0) {
                out.write(buf, 0, read);
            }
            final byte[] data = out.toByteArray();
            processLoaded(name, data);
            return new ByteArrayInputStream(data);
        } catch (IOException e) {
            throw new RuntimeException("Problem reading from input '" + name + "'", e);
        }
    }

    protected abstract void processLoaded(String name, byte[] data);
}
