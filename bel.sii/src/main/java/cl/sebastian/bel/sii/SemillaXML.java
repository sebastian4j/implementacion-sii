package cl.sebastian.bel.sii;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Clase para crear un XML con la semilla y firmarla.
 * @author Sebastian Avila A.
 */
public class SemillaXML {
    /** semilla que debe ser firmada. */
    private final String semilla;

    /**
     *
     * @param semilla
     */
    public SemillaXML(String semilla) {
        this.semilla = semilla;
    }

    /**
     *
     * @return
     * @throws ClassNotFoundException error de clase
     * @throws InstantiationException error de instancia
     * @throws IllegalAccessException error de acceso
     * @throws TransformerConfigurationException error de transformacion
     * @throws UnsupportedEncodingException error de codificacion
     * @throws TransformerException error de transformacion
     */
    public String getXMLSemilla() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
        TransformerConfigurationException, UnsupportedEncodingException, TransformerException {
        final DOMImplementation di = new DOMImplementationImpl();
        final Document doc = di.createDocument(null, "getToken", null);
        final Element root = doc.getDocumentElement();
        final Element item = doc.createElement("item");
        final Element semiya = doc.createElement("Semilla");
        final Text texto = doc.createTextNode(semilla);
        semiya.appendChild(texto);
        item.appendChild(semiya);
        root.appendChild(item);
        final String encod = "ISO-8859-1";
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer trans = tf.newTransformer();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final StreamResult sr = new StreamResult(new OutputStreamWriter(bos, encod));
        trans.setOutputProperty(OutputKeys.ENCODING, encod);
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        trans.setOutputProperty(OutputKeys.METHOD, "xml");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.transform(new DOMSource(doc), sr);
        return bos.toString(StandardCharsets.UTF_8.displayName());
    }
}
