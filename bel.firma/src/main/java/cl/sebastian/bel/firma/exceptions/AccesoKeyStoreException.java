package cl.sebastian.bel.firma.exceptions;

/**
 *
 * @author Sebastian Avila A.
 */
public class AccesoKeyStoreException extends Exception {
    public AccesoKeyStoreException() {
        super();
    }
    public AccesoKeyStoreException(final String error) {
        super(error);
    }
    public AccesoKeyStoreException(final Throwable error) {
        super(error);
    }
    public AccesoKeyStoreException(final String error, final Throwable thr) {
        super(error, thr);
    }
}
