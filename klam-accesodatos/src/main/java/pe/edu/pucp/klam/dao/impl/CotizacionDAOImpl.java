package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.dao.CotizacionDAO;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.SolicitudCirugia;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Cotizacion;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.EstadoCotizacion;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CotizacionDAOImpl implements CotizacionDAO {
    @Override
    public List<Cotizacion> findAll() throws SQLException {
        String sql = "{call listar_cotizaciones()}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql);
            ResultSet rs = cmd.executeQuery()) {

            List<Cotizacion> cotizaciones = new ArrayList<>();
            while (rs.next()) {
                cotizaciones.add(mapear(rs, new Cotizacion()));
            }
            return cotizaciones;
        }
    }

    @Override
    public Cotizacion findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_cotizacion_por_id(?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Cotizacion()) : null;
            }
        }
    }

    @Override
    public List<Cotizacion> findByCirugia(Integer idCirugia) throws SQLException {
        if (idCirugia == null) {
            throw new IllegalArgumentException("El id de la cirugia no puede ser nulo");
        }

        String sql = "{call listar_cotizaciones_por_cirugia(?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_cirugia", idCirugia);

            try (ResultSet rs = cmd.executeQuery()) {
                List<Cotizacion> cotizaciones = new ArrayList<>();
                while (rs.next()) {
                    cotizaciones.add(mapear(rs, new Cotizacion()));
                }
                return cotizaciones;
            }
        }
    }

    @Override
    public void insert(Cotizacion cotizacion) throws SQLException {
        if (cotizacion == null) {
            throw new IllegalArgumentException("La cotizacion no puede ser nula");
        }

        if (cotizacion.getSolicitud() != null) {
            insertarVersionSolicitud(cotizacion);
            return;
        }
        String sql = "{call insertar_cotizacion(?, ?, ?, ?, ?, ?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_cirugia", cotizacion.getCirugia().getIdCirugia());
            cmd.setDouble("p_precio_pactado", cotizacion.getPrecioPactado());
            cmd.setString("p_estado", cotizacion.getEstado().name());
            cmd.setTimestamp("p_fecha_emision", Timestamp.valueOf(cotizacion.getFechaEmision()));
            cmd.setBoolean("p_activo", cotizacion.isActivo());
            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la cotizacion");
            }

            cotizacion.setIdCotizacion(cmd.getInt("p_id"));
        }
    }

    @Override
    public void update(Cotizacion cotizacion) throws SQLException {
        if (cotizacion == null) {
            throw new IllegalArgumentException("La cotizacion no puede ser nula");
        }

        if (cotizacion.getSolicitud() != null) {
            throw new SQLException("Una versión de cotización de solicitud es inmutable");
        }
        String sql = "{call modificar_cotizacion(?, ?, ?, ?, ?, ?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_cirugia", cotizacion.getCirugia().getIdCirugia());
            cmd.setDouble("p_precio_pactado", cotizacion.getPrecioPactado());
            cmd.setString("p_estado", cotizacion.getEstado().name());
            cmd.setTimestamp("p_fecha_emision", Timestamp.valueOf(cotizacion.getFechaEmision()));
            cmd.setBoolean("p_activo", cotizacion.isActivo());
            cmd.setInt("p_id", cotizacion.getIdCotizacion());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la cotizacion");
            }
        }
    }

    @Override
    public List<Cotizacion> listarPorSolicitud(Integer idSolicitud) throws SQLException {
        if (idSolicitud == null || idSolicitud <= 0) throw new IllegalArgumentException("Solicitud inválida");
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall("{call listar_cotizaciones_por_solicitud(?)}")) {
            cmd.setInt(1, idSolicitud);
            try (ResultSet rs = cmd.executeQuery()) {
                List<Cotizacion> resultado = new ArrayList<>();
                while (rs.next()) resultado.add(mapear(rs, new Cotizacion()));
                return resultado;
            }
        }
    }

    @Override
    public Cotizacion buscarVigentePorSolicitud(Integer idSolicitud) throws SQLException {
        if (idSolicitud == null || idSolicitud <= 0) throw new IllegalArgumentException("Solicitud inválida");
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall("{call buscar_cotizacion_vigente_solicitud(?)}")) {
            cmd.setInt(1, idSolicitud);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Cotizacion()) : null;
            }
        }
    }

    @Override
    public void insertarVersionSolicitud(Cotizacion cotizacion) throws SQLException {
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall("{call insertar_cotizacion_solicitud(?, ?, ?, ?, ?, ?)}")) {
            cmd.setInt(1, cotizacion.getSolicitud().getIdSolicitud());
            cmd.setDouble(2, cotizacion.getPrecioPactado());
            cmd.setTimestamp(3, Timestamp.valueOf(cotizacion.getFechaEmision()));
            cmd.setString(4, cotizacion.getArchivoPath());
            cmd.registerOutParameter(5, Types.INTEGER);
            cmd.registerOutParameter(6, Types.INTEGER);
            cmd.execute();
            cotizacion.setIdCotizacion(cmd.getInt(5));
            cotizacion.setVersion(cmd.getInt(6));
            cotizacion.setEstado(EstadoCotizacion.EMITIDA);
            cotizacion.setVigente(true);
        } finally {
            TransactionsManager.liberar(conn);
        }
    }
    @Override
    public boolean resolverVersionVigente(int id, EstadoCotizacion estado, int idCuentaUsuario,
            String motivo) throws SQLException {
        Connection conn = TransactionsManager.obtenerConexion();
        String sql = "UPDATE cotizacion SET estado = ?, fecha_decision = NOW(), "
                + "id_cuenta_decision = ?, motivo_rechazo = ? "
                + "WHERE id_cotizacion = ? AND id_solicitud IS NOT NULL "
                + "AND vigente = 1 AND activo = 1 AND estado = 'EMITIDA'";
        try (var cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, estado.name());
            cmd.setInt(2, idCuentaUsuario);
            cmd.setString(3, motivo);
            cmd.setInt(4, id);
            return cmd.executeUpdate() == 1;
        } finally {
            TransactionsManager.liberar(conn);
        }
    }
    @Override
    public void actualizarEstado(Integer id, EstadoCotizacion estado) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }

        String sql = "{call actualizar_estado_cotizacion(?, ?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);
            cmd.setString("p_estado", estado.name());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar el estado de la cotizacion");
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call eliminar_cotizacion(?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar la cotizacion");
            }
        }
    }

    protected Cotizacion mapear(ResultSet rs, Cotizacion cotizacion) throws SQLException {
        cotizacion.setIdCotizacion(rs.getInt("id_cotizacion"));
        cotizacion.setPrecioPactado(rs.getDouble("precio_pactado"));
        cotizacion.setEstado(Enum.valueOf(EstadoCotizacion.class, rs.getString("estado")));
        cotizacion.setFechaEmision(rs.getTimestamp("fecha_emision").toLocalDateTime());
        cotizacion.setActivo(rs.getBoolean("activo"));
        int idCirugia = rs.getInt("id_cirugia");
        if (!rs.wasNull()) cotizacion.setCirugia(new CirugiaDAOImpl().findById(idCirugia));
        int idSolicitud = rs.getInt("id_solicitud");
        if (!rs.wasNull()) {
            SolicitudCirugia solicitud = new SolicitudCirugia();
            solicitud.setIdSolicitud(idSolicitud);
            cotizacion.setSolicitud(solicitud);
        }
        cotizacion.setVersion(rs.getInt("version"));
        cotizacion.setVigente(rs.getBoolean("vigente"));
        cotizacion.setArchivoPath(rs.getString("archivo_path"));
        Timestamp fechaDecision = rs.getTimestamp("fecha_decision");
        if (fechaDecision != null) cotizacion.setFechaDecision(fechaDecision.toLocalDateTime());
        int idCuentaDecision = rs.getInt("id_cuenta_decision");
        if (!rs.wasNull()) cotizacion.setIdCuentaDecision(idCuentaDecision);
        cotizacion.setMotivoRechazo(rs.getString("motivo_rechazo"));
        return cotizacion;
    }
}
