/*
 * Copyright © 2015 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.loader.use.xml;

import guru.nidi.loader.basic.ClassPathLoader;
import org.junit.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class XmlTest {
    private final ClassPathLoader loader = new ClassPathLoader("guru/nidi/loader");

    @Test
    public void xmlSchemaWithLoader() throws SAXException, IOException {
        final SchemaFactory factory = LoaderLSResourceResolver.createXmlSchemaFactory(loader);
        assertEquals(0, validate(factory).errors);
    }

    @Test
    public void xmlSchemaWithoutLoader() throws SAXException, IOException {
        final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        assertTrue(validate(factory).errors > 0);
    }

    private CountingErrorHandler validate(SchemaFactory schemaFactory) throws SAXException, IOException {
        final Schema schema = schemaFactory.newSchema(new StreamSource(loader.fetchResource("ref.xsd", -1)));
        final Validator validator = schema.newValidator();
        final CountingErrorHandler errorHandler = new CountingErrorHandler();
        validator.setErrorHandler(errorHandler);
        validator.validate(new StreamSource(new StringReader("<api-request xmlns='myNS'><input>bla</input></api-request>")));
        return errorHandler;
    }

    private static class CountingErrorHandler implements ErrorHandler {
        int errors;

        @Override
        public void warning(SAXParseException exception) throws SAXException {
            errors++;
            System.out.println(exception);
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            errors++;
            System.out.println(exception);
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            errors++;
            System.out.println(exception);
        }
    }
}

