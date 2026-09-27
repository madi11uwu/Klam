package pe.edu.pucp.klam.dao.impl.inventario;

import java.sql.SQLException;
import java.util.Map;

interface EquipoEspecificacionDAO {
    void insertEspecificaciones(int idEquipo, Map<String, Object> especificaciones) throws SQLException;
    void deleteEspecificaciones(int idEquipo) throws SQLException;
    Map<String, Object> findByEquipoId(int idEquipo) throws SQLException;
}