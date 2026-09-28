package pe.edu.pucp.klam.dao.impl.inventario;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;

import java.sql.SQLException;
import java.util.Map;

interface BandejaConsumibleDAO {
    void insertConsumibles(int idBandeja, Map<Consumible, Integer> despachados,
                           Map<Consumible, Integer> consumidos) throws SQLException;
    void deleteConsumibles(int idBandeja) throws SQLException;
    Map<Consumible, Integer> findDespachadosByBandejaId(int idBandeja) throws SQLException;
    Map<Consumible, Integer> findConsumidosByBandejaId(int idBandeja) throws SQLException;
}