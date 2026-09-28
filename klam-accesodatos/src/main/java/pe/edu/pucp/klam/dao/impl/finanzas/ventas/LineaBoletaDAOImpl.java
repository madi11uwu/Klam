package pe.edu.pucp.klam.dao.impl.finanzas.ventas;

import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaBoleta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LineaBoletaDAOImpl implements LineaBoletaDAO{
    @Override
    public void insertLineas(int idDocumento, List<LineaBoleta> lineas) throws SQLException {
        Connection conn = TransactionsManager.getConnection();

        String sql = "{call insertar_linea_boleta(?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            for (LineaBoleta linea : lineas) {
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
                    throw new SQLException("No se pudo insertar la linea de boleta");
                }

                linea.setIdLinea(cmd.getInt("p_id"));
            }
        }
    }

    @Override
    public void deleteLineas(int idDocumento) throws SQLException {
        Connection conn = TransactionsManager.getConnection();

        String sql = "{call eliminar_lineas_por_boleta(?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_documento", idDocumento);
            cmd.executeUpdate();
        }
    }

    @Override
    public List<LineaBoleta> findByDocumentoId(int idDocumento) throws SQLException {
        String sql = "{call listar_lineas_por_boleta(?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_documento", idDocumento);
            try (ResultSet rs = cmd.executeQuery()) {
                List<LineaBoleta> lineas = new ArrayList<>();
                while (rs.next()) {
                    lineas.add(mapear(rs));
                }
                return lineas;
            }
        }
    }

    private LineaBoleta mapear(ResultSet rs) throws SQLException {
        LineaBoleta linea = new LineaBoleta();
        linea.setIdLinea(rs.getInt("id_linea"));
        linea.setDescripcion(rs.getString("descripcion"));
        linea.setCantidad(rs.getInt("cantidad"));
        linea.setPrecioUnitario(rs.getDouble("precio_unitario"));
        linea.setItemReferencia(rs.getString("item_referencia"));

        int idConsumible = rs.getInt("id_consumible");
        if (!rs.wasNull()) {
            //Consumible DAO
            //linea.setConsumible(new ConsumibleDAOImpl().findById(idConsumible));
        } else {
            linea.setConsumible(null);
        }

        return linea;
    }

}
