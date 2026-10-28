package pe.edu.pucp.klam.dao.impl;

import java.sql.SQLException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

/**
 * Usa la conexion de la transaccion activa si existe; si no, una propia.
 */
public class DocumentoIngresoDAOImpl implements DocumentoIngresoDAO {
    private final OrdenCompraDAO ordenCompraDAO = new OrdenCompraDAOImpl();

    @Override
    public void insert(DocumentoIngreso doc) throws SQLException {
        if (doc == null) {
            throw new IllegalArgumentException("El documento no puede ser nulo");
        }
        String sql = "{CALL insertar_documento_ingreso(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.registerOutParameter("p_id", Types.INTEGER);
            asignarDatos(cs, doc);

            if (cs.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar el documento de ingreso");
            }
            doc.setIdDocumento(cs.getInt("p_id"));
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public void update(DocumentoIngreso doc) throws SQLException {
        if (doc == null) {
            throw new IllegalArgumentException("El documento no puede ser nulo");
        }
        String sql = "{CALL modificar_documento_ingreso(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt("p_id", doc.getIdDocumento());
            asignarDatos(cs, doc);

            if (cs.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar el documento de ingreso " + doc.getIdDocumento());
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    // Parametros comunes de insertar_documento_ingreso y modificar_documento_ingreso
    private void asignarDatos(CallableStatement cs, DocumentoIngreso doc) throws SQLException {
        cs.setString("p_tipo_documento", doc.getTipoDocumento().name());
        cs.setString("p_archivo_path", doc.getArchivoPath());
        cs.setString("p_estado_validacion", doc.getEstadoValidacion().name());
        cs.setTimestamp("p_fecha_carga", Timestamp.valueOf(doc.getFechaCarga()));
        DBManager.setFechaONula(cs, "p_fecha_validacion", doc.getFechaValidacion());
        DBManager.setStringONulo(cs, "p_motivo_rechazo", doc.getMotivoRechazo());
        cs.setInt("p_version", doc.getVersion());
        DBManager.setStringONulo(cs, "p_responsable", doc.getResponsable());
        DBManager.setIntONulo(cs, "p_id_solicitud", doc.getIdSolicitud());
        DBManager.setIntONulo(cs, "p_id_cirugia", doc.getIdCirugia());

        OrdenCompra orden = doc.getOrdenCompra();
        DBManager.setIntONulo(cs, "p_id_orden_compra",
                (orden != null && orden.getIdOrdenCompra() > 0) ? orden.getIdOrdenCompra() : null);

        setUsuarioParams(cs, doc.getUsuarioCarga());
        cs.setBoolean("p_activo", doc.isActivo());
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cs = conn.prepareCall("{CALL eliminar_documento_ingreso(?)}")) {
            cs.setInt("p_id", id);

            if (cs.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar el documento de ingreso " + id);
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public DocumentoIngreso findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cs = conn.prepareCall("{CALL buscar_documento_ingreso_por_id(?)}")) {
            cs.setInt("p_id", id);
            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public List<DocumentoIngreso> findAll() throws SQLException {
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cs = conn.prepareCall("{CALL listar_documentos_ingreso()}");
             ResultSet rs = cs.executeQuery()) {
            return mapearLista(rs);
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public List<DocumentoIngreso> listarPorSolicitud(int idSolicitud) throws SQLException {
        return listarPorOrigen(idSolicitud, null, false);
    }

    @Override
    public List<DocumentoIngreso> listarPorCirugia(int idCirugia) throws SQLException {
        return listarPorOrigen(null, idCirugia, false);
    }

    @Override
    public List<DocumentoIngreso> listarVigentesPorSolicitud(int idSolicitud) throws SQLException {
        return listarPorOrigen(idSolicitud, null, true);
    }

    @Override
    public List<DocumentoIngreso> listarVigentesPorCirugia(int idCirugia) throws SQLException {
        return listarPorOrigen(null, idCirugia, true);
    }

    private List<DocumentoIngreso> listarPorOrigen(Integer idSolicitud, Integer idCirugia,
                                                   boolean soloVigentes) throws SQLException {
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cs = prepararListadoOrigen(conn, idSolicitud, idCirugia, soloVigentes);
             ResultSet rs = cs.executeQuery()) {
            return mapearLista(rs);
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    // Unica llamada a listar_documentos_origen: la comparten los listados y el historial
    private CallableStatement prepararListadoOrigen(Connection conn, Integer idSolicitud, Integer idCirugia,
                                                    boolean soloVigentes) throws SQLException {
        CallableStatement cs = conn.prepareCall("{CALL listar_documentos_origen(?, ?, ?)}");
        DBManager.setIntONulo(cs, "p_id_solicitud", idSolicitud);
        DBManager.setIntONulo(cs, "p_id_cirugia", idCirugia);
        cs.setBoolean("p_solo_vigentes", soloVigentes);
        return cs;
    }

    @Override
    public int siguienteVersion(Integer idSolicitud, Integer idCirugia,
                                TipoDocumentoIngreso tipo) throws SQLException {
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cs = conn.prepareCall("{CALL siguiente_version_documento(?, ?, ?, ?)}")) {
            DBManager.setIntONulo(cs, "p_id_solicitud", idSolicitud);
            DBManager.setIntONulo(cs, "p_id_cirugia", idCirugia);
            cs.setString("p_tipo_documento", tipo.name());
            cs.registerOutParameter("p_version", Types.INTEGER);
            cs.execute();
            return cs.getInt("p_version");
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public void actualizarEstadoValidacion(int idDocumento, String estado, LocalDateTime fechaValidacion,
                                           String motivoRechazo, String responsable) throws SQLException {
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cs = conn.prepareCall(
                "{CALL actualizar_estado_validacion_documento(?, ?, ?, ?, ?)}")) {
            cs.setInt("p_id", idDocumento);
            cs.setString("p_estado_validacion", estado);
            DBManager.setFechaONula(cs, "p_fecha_validacion", fechaValidacion);
            DBManager.setStringONulo(cs, "p_motivo_rechazo", motivoRechazo);
            DBManager.setStringONulo(cs, "p_responsable", responsable);

            if (cs.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar el estado del documento " + idDocumento);
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public List<Map<String, Object>> historialDocumental(Integer idSolicitud, Integer idCirugia) throws SQLException {
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cs = prepararListadoOrigen(conn, idSolicitud, idCirugia, false);
             ResultSet rs = cs.executeQuery()) {
            List<Map<String, Object>> historial = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("idDocumento", rs.getInt("id_documento"));
                fila.put("fechaHora", rs.getTimestamp("fecha_carga").toLocalDateTime());
                fila.put("documento", rs.getString("tipo_documento"));
                fila.put("version", rs.getInt("version"));
                fila.put("responsable", rs.getString("responsable"));
                fila.put("estado", rs.getString("estado_validacion"));
                fila.put("motivoRechazo", rs.getString("motivo_rechazo"));
                Timestamp validacion = rs.getTimestamp("fecha_validacion");
                fila.put("fechaValidacion", validacion != null ? validacion.toLocalDateTime() : null);
                fila.put("referencia", rs.getString("archivo_path"));
                historial.add(fila);
            }
            return historial;
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    private List<DocumentoIngreso> mapearLista(ResultSet rs) throws SQLException {
        List<DocumentoIngreso> lista = new ArrayList<>();
        while (rs.next()) {
            lista.add(mapear(rs));
        }
        return lista;
    }

    private DocumentoIngreso mapear(ResultSet rs) throws SQLException {
        DocumentoIngreso doc = new DocumentoIngreso();
        doc.setIdDocumento(rs.getInt("id_documento"));
        doc.setTipoDocumento(TipoDocumentoIngreso.valueOf(rs.getString("tipo_documento")));
        doc.setArchivoPath(rs.getString("archivo_path"));
        doc.setEstadoValidacion(EstadoValidacionDocumento.valueOf(rs.getString("estado_validacion")));
        doc.setFechaCarga(rs.getTimestamp("fecha_carga").toLocalDateTime());

        Timestamp validacion = rs.getTimestamp("fecha_validacion");
        doc.setFechaValidacion(validacion != null ? validacion.toLocalDateTime() : null);
        doc.setMotivoRechazo(rs.getString("motivo_rechazo"));
        doc.setVersion(rs.getInt("version"));
        doc.setResponsable(rs.getString("responsable"));

        int idSolicitud = rs.getInt("id_solicitud");
        doc.setIdSolicitud(rs.wasNull() ? null : idSolicitud);
        int idCirugia = rs.getInt("id_cirugia");
        doc.setIdCirugia(rs.wasNull() ? null : idCirugia);

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

    // Concrete table inheritance: el usuario puede estar en una de 3 columnas (o en ninguna)
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

    private void setUsuarioParams(CallableStatement cs, UsuarioPlataforma usuario) throws SQLException {
        DBManager.setIntONulo(cs, "p_id_administrador",
                usuario instanceof Administrador ? usuario.getIdUsuario() : null);
        DBManager.setIntONulo(cs, "p_id_vendedor",
                usuario instanceof Vendedor ? usuario.getIdUsuario() : null);
        DBManager.setIntONulo(cs, "p_id_tecnico",
                usuario instanceof TecnicoInstrumentista ? usuario.getIdUsuario() : null);
    }
}
