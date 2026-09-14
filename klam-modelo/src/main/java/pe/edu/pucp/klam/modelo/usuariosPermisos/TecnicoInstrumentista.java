package pe.edu.pucp.klam.modelo.usuariosPermisos;

public class TecnicoInstrumentista extends UsuarioPlataforma {

    private String id_tecnico;
    private String especialidad;

    public TecnicoInstrumentista() {
        super();
        this.id_tecnico = "";
        this.especialidad = "";
        setRol("TECNICO_INSTRUMENTISTA");
    }

    public TecnicoInstrumentista(int id_usuario, String username, String passwordHash, String email,
                                  String nombres, String apellidos, boolean activo,
                                  String id_tecnico, String especialidad) {
        super(id_usuario, username, passwordHash, email, nombres, apellidos, "TECNICO_INSTRUMENTISTA", activo);
        this.id_tecnico = id_tecnico;
        this.especialidad = especialidad;
    }

    public TecnicoInstrumentista(TecnicoInstrumentista otro) {
        super(otro);
        this.id_tecnico = otro.id_tecnico;
        this.especialidad = otro.especialidad;
    }

    public String getIdTecnico() {
        return id_tecnico;
    }

    public void setIdTecnico(String id_tecnico) {
        this.id_tecnico = id_tecnico;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    // Depende de la clase Cirugia (módulo de Agenda y Operaciones).
    public void registrarIncidencia(Object cirugia, String descripcion) {
        throw new UnsupportedOperationException("Metodo no implementado aun.");
    }

    @Override
    public String toString() {
        return "TecnicoInstrumentista{" +
                "id_tecnico='" + id_tecnico + '\'' +
                ", especialidad='" + especialidad + '\'' +
                ", " + super.toString() +
                '}';
    }
}
