package cl.sebastian.bel.firma;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import cl.sebastian.bel.firma.dominio.RequisitoFirma;
import cl.sebastian.bel.firma.exceptions.AccesoKeyStoreException;
import cl.sebastian.bel.firma.exceptions.FirmaException;


/**
 * realiza el proceso de firma de un documento.
 * @author Sebastian Avila A.
 */
public class Firmador {
    /** factory de firmas. */
    public static final XMLSignatureFactory XMLSF = XMLSignatureFactory.getInstance("DOM");
    /** lista de transformaciones. */
    private final List<Transform> transformList;
    /** referencia. */
    private Reference ref;
    /** certificado. */
    private X509Certificate cert;
    /** entrada del keystore. */
    private KeyStore.PrivateKeyEntry keyEntry;
    /** nodo de la firma. */
    private SignedInfo signedInfo;
    /** nodo keyinfo. */
    private KeyInfo keyInfo;
    /** factory para el documento. */
    private final DocumentBuilderFactory dbf;
    /** logger de la clase. */
    private static final Logger LOGGER = LogManager.getLogger(Firmador.class);
    /** requisitos para realizar la firma. */
    private final RequisitoFirma rf;
    /** contiene la salida al firmar el archivo. */
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    /**
     * Constructor con los archivos de entrada y salida.
     * @param rf datos con los requisitos de la firma.
     */
    public Firmador(final RequisitoFirma rf) {
        this.rf = rf;
        transformList = new ArrayList<>();
        dbf =  DocumentBuilderFactory.newInstance();
    }

    /**
     * realiza la firma del documento.
     * @return documento firmado
     * @throws FirmaException error al realizar el proceso de la firma.
     */
    public String firmar() throws FirmaException {
        String retorno = null;
        try {
            LOGGER.info(rf.getAlias() + " - " + rf.getRutaJks() + " - " + rf.getClaveJks() + " - "+ rf.getClaveAlias());
            entrarKeyStore(rf.getAlias(), rf.getRutaJks(), rf.getClaveJks(), rf.getClaveAlias());
            if (rf.getIdFirma() != null) {
                referencias("#" + rf.getIdFirma());
            } else {
                referencias("");
            }
            transformaciones();
            certificado();
            firmarReferencia(rf.getCodificacionEntrada(), rf.isOmitirTagXml());
            final SAXReader reader = new SAXReader();
            retorno = baos.toString(rf.getCodificacionEntrada());
        } catch (AccesoKeyStoreException | FirmaException | InvalidAlgorithmParameterException |
                KeyException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
            LOGGER.error("error al realizar la firma", e);
            throw new FirmaException("error en el proceso de firma", e);
        }
        return retorno;
    }

    /**
     * Entra al JKS para obtener el certificado.
     * @param aliasCertificado
     * @param rutaJKS
     * @param claveJKS
     * @param claveCertificado
     * @throws AccesoKeyStoreException error al acceder al keystore.
     */
    private void entrarKeyStore(String aliasCertificado, String rutaJKS, String claveJKS, String claveCertificado)
            throws AccesoKeyStoreException {
        try {
            KeyStore.ProtectionParameter protParam;
            protParam = new KeyStore.PasswordProtection(claveCertificado.toCharArray());
            final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(new FileInputStream(rutaJKS), claveJKS.toCharArray());
            keyEntry = (KeyStore.PrivateKeyEntry)ks.getEntry(aliasCertificado, protParam);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException |
            CertificateException | UnrecoverableEntryException e) {
            throw new AccesoKeyStoreException("error al entrar al keystore", e);
        }
    }

