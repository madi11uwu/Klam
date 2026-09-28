package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.dao.NotificacionDAO;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.comunicaciones.Notificacion;
import pe.edu.pucp.klam.modelo.comunicaciones.TipoNotificacion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificacionDAOImpl implements NotificacionDAO {

    @Override
    public List findAll() throws SQLException {
        String sql = "{call listar_notificaciones()}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {

            List notificaciones = new ArrayList<>();
            while (rs.next()) {
                notificaciones.add(mapear(rs));
            }
            return notificaciones;
        }
    }

    @Override
    public Notificacion findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        String sql = "{call buscar_notificacion_por_id(?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_notificacion", id);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void insert(Notificacion notificacion) throws SQLException {
        if (notificacion == null) {
            throw new IllegalArgumentException("La notificación no puede ser nula");
        }

        String sql = "{call insertar_notificacion(?, ?, ?, ?, ?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setTimestamp("p_fecha_hora", Timestamp.valueOf(notificacion.getFechaHora()));
            cmd.setString("p_titulo", notificacion.getTitulo());
            cmd.setBoolean("p_estado_leida", notificacion.isEstado_leida());
            cmd.setString("p_tipo_notificacion", notificacion.getTipo_notificacion().name());

            cmd.registerOutParameter("p_id_notificacion", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la notificación");
            }

            notificacion.setId_notifacion(cmd.getInt("p_id_notificacion"));
        }
    }

    @Override
    public void update(Notificacion notificacion) throws SQLException {
        if (notificacion == null) {
            throw new IllegalArgumentException("La notificación no puede ser nula");
        }

        String sql = "{call modificar_notificacion(?, ?, ?, ?, ?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_notificacion", notificacion.getId_notifacion());
            cmd.setTimestamp("p_fecha_hora", Timestamp.valueOf(notificacion.getFechaHora()));
            cmd.setString("p_titulo", notificacion.getTitulo());
            cmd.setBoolean("p_estado_leida", notificacion.isEstado_leida());
            cmd.setString("p_tipo_notificacion", notificacion.getTipo_notificacion().name());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la notificación");
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call eliminar_notificacion(?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_notificacion", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar la notificación");
            }
        }
    }

    private Notificacion mapear(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_notificacion");

        Timestamp ts = rs.getTimestamp("fecha_hora");
        LocalDateTime fechaHora = (ts != null) ? ts.toLocalDateTime() : null;

        String titulo = rs.getString("titulo");
        boolean estadoLeida = rs.getBoolean("estado_leida");

        TipoNotificacion tipo = Enum.valueOf(TipoNotificacion.class, rs.getString("tipo_notificacion"));

        return new Notificacion(id, fechaHora, titulo, estadoLeida, tipo);
    }

    @Override
    public void marcarLeida(Integer id, boolean leida) throws SQLException {
        String sql = "{call marcar_notificacion_leida(?, ?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);
            cmd.setBoolean("p_estado_leida", leida);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo marcar la notificación como leída.");
            }
        }
    }

    @Override
    public List listarPorDestinatario(Integer idAdministrador, Integer idVendedor, Integer idTecnico) throws SQLException {
        String sql = "{call listar_notificaciones_por_destinatario(?, ?, ?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            // Manejo de nulos según el SP
            if (idAdministrador != null) cmd.setInt("p_id_administrador", idAdministrador);
            else cmd.setNull("p_id_administrador", Types.INTEGER);

            if (idVendedor != null) cmd.setInt("p_id_vendedor", idVendedor);
            else cmd.setNull("p_id_vendedor", Types.INTEGER);

            if (idTecnico != null) cmd.setInt("p_id_tecnico", idTecnico);
            else cmd.setNull("p_id_tecnico", Types.INTEGER);

            try (ResultSet rs = cmd.executeQuery()) {
                List notificaciones = new ArrayList<>();
                while (rs.next()) {
                    notificaciones.add(mapear(rs));
                }
                return notificaciones;
            }
        }
    }

    @Override
    public List listarNoLeidas() throws SQLException {
        String sql = "{call listar_notificaciones_no_leidas()}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {

            List notificaciones = new ArrayList<>();
            while (rs.next()) {
                notificaciones.add(mapear(rs));
            }
            return notificaciones;
        }
    }
}