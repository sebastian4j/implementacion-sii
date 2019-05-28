package cl.sebastian.bel.firma;

import javax.xml.crypto.Data;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dom.DOMURIReference;
import org.apache.jcp.xml.dsig.internal.dom.DOMSubTreeData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ws.security.WSDocInfo;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.IdResolver;
import org.apache.xml.security.utils.resolver.ResourceResolver;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Clase para permitir que las URI referencias puedan ser dentro del mismo documento.
 * @author Sebastian Avila A.
 */
@SuppressWarnings("deprecation")
public class UriLocal implements URIDereferencer {
    /** informacion del documento. */
    private WSDocInfo wsDocInfo;
    /** logger de la clase. */
    private static final Logger LOGGER = LogManager.getLogger(UriLocal.class);

    /**
     * establecer docinfo.
     * @param docInfo
     */
    public void setWsDocInfo(WSDocInfo docInfo) {
        wsDocInfo = docInfo;
    }

    @Override
    public Data dereference(URIReference uriRef, XMLCryptoContext context) throws URIReferenceException {
        final String uri = uriRef.getURI();
        final DOMURIReference domRef = (DOMURIReference)uriRef;
        final Attr uriAttr = (Attr)domRef.getHere();
        final DOMCryptoContext dcc = (DOMCryptoContext)context;


        if (uri != null && uri.length() != 0 && uri.charAt(0) == '#') {
            //es el mismo documento
            String id = uri.substring(1);

            if (id.startsWith("xpointer(id(")) {
                int i1 = id.indexOf('\'');
                int i2 = id.indexOf('\'', i1 + 1);
                id = id.substring(i1 + 1, i2);
            }
            final Node referencedElem = dcc.getElementById(id);
            if (referencedElem != null) {
                IdResolver.registerElementById((Element) referencedElem, uriAttr);
            }
        }
        final String baseURI = context.getBaseURI();

        XMLSignatureInput in = null;
        try {
            final EnvelopeIdResolver envelopeResolver = new EnvelopeIdResolver();
            if (envelopeResolver.engineCanResolve(uriAttr, baseURI)) {
                envelopeResolver.setWsDocInfo(wsDocInfo);
                in = envelopeResolver.engineResolve(uriAttr, baseURI);
            } else {
                final ResourceResolver resolver = ResourceResolver.getInstance(uriAttr, baseURI);
                in = resolver.resolve(uriAttr, baseURI);
            }
        } catch (ResourceResolverException e) {
            LOGGER.error("error dereference", e);
        }

        return new DOMSubTreeData(in.getSubNode(), in.isExcludeComments());
    }
}
