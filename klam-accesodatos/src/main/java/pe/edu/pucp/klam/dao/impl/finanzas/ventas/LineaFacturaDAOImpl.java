package pe.edu.pucp.klam.dao.impl.finanzas.ventas;

import pe.edu.pucp.klam.dao.impl.inventario.ConsumibleDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaFactura;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LineaFacturaDAOImpl implements LineaFacturaDAO {
    @Override
    public void insertLineas(int idDocumento, List<LineaFactura> lineas) throws SQLException {

        Connection conn = TransactionsManager.getConnection();

        String sql = "{call insertar_linea_factura(?, ?, ?, ?, ?, ?, ?)}";

        try (CallableStatement cmd = conn.prepareCall(sql)) {
            for (LineaFactura linea : lineas) {
                cmd.setInt("p_id_documento", idDocumento);

                if (linea.getConsumible() != null) {
                    cmd.setInt("p_id_consumible", linea.getConsumible().getId_consumible());
                } else {
                    cmd.setNull("p_id_consumible", Types.INTEGER);
                }

                cmd.setString("p_item_referencia", linea.getItemReferencia());
                cmd.setString("p_descripcion", linea.getDescripcion());
                cmd.setInt("p_cantidad", linea.getCantidad());
                cmd.setDouble("p_precio_unitario", linea.getPrecioUnitario());
                cmd.registerOutParameter("p_id", Types.INTEGER);

                if (cmd.executeUpdate() == 0) {
                    throw new SQLException("No se pudo insertar la linea de factura");
                }

                linea.setIdLinea(cmd.getInt("p_id"));
            }
        }

    }

    @Override
    public void deleteLineas(int idDocumento) throws SQLException {
        Connection conn = TransactionsManager.getConnection();

        String sql = "{call eliminar_lineas_por_factura(?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_documento", idDocumento);
            cmd.executeUpdate();
        }
    }

    @Override
    public List<LineaFactura> findByDocumentoId(int idDocumento) throws SQLException {
        String sql = "{call listar_lineas_por_factura(?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_documento", idDocumento);
            try (ResultSet rs = cmd.executeQuery()) {
                List<LineaFactura> lineas = new ArrayList<>();
                while (rs.next()) {
                    lineas.add(mapear(rs));
                }
                return lineas;
            }
        }
    }

    private LineaFactura mapear(ResultSet rs) throws SQLException {
        LineaFactura linea = new LineaFactura();
        linea.setIdLinea(rs.getInt("id_linea"));
        linea.setDescripcion(rs.getString("descripcion"));
        linea.setCantidad(rs.getInt("cantidad"));
        linea.setPrecioUnitario(rs.getDouble("precio_unitario"));
        linea.setItemReferencia(rs.getString("item_referencia"));

        int idConsumible = rs.getInt("id_consumible");
        if (!rs.wasNull()) {
            linea.setConsumible(new ConsumibleDAOImpl().findById(idConsumible));
        } else {
            linea.setConsumible(null);
        }

        return linea;
    }
}
