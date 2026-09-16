package pe.edu.pucp.klam.modelo.interfaces;


import pe.edu.pucp.klam.modelo.comunicaciones.Notificacion;

public interface Notificable {
    void recibirNotificacion(Notificacion notificacion);
}