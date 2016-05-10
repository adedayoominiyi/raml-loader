package guru.nidi.loader.use.jsonschema;

import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import guru.nidi.loader.basic.ClassPathLoader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import static org.junit.Assert.assertTrue;

/**
 *
 */
public class JsonSchemaTest {
    private final ClassPathLoader loader = new ClassPathLoader("guru/nidi/loader");

    @Test
    public void jsonSchemaWithLoader() throws IOException, ProcessingException {
        final JsonSchemaFactory factory = LoaderUriDownloader.createJsonSchemaFactory(loader);
        assertTrue(validate(factory).isSuccess());
    }

    @Test(expected = ProcessingException.class)
    public void jsonSchemaWithoutLoader() throws IOException, ProcessingException {
        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        validate(factory);
    }

    private ProcessingReport validate(JsonSchemaFactory factory) throws ProcessingException, IOException {
        final JsonSchema jsonSchema = factory.getJsonSchema(JsonLoader.fromReader(new InputStreamReader(loader.fetchResource("ref.json", -1), "utf-8")));
        return jsonSchema.validate(JsonLoader.fromReader(new StringReader("\"blu\"")));
    }
}
