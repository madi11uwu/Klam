package pe.edu.pucp.klam.modelo.usuariosPermisos;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.comunicaciones.Notificacion;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Cotizacion;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.NotaCredito;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;

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

    public void crearCirugia(Cirugia cirugia) {
        throw new UnsupportedOperationException("Metodo no implementado aun.");
    }

    public void modificarCirugia(Cirugia cirugia) {
        throw new UnsupportedOperationException("Metodo no implementado aun.");
    }

    public boolean validarDocumentoIngreso(DocumentoIngreso documentoIngreso) {
        throw new UnsupportedOperationException("Metodo no implementado aun.");
    }

    public void aprobarDocumentacionYAgendar(Cliente cliente) {
        throw new UnsupportedOperationException("Metodo no implementado aun.");
    }

    public void gestionarCotizacion(Cotizacion cotizacion) {
        throw new UnsupportedOperationException("Metodo no implementado aun.");
    }

    public void emitirDocumentoFacturacion(Cirugia cirugia) {
        throw new UnsupportedOperationException("Metodo no implementado aun.");
    }

    public void reaperturarCirugiaConNotaCredito(Cirugia cirugia, NotaCredito notaCredito) {
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
