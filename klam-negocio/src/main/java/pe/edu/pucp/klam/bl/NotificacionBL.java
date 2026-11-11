package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.comunicaciones.Notificacion;
import java.util.List;

public interface NotificacionBL extends BaseBL<Notificacion, Integer> {
    void marcarLeida(Integer id, boolean leida) throws BLException;
    List <Notificacion> listarPorDestinatario(Integer idAdministrador, Integer idVendedor, Integer idTecnico) throws BLException;
    List <Notificacion> listarNoLeidas() throws BLException;
    void notificarAdministradores(String titulo, String mensaje) throws BLException;
    void notificarTecnicos(String titulo, String mensaje) throws BLException;

    /** Avisa a un vendedor concreto (p. ej. cuando se rechaza una solicitud o un documento que él registró). */
    void notificarVendedor(Integer idVendedor, String titulo, String mensaje) throws BLException;
}
