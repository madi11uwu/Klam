package pe.edu.pucp.klam.modelo.usuariospermisos;

public class TecnicoInstrumentista extends  UsuarioPlataforma{
    private String id_tecnico;
    private String especialidad;

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        if(especialidad==null || especialidad.isEmpty()){
            throw new IllegalArgumentException("especialidad no puede ser nulo o vacío");
        }
        this.especialidad = especialidad;
    }

    public String getId_tecnico() {
        return id_tecnico;
    }

    public void setId_tecnico(String id_tecnico) {
        if(id_tecnico==null || id_tecnico.isEmpty()){
            throw new IllegalArgumentException("id_tecnico no puede ser nulo o vacío");
        }
        this.id_tecnico = id_tecnico;
    }

    public TecnicoInstrumentista(final TecnicoInstrumentista tecnicoInstrumentista) {
        if(tecnicoInstrumentista==null){
            throw new IllegalArgumentException("tecnicoInstrumentista no puede ser nulo");
        }
        super(tecnicoInstrumentista);
        setId_tecnico(tecnicoInstrumentista.getId_tecnico());
        setEspecialidad(tecnicoInstrumentista.getEspecialidad());
    }
}
