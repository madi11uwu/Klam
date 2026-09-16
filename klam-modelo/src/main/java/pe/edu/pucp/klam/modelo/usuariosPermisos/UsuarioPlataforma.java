package pe.edu.pucp.klam.modelo.usuariosPermisos;

public abstract class UsuarioPlataforma {

    private int id_usuario;
    private String username;
    private String passwordHash;
    private String email;
    private String nombres;
    private String apellidos;
    private String rol;
    private boolean activo;

    public UsuarioPlataforma() {
        this.id_usuario = 0;
        this.username = "";
        this.passwordHash = "";
        this.email = "";
        this.nombres = "";
        this.apellidos = "";
        this.rol = "";
        this.activo = true;
    }

    public UsuarioPlataforma(int id_usuario, String username, String passwordHash, String email,
                              String nombres, String apellidos, String rol, boolean activo) {
        this.id_usuario = id_usuario;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.rol = rol;
        this.activo = activo;
    }

    public UsuarioPlataforma(UsuarioPlataforma otro) {
        this.id_usuario = otro.id_usuario;
        this.username = otro.username;
        this.passwordHash = otro.passwordHash;
        this.email = otro.email;
        this.nombres = otro.nombres;
        this.apellidos = otro.apellidos;
        this.rol = otro.rol;
        this.activo = otro.activo;
    }

    public int getIdUsuario() {
        return id_usuario;
    }

    public void setIdUsuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getNombreCompleto() {
        return this.nombres + " " + this.apellidos;
    }

    public void observarCalendario() {
        // Depende del módulo de Agenda y Operaciones (Cirugia), aún no implementados.
        throw new UnsupportedOperationException("Metodo no implementado aun.");
    }

    @Override
    public String toString() {
        return "UsuarioPlataforma{" +
                "id_usuario=" + id_usuario +
                ", username='" + username + '\'' +
                ", nombreCompleto='" + getNombreCompleto() + '\'' +
                ", rol='" + rol + '\'' +
                ", activo=" + activo +
                '}';
    }
}
