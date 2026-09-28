package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;
import java.sql.SQLException;
import java.util.List;

public interface ConsumibleDAO extends DAO<Consumible, Integer> {
    List<Consumible> findByNombre(String nombre) throws SQLException;
}