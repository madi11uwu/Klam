package pe.edu.pucp.klam.bl;

/** El registro pedido no existe (la capa REST la responde con 404). */
public class NoEncontradoException extends BLException {
    public NoEncontradoException(String message) {
        super(message);
    }
}
