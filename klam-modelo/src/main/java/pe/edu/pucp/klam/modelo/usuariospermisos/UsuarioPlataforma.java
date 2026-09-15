package pe.edu.pucp.klam.modelo.usuariospermisos;

public abstract class  UsuarioPlataforma {
    private String username;
    private String passwordHash;
    private String email;
    private String nombres;
    private String apellidos;
    private String estado; //registro??

    public UsuarioPlataforma(final UsuarioPlataforma usuarioPlataforma){
        if(usuarioPlataforma==null){
            throw new IllegalArgumentException("usuarioPlataforma no puede ser nullo");
        }
        setUsername(usuarioPlataforma.getUsername());
        setPasswordHash(usuarioPlataforma.getPasswordHash());
        setEmail(usuarioPlataforma.getEmail());
        setApellidos(usuarioPlataforma.getApellidos());
        setApellidos(usuarioPlataforma.getApellidos());
        setEstado(usuarioPlataforma.getEstado());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if(username==null || username.isEmpty()){
            throw new IllegalArgumentException("username no puede ser nulo o vacío");
        }
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if(email==null || email.isEmpty()){
            throw new IllegalArgumentException("email no puede ser nulo o vacío");
        }
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        if(passwordHash==null || passwordHash.isEmpty()){
            throw new IllegalArgumentException("passwordHash no puede ser nulo o vacío");
        }
        this.passwordHash = passwordHash;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        if(nombres==null || nombres.isEmpty()){
            throw new IllegalArgumentException("nombre no puede ser nulo o vacío");
        }
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        if(apellidos==null || apellidos.isEmpty()){
            throw new IllegalArgumentException("apellidos no puede ser nulo o vacío");
        }
        this.apellidos = apellidos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if(estado==null || estado.isEmpty()) {
            throw new IllegalArgumentException("estado no puede ser nulo o vacío");
        }
        this.estado = estado;
    }
}
