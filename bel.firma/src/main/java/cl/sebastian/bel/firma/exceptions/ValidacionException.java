package cl.sebastian.bel.firma.exceptions;

/**
 * error al validar el xml contra el xsd.
 * @author Sebastian Avila A.
 */
public class ValidacionException extends Exception {
    public ValidacionException() {
        super();
    }
    public ValidacionException(final String error) {
        super(error);
    }
    public ValidacionException(final Throwable error) {
        super(error);
    }
    public ValidacionException(final String error, final Throwable thr) {
        super(error, thr);
    }
}
