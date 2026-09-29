package pe.edu.pucp.klam.dao.impl.compras;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.LineaOrdenCompra;

public class LineaOrdenCompraDAOImpl implements LineaOrdenCompraDAO {

    @Override
    public Integer insertar(LineaOrdenCompra linea, Integer idOrdenCompra, Connection conn) throws SQLException {
        String sql = "{CALL INSERTAR_LINEA_ORDEN_COMPRA(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.registerOutParameter(1, Types.INTEGER);
            cs.setInt(2, linea.getCantidad());
            cs.setDouble(3, linea.getPrecioUnitario());
            
            // Se usa calcularSubtotal() definido en LineaDocumento
            cs.setDouble(4, linea.calcularSubtotal());
            
            cs.setString(5, linea.getDescripcion());
            cs.setString(6, linea.getItemReferencia());
            
            if (linea.getConsumible() != null && linea.getConsumible().getId_consumible()<0) {
                cs.setInt(7, linea.getConsumible(). getId_consumible());
            } else {
                cs.setNull(7, Types.INTEGER);
            }
            
            cs.setInt(8, idOrdenCompra);
            cs.executeUpdate();
            
            int id = cs.getInt(1);
            // Se usa setIdLinea(int) definido en LineaDocumento
            linea.setIdLinea(id);
            return id;
        }
    }

    @Override
    public void eliminarPorOrdenCompra(Integer idOrdenCompra, Connection conn) throws SQLException {
        String sql = "{CALL ELIMINAR_LINEAS_POR_ORDEN_COMPRA(?)}";
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, idOrdenCompra);
            cs.executeUpdate();
        }
    }

    @Override
    public List<LineaOrdenCompra> listarPorOrdenCompra(Integer idOrdenCompra, Connection conn) throws SQLException {
        List<LineaOrdenCompra> lineas = new ArrayList<>();
        String sql = "{CALL LISTAR_LINEAS_POR_ORDEN_COMPRA(?)}";
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, idOrdenCompra);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    // Creamos el objeto consumible si viene en el ResultSet
                    Consumible c = null;
                    int idConsumible = rs.getInt("fid_consumible");
                    if (!rs.wasNull()) {
                        c = new Consumible();
                        c.setId_consumible(idConsumible);
                    }

                    // Se asignan los atributos con sus métodos reales
                    LineaOrdenCompra linea = new LineaOrdenCompra();
                    linea.setIdLinea(rs.getInt("id_linea_orden_compra"));
                    linea.setCantidad(rs.getInt("cantidad"));
                    linea.setPrecioUnitario(rs.getDouble("precio_unitario"));
                    linea.setDescripcion(rs.getString("descripcion"));
                    linea.setItemReferencia(rs.getString("item_referencia"));
                    linea.setConsumible(c);
                    
                    lineas.add(linea);
                }
            }
        }
        return lineas;
    }
}