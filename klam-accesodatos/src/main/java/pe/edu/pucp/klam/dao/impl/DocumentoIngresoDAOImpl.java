package pe.edu.pucp.klam.dao.impl;

import java.sql.SQLException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import pe.edu.pucp.klam.dao.DocumentoIngresoDAO;
import pe.edu.pucp.klam.dao.OrdenCompraDAO;
import pe.edu.pucp.klam.dao.impl.compras.OrdenCompraDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.EstadoValidacionDocumento;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.OrdenCompra;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.TipoDocumentoIngreso;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Administrador;
import pe.edu.pucp.klam.modelo.usuariosPermisos.TecnicoInstrumentista;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;

public class DocumentoIngresoDAOImpl implements DocumentoIngresoDAO {
    private final OrdenCompraDAO ordenCompraDAO = new OrdenCompraDAOImpl();
    @Override
   public void insert (DocumentoIngreso doc) throws SQLException {
        Connection conn = conexionTransaccional();
        boolean localConn = (conn == null);
        if (localConn) {
            conn = DBManager.getInstance().getConnection();
        }

        try {
            String sql = "{CALL INSERTAR_DOCUMENTO_INGRESO(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.registerOutParameter(1, Types.INTEGER);
                cs.setString(2, doc.getTipoDocumento() != null ? doc.getTipoDocumento().name() : null);
                cs.setString(3, doc.getArchivoPath());
                cs.setString(4, doc.getEstadoValidacion() != null ? doc.getEstadoValidacion().name() : null);
                
                if (doc.getFechaCarga() != null) {
                    cs.setTimestamp(5, java.sql.Timestamp.valueOf(doc.getFechaCarga()));
                } else {
                    cs.setNull(5, Types.TIMESTAMP);
                }

                if (doc.getOrdenCompra() != null && doc.getOrdenCompra().getIdOrdenCompra() > 0) {
                    cs.setInt(6, doc.getOrdenCompra().getIdOrdenCompra());
                } else {
                    cs.setNull(6, Types.INTEGER);
                }

                setUsuarioParams(cs, doc.getUsuarioCarga(), 7, 8, 9);
                cs.setBoolean(10, doc.isActivo());

                cs.executeUpdate();
                int id = cs.getInt(1);
                doc.setId_documento(id);
            }
        } finally {
            if (localConn && conn != null) {
                conn.close();
            }
        }
    }

    @Override
    public void update(DocumentoIngreso doc) throws SQLException {
        Connection conn = conexionTransaccional();
        boolean localConn = (conn == null);
        if (localConn) conn = DBManager.getInstance().getConnection();

        try {
            String sql = "{CALL MODIFICAR_DOCUMENTO_INGRESO(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setInt(1, doc.getId_documento());
                cs.setString(2, doc.getTipoDocumento() != null ? doc.getTipoDocumento().name() : null);
                cs.setString(3, doc.getArchivoPath());
                cs.setString(4, doc.getEstadoValidacion() != null ? doc.getEstadoValidacion().name() : null);
                
                if (doc.getFechaCarga() != null) {
                    cs.setTimestamp(5, java.sql.Timestamp.valueOf(doc.getFechaCarga()));
                } else {
                    cs.setNull(5, Types.TIMESTAMP);
                }

                if (doc.getOrdenCompra() != null && doc.getOrdenCompra().getIdOrdenCompra() > 0) {
                    cs.setInt(6, doc.getOrdenCompra().getIdOrdenCompra());
                } else {
                    cs.setNull(6, Types.INTEGER);
                }

                setUsuarioParams(cs, doc.getUsuarioCarga(), 7, 8, 9);
                cs.setBoolean(10, doc.isActivo());

                cs.executeUpdate();
            }
        } finally {
            if (localConn && conn != null) conn.close();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException{
        Connection conn = conexionTransaccional();
        boolean localConn = (conn == null);
        if (localConn) conn = DBManager.getInstance().getConnection();

        try {
            String sql = "{CALL ELIMINAR_DOCUMENTO_INGRESO(?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setInt(1, id);
                cs.executeUpdate();
            }
        } finally {
            if (localConn && conn != null) conn.close();
        }
    }

    @Override
    public DocumentoIngreso findById(Integer id) throws SQLException {
        Connection conn = conexionTransaccional();
        boolean localConn = (conn == null);
        if (localConn) conn = DBManager.getInstance().getConnection();

        try {
            String sql = "{CALL BUSCAR_DOCUMENTO_INGRESO_POR_ID(?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setInt(1, id);
                try (ResultSet rs = cs.executeQuery()) {
                    if (rs.next()) {
                        return mapear(rs);
                    }
                }
            }
            return null;
        } finally {
            if (localConn && conn != null) conn.close();
        }
    }

    @Override
    public List<DocumentoIngreso> findAll() throws SQLException {
        List<DocumentoIngreso> lista = new ArrayList<>();
        Connection conn = conexionTransaccional();
        boolean localConn = (conn == null);
        if (localConn) conn = DBManager.getInstance().getConnection();

        try {
            String sql = "{CALL LISTAR_DOCUMENTOS_INGRESO()}";
            try (CallableStatement cs = conn.prepareCall(sql);
                 ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } finally {
            if (localConn && conn != null) conn.close();
        }
    }

    private DocumentoIngreso mapear(ResultSet rs) throws SQLException {
        DocumentoIngreso doc = new DocumentoIngreso();
        doc.setId_documento(rs.getInt("id_documento"));
        
        String tipoStr = rs.getString("tipo_documento");
        if (tipoStr != null) doc.setTipoDocumento(TipoDocumentoIngreso.valueOf(tipoStr));
        
        doc.setArchivoPath(rs.getString("archivo_path"));
        
        String estadoStr = rs.getString("estado_validacion");
        if (estadoStr != null) doc.setEstadoValidacion(EstadoValidacionDocumento.valueOf(estadoStr));
        
        if (rs.getTimestamp("fecha_carga") != null) {
            doc.setFechaCarga(rs.getTimestamp("fecha_carga").toLocalDateTime());
        }

        int idOrden = rs.getInt("id_orden_compra");
        if (!rs.wasNull()) {
            OrdenCompra oc = ordenCompraDAO.findById(idOrden);
            if (oc != null) {
                doc.setOrdenCompra(oc);
            }
        }
        
        doc.setUsuarioCarga(mapearUsuario(rs));
        doc.setActivo(rs.getBoolean("activo"));
        return doc;
    }

    // Concrete table inheritance: el usuario puede estar en una de 3 columnas
    private UsuarioPlataforma mapearUsuario(ResultSet rs) throws SQLException {
        UsuarioPlataforma usuario = null;

        int idAdmin = rs.getInt("id_administrador");
        if (!rs.wasNull()) {
            usuario = new Administrador();
            usuario.setIdUsuario(idAdmin);
        } else {
            int idVend = rs.getInt("id_vendedor");
            if (!rs.wasNull()) {
                usuario = new Vendedor();
                usuario.setIdUsuario(idVend);
            } else {
                int idTec = rs.getInt("id_tecnico");
                if (!rs.wasNull()) {
                    usuario = new TecnicoInstrumentista();
                    usuario.setIdUsuario(idTec);
                }
            }
        }
        return usuario;
    }

    private void setUsuarioParams(CallableStatement cs, UsuarioPlataforma usuario,
                                  int idxAdmin, int idxVend, int idxTec) throws SQLException {
        cs.setNull(idxAdmin, Types.INTEGER);
        cs.setNull(idxVend, Types.INTEGER);
        cs.setNull(idxTec, Types.INTEGER);

        if (usuario instanceof Administrador) {
            cs.setInt(idxAdmin, usuario.getIdUsuario());
        } else if (usuario instanceof Vendedor) {
            cs.setInt(idxVend, usuario.getIdUsuario());
        } else if (usuario instanceof TecnicoInstrumentista) {
            cs.setInt(idxTec, usuario.getIdUsuario());
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
}