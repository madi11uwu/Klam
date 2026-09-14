package pe.edu.pucp.klam.modelo.agendaoperaciones;

import java.util.Map;

public class Equipo {
    private int id_equipo;
    private String nombre;
    private CategoriaEquipo categoria;
    private Map<String,Object> especificaciones;
    private boolean disponible;
    private boolean activo;

    public Map<String, Object> getEspecificaciones() {
        return especificaciones;
    }

    public void setEspecificaciones(Map<String, Object> especificaciones) {
        this.especificaciones = especificaciones;
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

    //Dominio

    public boolean verificarDisponibilidad() {
        return activo && disponible;
    }

    public void reservar() {
        if (activo && disponible) {
            disponible = false;
        }
    }

    public void liberar() {
        if (activo) {
            disponible = true;
        }
    }

    public void desactivar() {
        activo = false;
        disponible = false;
    }

    public void activar() {
        activo = true;
    }

    public void agregarEspecificacion(String clave, Object valor) {
        especificaciones.put(clave, valor);
    }

    public Object obtenerEspecificacion(String clave) {
        return especificaciones.get(clave);
    }

    public void eliminarEspecificacion(String clave) {
        especificaciones.remove(clave);
    }
}
