package cl.sebastian.bel.firma.dominio;

/**
 * contiene los datos requeridos para la firma del documento.
 * @author Sebastian Avila A.
 */
public class RequisitoFirma {
    /** xml como string para firmar. */
    private String entrada;
    /** indica si se revisa la uri local. */
    private boolean revisarUriLocal;
    /** id para firmar, sin incluir #. */
    private String idFirma;
    /** alias en el jks. */
    private String alias;
    /** ruta absoluta al jks. */
    private String rutaJks;
    /** clave de acceso al jks. */
    private String claveJks;
    /** clave de acceso al alias. */
    private String claveAlias;
    /** valor de la referencia.  */
    private String codificacionEntrada;
    /** indicador de omitir el xml en la salida. */
    private boolean omitirTagXml;
    /** espacio de nombres de la firma. */
    private String espacioFirma;

    public String getEntrada() {
        return entrada;
    }

    public void setEntrada(String entrada) {
        this.entrada = entrada;
    }

    public boolean isRevisarUriLocal() {
        return revisarUriLocal;
    }

    public void setRevisarUriLocal(boolean revisarUriLocal) {
        this.revisarUriLocal = revisarUriLocal;
    }

    public String getIdFirma() {
        return idFirma;
    }

    public void setIdFirma(String idFirma) {
        this.idFirma = idFirma;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getRutaJks() {
        return rutaJks;
    }

    public void setRutaJks(String rutaJks) {
        this.rutaJks = rutaJks;
    }

    public String getClaveJks() {
        return claveJks;
    }

    public void setClaveJks(String claveJks) {
        this.claveJks = claveJks;
    }

    public String getClaveAlias() {
        return claveAlias;
    }

    public void setClaveAlias(String claveAlias) {
        this.claveAlias = claveAlias;
    }

    public String getCodificacionEntrada() {
        return codificacionEntrada;
    }

    public void setCodificacionEntrada(String codificacionEntrada) {
        this.codificacionEntrada = codificacionEntrada;
    }

    public boolean isOmitirTagXml() {
        return omitirTagXml;
    }

    public void setOmitirTagXml(boolean omitirTagXml) {
        this.omitirTagXml = omitirTagXml;
    }

    public String getEspacioFirma() {
        return espacioFirma;
    }

    public void setEspacioFirma(String espacioFirma) {
        this.espacioFirma = espacioFirma;
    }


}
