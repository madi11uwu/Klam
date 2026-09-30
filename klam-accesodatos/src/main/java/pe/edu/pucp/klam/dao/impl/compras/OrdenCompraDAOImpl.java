package pe.edu.pucp.klam.dao.impl.compras;

import java.sql.SQLException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import pe.edu.pucp.klam.dao.OrdenCompraDAO;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.LineaOrdenCompra;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.OrdenCompra;

public class OrdenCompraDAOImpl implements OrdenCompraDAO {

    private final LineaOrdenCompraDAO lineaDAO = new LineaOrdenCompraDAOImpl();

    @Override
    public void insert(OrdenCompra orden) throws SQLException{
        Connection conn = conexionTransaccional();
        boolean localConn = (conn == null);
        if (localConn) conn = DBManager.getInstance().getConnection();

        try {
            String sql = "{CALL INSERTAR_ORDEN_COMPRA(?, ?, ?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.registerOutParameter(1, Types.INTEGER);
                cs.setString(2, orden.getArchivoRespaldoPath());
                
                if (orden.getFechaRecepcion() != null) {
                    cs.setTimestamp(3, java.sql.Timestamp.valueOf(orden.getFechaRecepcion()));
                } else {
                    cs.setNull(3, Types.TIMESTAMP);
                }
                cs.setBoolean(4, orden.isActivo());

                cs.executeUpdate();

                int idOrden = cs.getInt(1);
                orden.setIdOrdenCompra(idOrden);

                if (orden.getLineasOrdenCompra() != null) {
                    for (LineaOrdenCompra linea : orden.getLineasOrdenCompra()) {
                        lineaDAO.insertar(linea, idOrden, conn);
                    }
                }
            }
        } finally {
            if (localConn && conn != null) conn.close();
        }
    }

    @Override
    public void update(OrdenCompra orden) throws SQLException {
        Connection conn = conexionTransaccional();
        boolean localConn = (conn == null);
        if (localConn) conn = DBManager.getInstance().getConnection();

        try {
            String sql = "{CALL MODIFICAR_ORDEN_COMPRA(?, ?, ?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                int idOrden = orden.getIdOrdenCompra();
                cs.setInt(1, idOrden);
                cs.setString(2, orden.getArchivoRespaldoPath());
                
                if (orden.getFechaRecepcion() != null) {
                    cs.setTimestamp(3, java.sql.Timestamp.valueOf(orden.getFechaRecepcion()));
                } else {
                    cs.setNull(3, Types.TIMESTAMP);
                }
                cs.setBoolean(4, orden.isActivo());

                cs.executeUpdate();

                lineaDAO.eliminarPorOrdenCompra(idOrden, conn);
                if (orden.getLineasOrdenCompra() != null) {
                    for (LineaOrdenCompra linea : orden.getLineasOrdenCompra()) {
                        lineaDAO.insertar(linea, idOrden, conn);
                    }
                }
            }
        } finally {
            if (localConn && conn != null) conn.close();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        Connection conn = conexionTransaccional();
        boolean localConn = (conn == null);
        if (localConn) conn = DBManager.getInstance().getConnection();

        try {
            String sql = "{CALL ELIMINAR_ORDEN_COMPRA(?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setInt(1, id);
                cs.executeUpdate();
            }
        } finally {
            if (localConn && conn != null) conn.close();
        }
    }

    @Override
    public OrdenCompra findById(Integer id) throws SQLException{
        Connection conn = conexionTransaccional();
        boolean localConn = (conn == null);
        if (localConn) conn = DBManager.getInstance().getConnection();

        try {
            String sql = "{CALL BUSCAR_ORDEN_COMPRA_POR_ID(?)}";
            OrdenCompra orden = null;
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setInt(1, id);
                try (ResultSet rs = cs.executeQuery()) {
                    if (rs.next()) {
                        orden = mapearCabecera(rs);
                    }
                }
            }
            if (orden != null) {
                orden.setLineasOrdenCompra(lineaDAO.listarPorOrdenCompra(id, conn));
            }
            return orden;
        } finally {
            if (localConn && conn != null) conn.close();
        }
    }

    @Override
    public List<OrdenCompra> findAll() throws SQLException {
        List<OrdenCompra> lista = new ArrayList<>();
        Connection conn = conexionTransaccional();
        boolean localConn = (conn == null);
        if (localConn) conn = DBManager.getInstance().getConnection();

        try {
            String sql = "{CALL LISTAR_ORDENES_COMPRA()}";
            try (CallableStatement cs = conn.prepareCall(sql);
                 ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCabecera(rs));
                }
            }
            for (OrdenCompra oc : lista) {
                int idOrden = oc.getIdOrdenCompra();
                oc.setLineasOrdenCompra(lineaDAO.listarPorOrdenCompra(idOrden, conn));
            }
            return lista;
        } finally {
            if (localConn && conn != null) conn.close();
        }
    }

    // TransactionsManager.getConnection() lanza excepcion si no hay transaccion activa
    private Connection conexionTransaccional() {
        try {
            return TransactionsManager.getConnection();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private OrdenCompra mapearCabecera(ResultSet rs) throws SQLException {
        OrdenCompra oc = new OrdenCompra();
        oc.setIdOrdenCompra(rs.getInt("id_orden_compra"));
        oc.setArchivoRespaldoPath(rs.getString("archivo_respaldo_path"));
        if (rs.getTimestamp("fecha_recepcion") != null) {
            oc.setFechaRecepcion(rs.getTimestamp("fecha_recepcion").toLocalDateTime());
        }
        oc.setActivo(rs.getBoolean("activo"));
        return oc;
    }
}