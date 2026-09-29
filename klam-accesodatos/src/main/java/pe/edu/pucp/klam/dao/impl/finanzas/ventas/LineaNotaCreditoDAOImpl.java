package pe.edu.pucp.klam.dao.impl.finanzas.ventas;

import pe.edu.pucp.klam.dao.impl.inventario.ConsumibleDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaNotaCredito;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LineaNotaCreditoDAOImpl implements LineaNotaCreditoDAO{
    @Override
    public void insertLineas(int idNotaCredito, List<LineaNotaCredito> lineas) throws SQLException {
        Connection conn = TransactionsManager.getConnection();

        String sql = "{call insertar_linea_nota_credito(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            for (LineaNotaCredito linea : lineas) {
                cmd.setInt("p_id_nota_credito", idNotaCredito);

                if (linea.getConsumible() != null) {
                    cmd.setInt("p_id_consumible", linea.getConsumible().getId_consumible());
                } else {
                    cmd.setNull("p_id_consumible", Types.INTEGER);
                }
                cmd.setString("p_item_referencia", linea.getItemReferencia());
                cmd.setString("p_descripcion", linea.getDescripcion());
                cmd.setInt("p_cantidad", linea.getCantidad());
                cmd.setDouble("p_precio_unitario", linea.getPrecioUnitario());
                cmd.setString("p_motivo_devolucion", linea.getMotivoDevolucion());
                cmd.registerOutParameter("p_id", Types.INTEGER);

                if (cmd.executeUpdate() == 0) {
                    throw new SQLException("No se pudo insertar la linea de nota de credito");
                }

                linea.setIdLinea(cmd.getInt("p_id"));
            }
        }

    }

    @Override
    public void deleteLineas(int idNotaCredito) throws SQLException {
        Connection conn = TransactionsManager.getConnection();

        String sql = "{call eliminar_lineas_por_nota_credito(?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_nota_credito", idNotaCredito);
            cmd.executeUpdate();
        }
    }

    @Override
    public List<LineaNotaCredito> findByNotaCreditoId(int idNotaCredito) throws SQLException {
        String sql = "{call listar_lineas_por_nota_credito(?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_nota_credito", idNotaCredito);
            try (ResultSet rs = cmd.executeQuery()) {
                List<LineaNotaCredito> lineas = new ArrayList<>();
                while (rs.next()) {
                    lineas.add(mapear(rs));
                }
                return lineas;
            }
        }
    }

    private LineaNotaCredito mapear(ResultSet rs) throws SQLException {
        LineaNotaCredito linea = new LineaNotaCredito();
        linea.setIdLinea(rs.getInt("id_linea"));
        linea.setDescripcion(rs.getString("descripcion"));
        linea.setCantidad(rs.getInt("cantidad"));
        linea.setPrecioUnitario(rs.getDouble("precio_unitario"));
        linea.setItemReferencia(rs.getString("item_referencia"));
        linea.setMotivoDevolucion(rs.getString("motivo_devolucion"));

        int idConsumible = rs.getInt("id_consumible");
        if (!rs.wasNull()) {
            linea.setConsumible(new ConsumibleDAOImpl().findById(idConsumible));
        } else {
            linea.setConsumible(null);
        }

        return linea;
    }

}
