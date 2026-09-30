package pe.edu.pucp.klam.dao.impl.inventario;

import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class BandejaConsumibleDAOImpl implements BandejaConsumibleDAO {

    @Override
    public void insertConsumibles(int idBandeja, Map<Consumible, Integer> despachados,
                                  Map<Consumible, Integer> consumidos) throws SQLException {
        Connection conn = TransactionsManager.getConnection();

        Set<Consumible> todos = new HashSet<>(despachados.keySet());
        todos.addAll(consumidos.keySet());

        String sql = "{call insertar_consumible_bandeja(?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            for (Consumible c : todos) {
                cmd.setInt("p_id_bandeja", idBandeja);
                cmd.setInt("p_id_consumible", c.getIdConsumible());
                cmd.setInt("p_cantidad_despachada", despachados.getOrDefault(c, 0));
                cmd.setInt("p_cantidad_consumida", consumidos.getOrDefault(c, 0));

                if (cmd.executeUpdate() == 0) {
                    throw new SQLException("No se pudo insertar el consumible " + c.getIdConsumible());
                }
            }
        }
    }

    @Override
    public void deleteConsumibles(int idBandeja) throws SQLException {
        Connection conn = TransactionsManager.getConnection();

        String sql = "{call eliminar_consumibles_bandeja(?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_bandeja", idBandeja);
            cmd.executeUpdate();
        }
    }

    @Override
    public Map<Consumible, Integer> findDespachadosByBandejaId(int idBandeja) throws SQLException {
        return listarPorColumna(idBandeja, "cantidad_despachada");
    }

    @Override
    public Map<Consumible, Integer> findConsumidosByBandejaId(int idBandeja) throws SQLException {
        return listarPorColumna(idBandeja, "cantidad_consumida");
    }

    // Lee el detalle y arma el Map con la columna de cantidad pedida
    private Map<Consumible, Integer> listarPorColumna(int idBandeja, String columna) throws SQLException {
        String sql = "{call listar_consumibles_bandeja(?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_bandeja", idBandeja);
            try (ResultSet rs = cmd.executeQuery()) {
                Map<Consumible, Integer> resultado = new HashMap<>();
                while (rs.next()) {
                    int cantidad = rs.getInt(columna);
                    if (cantidad > 0) {
                        resultado.put(mapearConsumible(rs), cantidad);
                    }
                }
                return resultado;
            }
        }
    }

    private Consumible mapearConsumible(ResultSet rs) throws SQLException {
        Consumible c = new Consumible();
        c.setIdConsumible(rs.getInt("id_consumible"));
        c.setNombreComercial(rs.getString("nombre_comercial"));
        c.setMarca(rs.getString("marca"));
        c.setMedida(rs.getString("medida"));
        c.setActivo(rs.getBoolean("activo"));
        return c;
    }
}