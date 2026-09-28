package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.comunicaciones.Notificacion;
import java.sql.SQLException;
import java.util.List;

public interface NotificacionDAO extends DAO<Notificacion, Integer>{
    void marcarLeida(Integer id, boolean leida) throws SQLException;
    List listarPorDestinatario(Integer idAdministrador, Integer idVendedor, Integer idTecnico) throws SQLException;
    List listarNoLeidas() throws SQLException;
}
