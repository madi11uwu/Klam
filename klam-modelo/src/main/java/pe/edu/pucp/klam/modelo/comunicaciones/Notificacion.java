package pe.edu.pucp.klam.modelo.comunicaciones;

import java.time.LocalDateTime;


public class Notificacion {
    String id_notifacion;
    LocalDateTime fechaHora;
    String titulo;
    boolean estado_leida;

    public Notificacion(String id_notifacion, LocalDateTime fechaHora, String titulo, boolean estado_leida) {
        this.id_notifacion = id_notifacion;
        this.fechaHora = fechaHora;
        this.titulo = titulo;
        this.estado_leida = estado_leida;
    }
    public String getId_notifacion() {
        return id_notifacion;
    }
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora=fechaHora;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo=titulo;
    }
    public boolean isEstado_leida() {
        return estado_leida;
    }

    public void setEstado_leida(boolean estado_leida) {
        this.estado_leida = estado_leida;
    }
}
