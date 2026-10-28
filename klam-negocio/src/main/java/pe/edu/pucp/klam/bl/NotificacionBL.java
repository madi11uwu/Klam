package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.comunicaciones.Notificacion;
import java.util.List;

public interface NotificacionBL extends BaseBL<Notificacion, Integer> {
    void marcarLeida(Integer id, boolean leida) throws BLException;
    List listarPorDestinatario(Integer idAdministrador, Integer idVendedor, Integer idTecnico) throws BLException;
    List listarNoLeidas() throws BLException;

    // ---- Notificaciones automaticas (RF11, RF12) ----
    /** Crea una notificacion interna para cada administrador activo. */
    void notificarAdministradores(String titulo, String mensaje) throws BLException;

    /** Crea una notificacion interna para cada tecnico/instrumentista activo. */
    void notificarTecnicos(String titulo, String mensaje) throws BLException;

    /** true si hay un servidor SMTP configurado (propiedades klam.smtp.* o variables KLAM_SMTP_*). */
    boolean correoConfigurado();

    /**
     * Envia un correo a los administradores activos. Devuelve false si el correo no esta
     * configurado (no es un error); lanza BLException si el envio falla.
     */
    boolean enviarCorreoAdministradores(String asunto, String cuerpo) throws BLException;
}
