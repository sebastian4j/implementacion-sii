package cl.sebastian.bel.sii;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

/**
 * Clase para obtener la semilla desde el SII.
 * @author Sebastian Avila A.
 */
public class SemillaSoap {
    /** url del wsdl. */
    private final String url;
    /** espacio de nombres. */
    private final String espacio;
    /** factory. */
    private final MessageFactory messageFactory;
    /** soap message. */
    private final SOAPMessage soapMessage;
    /** soap part. */
    private final SOAPPart soapPart;
    /** soap envelope. */
    private final SOAPEnvelope envelope;
    /** semilla obtenido. */
    private String semilla;
    /** token obtenido. */
    private String token;

    /**
     * instancia la clase.
     * @param url url del servicio del SII
     * @throws MalformedURLException error de wsdl entregado
     * @throws SOAPException error en el servicio
     */
    public SemillaSoap(String url) throws MalformedURLException, SOAPException {
        this.url = url;
        final URL u = new URL(url);
        espacio = u.getProtocol() + "://" + u.getHost() + u.getPath();
        messageFactory = MessageFactory.newInstance();
        soapMessage = messageFactory.createMessage();
        soapPart = soapMessage.getSOAPPart();
        envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("SOAP-ENC", "http://schemas.xmlsoap.org/soap/encoding/");
        envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
        envelope.addNamespaceDeclaration("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");
    }

    /**
     * solicitar la semilla al sii.
     * @throws SOAPException error soap
     * @throws IOException error E/S
     */
    public String solicitarSemilla() throws SOAPException, IOException {
        final SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        final SOAPConnection soapConnection = soapConnectionFactory.createConnection();
        final SOAPBody soapBody = envelope.getBody();
        soapBody.addChildElement("getSeed", "m", espacio);
        soapMessage.saveChanges();
        soapMessage.writeTo(System.out);
        final SOAPMessage soapResponse = soapConnection.call(soapMessage, url);
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        soapResponse.writeTo(b);
        soapResponse.writeTo(System.out);
        semilla = b.toString(StandardCharsets.UTF_8.displayName());
        envelope.recycleNode();
        return semilla;
    }

    /**
     * enviar la semilla al sii para obtener el token.
     * @param contenido
     * @throws SOAPException error soap
     * @throws IOException error E/S
     */
    public String enviarSemillaParaObtenerToken(String contenido) throws SOAPException, IOException {
        final SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        final SOAPConnection soapConnection = soapConnectionFactory.createConnection();
        final SOAPBody soapBody = envelope.getBody();
        final SOAPElement soapBodyElem = soapBody.addChildElement("getToken", "m", espacio);
        final SOAPElement elemento = soapBodyElem.addChildElement("pszXml");
        elemento.addAttribute(envelope.createQName("type", "xsi"), "xsd:string");
        elemento.addTextNode(contenido);
        soapMessage.saveChanges();
        soapMessage.writeTo(System.out);
        final SOAPMessage soapResponse = soapConnection.call(soapMessage, url);
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        soapResponse.writeTo(b);
        soapResponse.writeTo(System.out);
        soapResponse.writeTo(System.out);
        token = b.toString(StandardCharsets.UTF_8.displayName());
        envelope.recycleNode();
        return token;
    }

    /**
     * obtener la semilla.
     * @return
     */
    public String getSemilla() {
        return semilla;
    }

    /**
     * obtener el token.
     * @return
     */
    public String getToken() {
        return token;
    }
}

