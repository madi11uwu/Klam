package pe.edu.pucp.klam.modelo.usuariospermisos;

public class Administrador extends UsuarioPlataforma{
    private String id_admin;

    public Administrador(final Administrador administrador) {
        if(administrador==null){
            throw new IllegalArgumentException("Administrador no puede ser nulo");
        }
        super(administrador);
        setId_admin(administrador.getId_admin());
    }

    public String getId_admin() {
        return id_admin;
    }

    public void setId_admin(String id_admin) {
        if(id_admin == null || id_admin.isEmpty()){
            throw new IllegalArgumentException("id_admin no puede ser nulo o vacío");
        }
        this.id_admin = id_admin;
    }

}
