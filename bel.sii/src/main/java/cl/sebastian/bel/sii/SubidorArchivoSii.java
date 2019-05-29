package cl.sebastian.bel.sii;

import cl.sebastian.bel.sii.exceptions.SiiSubirArchivoException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * subir archivos al SII.
 *
 * @author Sebastian Avila A.
 */

public class SubidorArchivoSii {
    /** logger de la clase. */
    private static final Logger LOGGER = LogManager.getLogger(SubidorArchivoSii.class);

    /**
     * subir una cesion al SII.
     *
     * @param usuario
     * @param archivo
     * @param empresa
     * @param email
     * @throws IOException error E/S
     * @throws SAXException error sax
     * @throws ParserConfigurationException error parser
     * @throws SiiSubirArchivoException error al subir el archivo
     */
    public SubidaArchivoSii subir(final String token, final File archivo, final Empresa empresa, final String email,
        final String host, final String url) throws SiiSubirArchivoException, IOException, SAXException, ParserConfigurationException {
        final StringBuilder builder = new StringBuilder();
        try {
            if (token == null || token.isEmpty()) {
                throw new SiiSubirArchivoException("sin token para subir el archivo");
            }
            final HttpClient httpclient = new DefaultHttpClient();
            final HttpPost httppost = new HttpPost(url);
            final MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("rutCompany", new StringBody(String.valueOf(empresa.getRut())));
            reqEntity.addPart("dvCompany", new StringBody(empresa.getDv()));
            reqEntity.addPart("emailNotif", new StringBody(email));
            final FileBody bin = new FileBody(archivo);
            reqEntity.addPart("archivo", bin);
            httppost.setEntity(reqEntity);
            final BasicClientCookie cookie = new BasicClientCookie("TOKEN", token);
            cookie.setPath("/");
            cookie.setDomain(host);
            cookie.setSecure(true);
            cookie.setVersion(1);
            final CookieStore cookieStore = new BasicCookieStore();
            cookieStore.addCookie(cookie);
            httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
            httppost.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
            final HttpContext localContext = new BasicHttpContext();
            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            httppost.addHeader(new BasicHeader("User-Agent", "Mozilla/4.0 (compatible; PROG 1.0; Windows NT 5.0; YComp 5.0.2.4)"));
            httppost.addHeader(new BasicHeader("Cookie", "TOKEN=" + token));
            final HttpResponse response = httpclient.execute(httppost, localContext);
            final StatusLine sl = response.getStatusLine();
            if (sl.getStatusCode() != javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
                throw new SiiSubirArchivoException("error subiendo el archivo al sii");
            }
            final HttpEntity resEntity = response.getEntity();
            final InputStream is = resEntity.getContent();
            int lee;
            while ((lee = is.read()) != -1) {
                builder.append((char) lee);
            }
            LOGGER.debug("mensaje recibido: " + builder);
            // obtener el xml
        } catch (IOException  e) {
            LOGGER.error("error subir archivo", e);
            throw new SiiSubirArchivoException("error al intentar subir el archivo", e);
        }
        final SubidaArchivoSii retorno = analizarRespuesta(builder.toString());
        retorno.setEmpresa(empresa.getRut());
        retorno.setFechaSubida(new Date());
        return retorno;
    }

    /**
     * analiza la respuesta que el sii retorna.
     * @param txtSii texto retornado por el sii
     * @return
     * @throws ParserConfigurationException error de parser
     * @throws SAXException error sax
     * @throws IOException error E/S
     */
    private SubidaArchivoSii analizarRespuesta(final String txtSii) throws ParserConfigurationException,
        SAXException, IOException {
        final SubidaArchivoSii retorno = new SubidaArchivoSii();
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(new InputSource(new StringReader(txtSii)));
        final NodeList recepcion = doc.getElementsByTagName("RECEPCIONDTE");
        if (recepcion.getLength() > 0) {
            final NodeList status = doc.getElementsByTagName("STATUS");
            if (status != null && status.getLength() > 0) {
                retorno.setCodigoRetornado(Integer.parseInt(status.item(0).getTextContent()));
                if (retorno.getCodigoRetornado() == 0) {
                    retorno.setSubido(true);
                    retorno.setError(false);
                } else {
                    retorno.setError(true);
                    final NodeList nlerr = doc.getElementsByTagName("ERROR");
                    if (nlerr != null && nlerr.getLength() > 0) {
                        retorno.setMensajeRetornado(nlerr.item(0).getTextContent());
                    }
                }
            }
            final NodeList trackid = doc.getElementsByTagName("TRACKID");
            if (trackid != null && trackid.getLength() > 0) {
                retorno.setTrackId(Long.parseLong(trackid.item(0).getTextContent()));
            }
        }
        return retorno;
    }
}
