package pe.edu.pucp.klam.modelo.usuariosPermisos;

public class TecnicoInstrumentista extends UsuarioPlataforma {

    private String especialidad;

    public TecnicoInstrumentista() {
        super();
        this.especialidad = "";
        setRol("TECNICO_INSTRUMENTISTA");
    }

    public TecnicoInstrumentista(int id_usuario, String username, String passwordHash, String email,
                                  String nombres, String apellidos, boolean activo, String especialidad) {
        super(id_usuario, username, passwordHash, email, nombres, apellidos, "TECNICO_INSTRUMENTISTA", activo);
        this.especialidad = especialidad;
    }

    public TecnicoInstrumentista(TecnicoInstrumentista otro) {
        super(otro);
        this.especialidad = otro.especialidad;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

}
