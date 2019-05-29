package cl.sebastian.bel.sii.exceptions;

/**
 * error al subir el archivo al sii.
 * @author Sebastián Ávila A.
 *
 */
public class SiiSubirArchivoException extends Exception {
    public SiiSubirArchivoException(final String error) {
        super(error);
    }
    public SiiSubirArchivoException(final Throwable error) {
        super(error);
    }
    public SiiSubirArchivoException(final String txt, final Throwable error) {
        super(txt, error);
    }
}
