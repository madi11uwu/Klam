package pe.edu.pucp.klam.rest.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class EquipoDTO {
    private int idEquipo;
    private boolean activo;
    private String nombre;
    private String categoria;
    private Map<String, String> especificaciones = new LinkedHashMap<>();
    private boolean disponible;

    public EquipoDTO() {
    }

    public int getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(int idEquipo) {
        this.idEquipo = idEquipo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Map<String, String> getEspecificaciones() {
        return especificaciones;
    }

    public void setEspecificaciones(Map<String, String> especificaciones) {
        this.especificaciones = especificaciones;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
