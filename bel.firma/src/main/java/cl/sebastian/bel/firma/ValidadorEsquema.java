package cl.sebastian.bel.firma;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import cl.sebastian.bel.firma.exceptions.ValidacionException;

/**
 * realiza la validacion del xml contra el xsd.
 * @author Sebastián Ávila A.
 *
 */
public class ValidadorEsquema {
    private final String xml;
    private final String xsd;

    /**
     * instancia el validador.
     * @param xml contenido del archivo xml
     * @param xsd ruta del xsd
     */
    public ValidadorEsquema(final String xml, final String xsd) {
        this.xml = xml;
        this.xsd = xsd;
    }

    /**
     * realiza la validacion del xml contra el xsd.
     * @throws ValidacionException error al validar el xml contra el xsd.
     */
    public void validar() throws ValidacionException {
        try {
            final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = factory.newSchema(new StreamSource[] {
                new StreamSource(xsd)
            });
            schema.newValidator().validate(new StreamSource(new StringReader(xml)));
        } catch (SAXException | IOException e) {
            throw new ValidacionException("error al validar el xml contra el xsd", e);
        }
    }
}
