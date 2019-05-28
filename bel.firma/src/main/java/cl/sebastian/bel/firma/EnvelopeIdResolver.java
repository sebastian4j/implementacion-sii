package cl.sebastian.bel.firma;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.CallbackLookup;
import org.apache.ws.security.message.DOMCallbackLookup;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Clase para resolver las referencias en el mismo documento.
 * @author Sebastian Avila A.
 */
public class EnvelopeIdResolver extends ResourceResolverSpi {
    /** informacion del documento. */
    private WSDocInfo wsDocInfo;
    /** elemeneto selecionado. */
    private Element selectedElem;
    /** logger de la clase. */
    private static final Logger LOGGER = LogManager.getLogger(EnvelopeIdResolver.class);

    /**
     * establecer la referencia.
     * @param docInfo
     */
    public void setWsDocInfo(WSDocInfo docInfo) {
        wsDocInfo = docInfo;
    }

    /**
     * obtener el elemento seleccionado.
     * @param id identificador a buscar
     */
    @SuppressWarnings("deprecation")
    private void obtenerElementoSeleccionad(final String id) {
        if (wsDocInfo != null) {
            selectedElem = wsDocInfo.getProtectionElement(id);
        }

        if (selectedElem == null && wsDocInfo != null) {
            selectedElem = wsDocInfo.getTokenElement(id);
        }
    }

    @Override
    public XMLSignatureInput engineResolve(final Attr uri, final String baseURI) throws ResourceResolverException {
        final String uriNodeValue = uri.getNodeValue();
        final String id = uriNodeValue.substring(1);
        obtenerElementoSeleccionad(id);

        if (selectedElem == null && id != null) {
            CallbackLookup callbackLookup = null;
            if (wsDocInfo != null) {
                callbackLookup = wsDocInfo.getCallbackLookup();
            }
            if (callbackLookup == null) {
                callbackLookup = new DOMCallbackLookup(uri.getOwnerDocument());
            }
            try {
                selectedElem = callbackLookup.getElement(id, null, true);
            } catch (WSSecurityException ex) {
                LOGGER.error(ex);
                throw new ResourceResolverException(
                     ex.getMessage(), new Object[]{"Id: " + id + " no encontrada"}, uri, baseURI);
            }
            if (selectedElem == null) {
                throw new ResourceResolverException("generic.EmptyMessage",
                         new Object[]{"Id: " + id + " no encontrada"}, uri, baseURI);
            }
        }

        final XMLSignatureInput result = new XMLSignatureInput(selectedElem);
        result.setMIMEType("text/xml");

        return result;
    }

    @Override
    public boolean engineCanResolve(Attr uri, String baseURI) {
        final boolean ret;
        if (uri == null) {
            ret = false;
        } else {
            final String uriNodeValue = uri.getNodeValue();
            ret = uriNodeValue.startsWith("#");
        }
        return ret;
    }
}
