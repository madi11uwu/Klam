package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.comunicaciones.Notificacion;
import java.util.List;

public interface NotificacionBL extends BaseBL<Notificacion, Integer> {
    void marcarLeida(Integer id, boolean leida) throws BLException;
    List listarPorDestinatario(Integer idAdministrador, Integer idVendedor, Integer idTecnico) throws BLException;
    List listarNoLeidas() throws BLException;
}