    /**
     * agregar todas las transformaciones que sean necesarias al documento (una).
     * @throws NoSuchAlgorithmException error de algoritmo
     * @throws InvalidAlgorithmParameterException error de algoritmo
     */
    private void transformaciones() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        final String espacio;
        if (rf.getEspacioFirma() != null) {
            espacio = rf.getEspacioFirma() + "#enveloped-signature";
        } else {
            espacio = "http://www.w3.org/2000/09/xmldsig#enveloped-signature";
        }
        transformList.add(XMLSF.newTransform(espacio,
                         (TransformParameterSpec)null));
    }
    /**
     * agregar la referencia interna al documento para la firma.
     * @param uri
     * @throws NoSuchAlgorithmException error de algoritmo
     * @throws InvalidAlgorithmParameterException error de algoritmo
     */
    private void referencias(String uri) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        ref = XMLSF.newReference(uri, XMLSF.newDigestMethod(DigestMethod.SHA1, null), // digest
                            transformList, // transformaciones
                            null, // tipo de referencia type="contenidoDelString" o null
                            null); // id="ContenidoDelString" o null
    }
    /**
     * obtener el certificado y la clave privada para la firma.
     * @throws NoSuchAlgorithmException error de algoritmo
     * @throws InvalidAlgorithmParameterException error de algoritmo
     * @throws KeyException error de keystore
     */
    private void certificado() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyException {
        cert = (X509Certificate) keyEntry.getCertificate();
        signedInfo = XMLSF.newSignedInfo(
                    XMLSF.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec)null),
                    XMLSF.newSignatureMethod(getMetodoSign(cert.getSigAlgName()), null),
                    Collections.singletonList(ref));

        final KeyInfoFactory kif = Firmador.XMLSF.getKeyInfoFactory();
        final X509Data xd = kif.newX509Data(Collections.singletonList(cert));

        KeyValue keyValue;
        keyValue = kif.newKeyValue(cert.getPublicKey());
        final List<XMLStructure> kviItems = new ArrayList<>();
        kviItems.add(keyValue);
        kviItems.add(xd);
        keyInfo = kif.newKeyInfo(kviItems);
    }
    /**
     * realizar la firma sobre el documento con las transformaciones y escribir la salida.
     * @return
     * @throws ParserConfigurationException error de parser
     * @throws SAXException error sax
     * @throws IOException error E/S
     * @throws MarshalException error de marshaller
     * @throws XMLSignatureException error de firma
     * @throws TransformerConfigurationException error de transformacion
     * @throws TransformerException error de transformacion
     */
    private String firmardte() throws ParserConfigurationException, SAXException, IOException, MarshalException,
            XMLSignatureException, TransformerConfigurationException, TransformerException {
        dbf.setNamespaceAware(true);

        final Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(rf.getEntrada())));
        final DOMSignContext dsc = new DOMSignContext(keyEntry.getPrivateKey(), doc.getDocumentElement());

        final XMLSignature signature = XMLSF.newXMLSignature(signedInfo, keyInfo);

        if (rf.isRevisarUriLocal()) {
            dsc.setURIDereferencer(new UriLocal());
        }

        signature.sign(dsc);
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer trans = tf.newTransformer();
        final String encod = "ISO-8859-1";
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final StreamResult sr = new StreamResult(new OutputStreamWriter(bos, encod));
        trans.setOutputProperty(OutputKeys.ENCODING, encod);
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.METHOD, "xml");
        trans.transform(new DOMSource(doc), sr);
        final byte[] outputBytes = bos.toByteArray();
        final String docFirmado = new String(outputBytes, encod);
        return docFirmado;
    }

    /**
     * firma el documento incluyendo la referencia.
     * @param charset
     * @param salida
     * @param omitirXml
     * @param agregarcharset
     * @throws FirmaException error al firmar el documento.
     */
    private void firmarReferencia(final String charset, final boolean omitirXml)
            throws FirmaException {
        try {
            org.apache.xml.security.Init.init();


            dbf.setNamespaceAware(true);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("entrada:");
                LOGGER.debug(rf.getEntrada());
            }
            final Document doc = dbf.newDocumentBuilder().parse(
                    new ByteArrayInputStream(rf.getEntrada().getBytes(charset)));
            final DOMSignContext dsc = new DOMSignContext(keyEntry.getPrivateKey(), doc.getDocumentElement());
            final XMLSignature signature = XMLSignatureFactory.getInstance("DOM").newXMLSignature(signedInfo, keyInfo);

            if (rf.isRevisarUriLocal()) {
                dsc.setURIDereferencer(new UriLocal());
            } else {
                dsc.setURIDereferencer(null);
            }
            signature.sign(dsc);
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer trans = tf.newTransformer();
            if (omitirXml) {
                trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            } else {
                trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            }
            trans.transform(new DOMSource(doc), new StreamResult(baos));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("salida:");
                LOGGER.debug(baos.toString());
            }
        } catch (IOException | IllegalArgumentException | MarshalException | ParserConfigurationException | SAXException
                | TransformerException | TransformerFactoryConfigurationError | XMLSignatureException e) {
            LOGGER.error("error de firma", e);
        }

    }
    /**
     * obtener el tipo de metodo de firma segun el tipo de certificado.
     * @param algoritmo
     * @return
     */
    private String getMetodoSign(String algoritmo) {
        String result = "";

        if (algoritmo.contains("SHA")) {
            result = SignatureMethod.RSA_SHA1;
        } else if (algoritmo.contains("DSA")) {
            result = SignatureMethod.DSA_SHA1;
        }
        return result;
    }
}
