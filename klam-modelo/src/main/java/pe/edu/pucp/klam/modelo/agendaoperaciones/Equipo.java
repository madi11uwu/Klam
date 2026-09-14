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

    //Dominio

    public boolean verificarDisponibilidad() {
        return super.isActivo() && disponible;
    }

    public void reservar() {
        if (super.isActivo() && disponible) {
            disponible = false;
        }
    }

    public void liberar() {
        if (super.isActivo()) {
            disponible = true;
        }
    }

    public void desactivar() {
        setActivo(false);
        disponible = false;
    }

    public void activar() {
        setActivo(true);
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
