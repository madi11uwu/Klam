package pe.edu.pucp.klam.dao.impl.inventario;

import pe.edu.pucp.klam.dao.impl.inventario.EquipoEspecificacionDAO;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.db.DBManager;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

class EquipoEspecificacionDAOImpl implements EquipoEspecificacionDAO {

    @Override
    public void insertEspecificaciones(int idEquipo, Map<String, Object> especificaciones) throws SQLException {
        Connection conn = TransactionsManager.getConnection();

        String sql = "{call insertar_especificacion_equipo(?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            for (Map.Entry<String, Object> esp : especificaciones.entrySet()) {
                cmd.setInt("p_id_equipo", idEquipo);
                cmd.setString("p_clave", esp.getKey());
                cmd.setString("p_valor", String.valueOf(esp.getValue()));

                if (cmd.executeUpdate() == 0) {
                    throw new SQLException("No se pudo insertar la especificacion " + esp.getKey());
                }
            }
        }
    }

    @Override
    public void deleteEspecificaciones(int idEquipo) throws SQLException {
        Connection conn = TransactionsManager.getConnection();

        String sql = "{call eliminar_especificaciones_equipo(?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_equipo", idEquipo);
            cmd.executeUpdate();
        }
    }

    @Override
    public Map<String, Object> findByEquipoId(int idEquipo) throws SQLException {
        String sql = "{call listar_especificaciones_equipo(?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_equipo", idEquipo);
            try (ResultSet rs = cmd.executeQuery()) {
                Map<String, Object> especificaciones = new HashMap<>();
                while (rs.next()) {
                    especificaciones.put(rs.getString("clave"), rs.getString("valor"));
                }
                return especificaciones;
            }
        }
    }
}