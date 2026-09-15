package pe.edu.pucp.klam.modelo.agendaoperaciones;

public class Equipo {
    private int id_equipo;
    private String nombre;
    private CategoriaEquipo categoria;
    //Map<String,Object> especificaciones
    private boolean disponible;
    private boolean activo;

    public Equipo(final Equipo equipo){
        if(equipo==null){
            throw new IllegalArgumentException("equipo no puede ser nulo");
        }
        setId_equipo(equipo.getId_equipo());
        setNombre(equipo.getNombre());
        setCategoria(equipo.getCategoria());
        setDisponible(equipo.isDisponible());
        setActivo(equipo.isActivo());
    }
    public int getId_equipo() {
        return id_equipo;
    }

    public void setId_equipo(int id_equipo) {
        this.id_equipo = id_equipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public CategoriaEquipo getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaEquipo categoria) {
        if (categoria==null){
            throw new IllegalArgumentException("categoria nula");
        }
        this.categoria = categoria;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

}
