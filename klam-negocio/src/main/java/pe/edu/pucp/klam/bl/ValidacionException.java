package pe.edu.pucp.klam.bl;

/** Dato inválido o regla de negocio incumplida (la capa REST la responde con 400). */
public class ValidacionException extends BLException {
    public ValidacionException(String message) {
        super(message);
    }
}
