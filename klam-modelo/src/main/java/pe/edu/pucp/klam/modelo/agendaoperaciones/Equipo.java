package pe.edu.pucp.klam.modelo.agendaoperaciones;

import pe.edu.pucp.klam.modelo.Registro;

import java.util.Map;

public class Equipo extends Registro {
    private String nombre;
    private CategoriaEquipo categoria;
    private Map<String,Object> especificaciones;
    private boolean disponible;

    public Map<String, Object> getEspecificaciones() {
        return especificaciones;
    }

    public void setEspecificaciones(Map<String, Object> especificaciones) {
        this.especificaciones = especificaciones;
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
}
