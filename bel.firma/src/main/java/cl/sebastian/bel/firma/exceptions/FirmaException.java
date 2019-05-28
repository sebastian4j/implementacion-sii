package cl.sebastian.bel.firma.exceptions;

/**
 *
 * @author Sebastian Avila A.
 */
public class FirmaException extends Exception {
    public FirmaException() {
        super();
    }
    public FirmaException(final String error) {
        super(error);
    }
    public FirmaException(final Throwable error) {
        super(error);
    }
    public FirmaException(final String error, final Throwable thr) {
        super(error, thr);
    }
}
