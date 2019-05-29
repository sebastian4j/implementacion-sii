package cl.sebastian.bel.sii;

import cl.sebastian.bel.firma.Firmador;
import cl.sebastian.bel.firma.dominio.RequisitoFirma;
import cl.sebastian.bel.firma.exceptions.FirmaException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.xml.soap.SOAPException;
import javax.xml.transform.TransformerException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * Clase para obtener solicitar la semilla desde el SII, firmarla, solicitar el
 * token obtenerlo y retornarlo.
 *
 * @author Sebastian Avila A.
 */
public class SIIObtenerToken {
    static {
        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
    }
    /** url de la semilla. */
    private final String urlSemilla;
    /** url del token. */
    private final String urlToken;
    /** token obtenido. */
    private String token;
    /** credenciales para la consulta. */
    private final AccesoKeytool ak;
    /** empresa del proceso. */
    private final Empresa empresa;
    /** logger de la clase. */
    private static final Logger LOGGER = LogManager.getLogger(SIIObtenerToken.class);

    /**
     *
     * @param urls url de la semilla
     * @param urlt url del token
     * @param AccesoKeytool datos de acceso al keytool
     * @param Empresa empresa del proceso
     */
    public SIIObtenerToken(String urls, String urlt, final AccesoKeytool akt, final Empresa emp) {
        urlSemilla = urls;
        urlToken = urlt;
        this.ak = akt;
        empresa = emp;
    }

    /**
     * obtener el token.
     *
     * @return
     */
    public String getToken() {
        return token;
    }

    /**
     * solicitar el token al sii.
     *
     * @throws FirmaException error al firmar el documento.
     */
    public String solicitarToken() throws FirmaException {
        try {
            // obtener la semilla desde el SII
            final SemillaSoap sp = new SemillaSoap(urlSemilla);
            sp.solicitarSemilla();
            final BuscarTxtNodo buscarSemilla = new BuscarTxtNodo(sp.getSemilla(), "getSeedReturn", null);
            buscarSemilla.buscar(buscarSemilla.getDocument());
            final String semRetorno = buscarSemilla.getBuscado();
            final BuscarTxtNodo semEstado = new BuscarTxtNodo(semRetorno, "ESTADO", null);
            semEstado.buscar(semEstado.getDocument());
            final BuscarTxtNodo semTexto = new BuscarTxtNodo(semRetorno, "SEMILLA", null);
            semTexto.buscar(semTexto.getDocument());

            if ("00".equals(semEstado.getBuscado()) && !"".equals(semTexto.getBuscado())
                    && semTexto.getBuscado().length() > 0 && semTexto.getBuscado() != null) {
                final SemillaXML semXML = new SemillaXML(semTexto.getBuscado());
                final RequisitoFirma rf = new RequisitoFirma();
                rf.setAlias(ak.getAliaskt());
                rf.setClaveAlias(ak.getClaveAlias());
                rf.setClaveJks(ak.getClavekt());
                rf.setCodificacionEntrada(StandardCharsets.UTF_8.displayName());
                rf.setOmitirTagXml(true);
                rf.setRevisarUriLocal(false);
                // TODO
                rf.setRutaJks("ruta-jks");
                rf.setEntrada(semXML.getXMLSemilla());
                rf.setIdFirma(null);
                final Firmador f = new Firmador(rf);
                final String firmado = f.firmar();
                final SemillaSoap tkn = new SemillaSoap(urlToken);
                tkn.enviarSemillaParaObtenerToken(firmado);
                final BuscarTxtNodo respSII = new BuscarTxtNodo(tkn.getToken(), "getTokenReturn", null);
                respSII.buscar(respSII.getDocument());
                final BuscarTxtNodo tknEstado = new BuscarTxtNodo(respSII.getBuscado(), "ESTADO", null);
                tknEstado.buscar(tknEstado.getDocument());
                final BuscarTxtNodo tknTexto = new BuscarTxtNodo(respSII.getBuscado(), "TOKEN", null);
                tknTexto.buscar(tknTexto.getDocument());

                if ("00".equals(tknEstado.getBuscado()) && !"".equals(tknTexto.getBuscado())
                        && tknTexto.getBuscado().length() > 0 && tknTexto.getBuscado() != null) {
                    token = tknTexto.getBuscado();
                } else {
                    throw new SOAPException("token no Obtenido");
                }
            } else {
                throw new SOAPException("semilla no Obtenido");
            }
        } catch (SOAPException | IOException | SAXException | ClassNotFoundException | InstantiationException
                | IllegalAccessException | TransformerException e) {
            LOGGER.error("error al obtener token", e);
            throw new FirmaException("no se pudo obtener el token", e);
        }
        return getToken();
    }

}
