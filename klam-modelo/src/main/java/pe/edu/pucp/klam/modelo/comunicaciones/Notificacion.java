package pe.edu.pucp.klam.modelo.comunicaciones;

import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;
import java.time.LocalDateTime;

public class Notificacion {
    private int idNotificacion; // Cambiado a camelCase
    private LocalDateTime fechaHora;
    private String titulo;
    private String mensaje;
    private boolean estadoLeida; // Cambiado a camelCase
    private UsuarioPlataforma destinatario; // <-- LA CLAVE DEL POLIMORFISMO

    public Notificacion() {}

    public Notificacion(int idNotificacion, LocalDateTime fechaHora, String titulo, String mensaje, boolean estadoLeida, UsuarioPlataforma destinatario) {
        this.idNotificacion = idNotificacion;
        this.fechaHora = fechaHora;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.estadoLeida = estadoLeida;
        this.destinatario = destinatario;
    }

    public int getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(int idNotificacion) { this.idNotificacion = idNotificacion; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public boolean isEstadoLeida() { return estadoLeida; }
    public void setEstadoLeida(boolean estadoLeida) { this.estadoLeida = estadoLeida; }

    public UsuarioPlataforma getDestinatario() { return destinatario; }
    public void setDestinatario(UsuarioPlataforma destinatario) { this.destinatario = destinatario; }
}