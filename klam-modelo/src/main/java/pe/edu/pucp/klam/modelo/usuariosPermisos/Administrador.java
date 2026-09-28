package pe.edu.pucp.klam.modelo.usuariosPermisos;

import pe.edu.pucp.klam.modelo.comunicaciones.Notificacion;

public class Administrador extends UsuarioPlataforma {


    public Administrador() {
        super();
        setRol("ADMINISTRADOR");
    }

    public Administrador(int id_usuario, String username, String passwordHash, String email,
                          String nombres, String apellidos, boolean activo) {
        super(id_usuario, username, passwordHash, email, nombres, apellidos, "ADMINISTRADOR", activo);
    }
    //holiiii

    public Administrador(Administrador otro) {
        super(otro);
    }


    // Los siguientes métodos dependen de clases de otros módulos/roles
    // (Cirugia, DocumentoIngreso, Cliente, Cotizacion, NotaCredito).
    // Tiran el mensaje pq las demás clases no están implementadas

    public void crearCirugia(Object cirugia) {
        throw new UnsupportedOperationException("Metodo no implementado aun.");
    }

    public void modificarCirugia(Object cirugia) {
        throw new UnsupportedOperationException("Metodo no implementado aun.");
    }

    public boolean validarDocumentoIngreso(Object documentoIngreso) {
        throw new UnsupportedOperationException("Metodo no implementado aun.");
    }

    public void aprobarDocumentacionYAgendar(Object cliente) {
        throw new UnsupportedOperationException("Metodo no implementado aun.");
    }

    public void gestionarCotizacion(Object cotizacion) {
        throw new UnsupportedOperationException("Metodo no implementado aun.");
    }

    public void emitirDocumentoFacturacion(Object cirugia) {
        throw new UnsupportedOperationException("Metodo no implementado aun.");
    }

    public void reaperturarCirugiaConNotaCredito(Object cirugia, Object notaCredito) {
        throw new UnsupportedOperationException("Metodo no implementado aun.");
    }

    @Override
    public String toString() {
        return "Administrador{" +
                ", " + super.toString() +
                '}';
    }

    @Override
    public void recibirNotificacion(Notificacion notificacion) {

    }
}
