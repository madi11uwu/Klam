package pe.edu.pucp.klam.modelo.usuariosPermisos;

public class Administrador extends UsuarioPlataforma {

    private String id_admin;

    public Administrador() {
        super();
        this.id_admin = "";
        setRol("ADMINISTRADOR");
    }

    public Administrador(int id_usuario, String username, String passwordHash, String email,
                          String nombres, String apellidos, boolean activo, String id_admin) {
        super(id_usuario, username, passwordHash, email, nombres, apellidos, "ADMINISTRADOR", activo);
        this.id_admin = id_admin;
    }

    public Administrador(Administrador otro) {
        super(otro);
        this.id_admin = otro.id_admin;
    }

    public String getiAdmin() {
        return id_admin;
    }

    public void setidAdmin(String id_admin) {
        this.id_admin = id_admin;
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
                "idAdmin='" + id_admin + '\'' +
                ", " + super.toString() +
                '}';
    }
}
