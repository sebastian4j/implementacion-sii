package cl.sebastian.bel.firma;

import java.io.IOException;
import java.io.StringReader;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Clase para buscar el contenido texto de un nodo.
 * @author Sebastian Avila A.
 */
public class BuscarTxtNodo {
    /** contenido xml. */
    private final String xml;
    /** nombre a buscar. */
    private final String nombre;
    /** prefijo de la busqueda. */
    private final String prefijo;
    /** contenido obtenido. */
    private String buscado;
    /** parser. */
    private final DOMParser parser;

    /**
     *
     * @param xml
     * @param nombre
     * @param prefijo
     * @throws SAXException error sax
     * @throws IOException error E/S
     */
    public BuscarTxtNodo(String xml, String nombre, String prefijo) throws SAXException, IOException {
        this.xml = xml;
        this.nombre = nombre;
        this.prefijo = prefijo;
        parser = new DOMParser();
        parser.parse(new InputSource(new StringReader(this.xml)));
    }
    /**
     * obtener el documento.
     * @return
     */
    public Document getDocument() {
        return parser.getDocument();
    }
    /**
     * buscar un elemento.
     * @param d
     */
    public void buscar(Node d) {
        switch (d.getNodeType()) {
        case Node.DOCUMENT_NODE:
            buscarDocumentNode(d);
            break;
        case Node.ELEMENT_NODE:
            if (d.getLocalName().equals(nombre) && (prefijo != null ? d.getPrefix().equals(prefijo) : true)) {
                buscado = d.getTextContent();
                break;
            }
            buscarElementNode(d);
            break;
        default:
            // sin default
            break;
        }
    }

    /**
     * buscar un element node.
     * @param d
     */
    private void buscarElementNode(final Node d) {
        final NodeList ln = d.getChildNodes();
        for (int a = 0; a < ln.getLength(); a++) {
            buscar(ln.item(a));
        }
    }
    /**
     * busca el document node.
     * @param d
     */
    private void buscarDocumentNode(final Node d) {
        final Document dc = (Document)d;
        final NodeList nl = dc.getChildNodes();
        for (int a = 0; a < nl.getLength(); a++) {
            buscar(nl.item(a));
        }
    }
    /**
     * obtener el resultado de la busqueda.
     * @return
     */
    public String getBuscado() {
        return buscado;
    }
}
