package pe.edu.pucp.klam.modelo.agendaoperaciones;

import pe.edu.pucp.klam.modelo.interfaces.Verificable;

import java.util.HashMap;
import java.util.Map;

public class Equipo implements Verificable {
    private int id_equipo;
    private String nombre;
    private CategoriaEquipo categoria;
    private Map<String,Object> especificaciones;
    private boolean disponible;

    public Equipo(final Equipo equipo){
        if (equipo == null){
            throw new IllegalArgumentException("Equipo no puede ser nulo");
        }
        setId_equipo(equipo.getId_equipo());
        setNombre(equipo.getNombre());
        setCategoria(equipo.getCategoria());
        setEspecificaciones(equipo.getEspecificaciones());
        setDisponible(equipo.isDisponible());
        setEspecificaciones(equipo.getEspecificaciones());
    }
    public int getId_equipo() {
        return id_equipo;
    }

    public void setId_equipo(int id_equipo) {
        this.id_equipo = id_equipo;
    }

    public Map<String, Object> getEspecificaciones() {
        return new HashMap<>(especificaciones);
    }

    public void setEspecificaciones(Map<String, Object> especificaciones) {
        this.especificaciones = new HashMap<>(especificaciones);
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

    @Override
    public boolean verificar() {
        if (disponible) return true;
        return false;
    }
}
