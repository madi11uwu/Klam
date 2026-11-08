package pe.edu.pucp.klam.modelo.usuariosPermisos;

public class Administrador extends UsuarioPlataforma {
    public Administrador() {
        super();
        setRol("ADMINISTRADOR");
    }
    public Administrador(int id_usuario, String username, String passwordHash, String email,
                          String nombres, String apellidos, boolean activo) {
        super(id_usuario, username, passwordHash, email, nombres, apellidos, "ADMINISTRADOR", activo);
    }
    public Administrador(Administrador otro) {
        super(otro);
    }
}
