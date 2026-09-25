package pe.edu.pucp.klam.bl;

public class BLException extends RuntimeException {
    public BLException(String message) {
        super(message);
    }
    public BLException(String message, Throwable cause) {
        super(message, cause);
    }
}
