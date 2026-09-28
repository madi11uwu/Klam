package pe.edu.pucp.klam.modelo.comunicaciones;

import java.time.LocalDateTime;


public class Notificacion {
    int id_notifacion;
    LocalDateTime fechaHora;
    String titulo;
    boolean estado_leida;
    TipoNotificacion tipo_notificacion;

    public TipoNotificacion getTipo_notificacion() {
        return tipo_notificacion;
    }

    public void setTipo_notificacion(TipoNotificacion tipo_notificacion) {
        this.tipo_notificacion = tipo_notificacion;
    }


    public Notificacion(int id_notifacion, LocalDateTime fechaHora, String titulo, boolean estado_leida, TipoNotificacion tiponoti) {
        this.id_notifacion = id_notifacion;
        this.fechaHora = fechaHora;
        this.titulo = titulo;
        this.estado_leida = estado_leida;
        this.tipo_notificacion = tiponoti;
    }
    public int getId_notifacion() {
        return id_notifacion;
    }

    public void setId_notifacion(int id_notifacion) {
        this.id_notifacion = id_notifacion;
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
