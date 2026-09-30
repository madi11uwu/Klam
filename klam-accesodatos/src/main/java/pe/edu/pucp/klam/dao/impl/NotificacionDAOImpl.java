package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.dao.NotificacionDAO;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.comunicaciones.Notificacion;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Administrador;
import pe.edu.pucp.klam.modelo.usuariosPermisos.TecnicoInstrumentista;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificacionDAOImpl implements NotificacionDAO {

    @Override
    public List<Notificacion> findAll() throws SQLException {
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
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        String sql = "{call buscar_notificacion_por_id(?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt(1, id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void insert(Notificacion notificacion) throws SQLException {
        if (notificacion == null) throw new IllegalArgumentException("La notificación no puede ser nula");

        // El SP tiene 8 parámetros
        String sql = "{call insertar_notificacion(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.registerOutParameter(1, Types.INTEGER);
            cmd.setTimestamp(2, Timestamp.valueOf(notificacion.getFechaHora()));
            cmd.setString(3, notificacion.getTitulo());
            cmd.setString(4, notificacion.getMensaje());
            cmd.setBoolean(5, notificacion.isEstadoLeida());

            // Lógica para asignar el ID al rol correcto usando instanceof
            setDestinatarioParams(cmd, notificacion.getDestinatario(), 6, 7, 8);

            cmd.executeUpdate();
            notificacion.setIdNotificacion(cmd.getInt(1));
        }
    }

    @Override
    public void update(Notificacion notificacion) throws SQLException {
        if (notificacion == null) throw new IllegalArgumentException("La notificación no puede ser nula");

        String sql = "{call modificar_notificacion(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt(1, notificacion.getIdNotificacion());
            cmd.setTimestamp(2, Timestamp.valueOf(notificacion.getFechaHora()));
            cmd.setString(3, notificacion.getTitulo());
            cmd.setString(4, notificacion.getMensaje());
            cmd.setBoolean(5, notificacion.isEstadoLeida());

            setDestinatarioParams(cmd, notificacion.getDestinatario(), 6, 7, 8);

            cmd.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        String sql = "{call eliminar_notificacion(?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt(1, id);
            cmd.executeUpdate();
        }
    }

    @Override
    public void marcarLeida(Integer id, boolean leida) throws SQLException {
        String sql = "{call marcar_notificacion_leida(?, ?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt(1, id);
            cmd.setBoolean(2, leida);
            cmd.executeUpdate();
        }
    }

    @Override
    public List<Notificacion> listarPorDestinatario(Integer idAdministrador, Integer idVendedor, Integer idTecnico) throws SQLException {
        String sql = "{call listar_notificaciones_por_destinatario(?, ?, ?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            if (idAdministrador != null) cmd.setInt(1, idAdministrador);
            else cmd.setNull(1, Types.INTEGER);

            if (idVendedor != null) cmd.setInt(2, idVendedor);
            else cmd.setNull(2, Types.INTEGER);

            if (idTecnico != null) cmd.setInt(3, idTecnico);
            else cmd.setNull(3, Types.INTEGER);

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
    public List<Notificacion> listarNoLeidas() throws SQLException {
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

    // --- MÉTODOS AUXILIARES PRIVADOS ---

    private Notificacion mapear(ResultSet rs) throws SQLException {
        Notificacion noti = new Notificacion();
        noti.setIdNotificacion(rs.getInt("id_notificacion"));

        Timestamp ts = rs.getTimestamp("fecha_hora");
        noti.setFechaHora(ts != null ? ts.toLocalDateTime() : null);
        noti.setTitulo(rs.getString("titulo"));
        noti.setMensaje(rs.getString("mensaje"));
        noti.setEstadoLeida(rs.getBoolean("estado_leida"));

        // Recuperar el destinatario
        UsuarioPlataforma destinatario = null;

        int idAdmin = rs.getInt("id_administrador");
        if (!rs.wasNull()) {
            destinatario = new Administrador();
            destinatario.setIdUsuario(idAdmin);
        } else {
            int idVend = rs.getInt("id_vendedor");
            if (!rs.wasNull()) {
                destinatario = new Vendedor();
                destinatario.setIdUsuario(idVend);
            } else {
                int idTec = rs.getInt("id_tecnico");
                if (!rs.wasNull()) {
                    destinatario = new TecnicoInstrumentista();
                    destinatario.setIdUsuario(idTec);
                }
            }
        }
        noti.setDestinatario(destinatario);
        return noti;
    }

    private void setDestinatarioParams(CallableStatement cmd, UsuarioPlataforma destinatario, int idxAdmin, int idxVend, int idxTec) throws SQLException {
        // Por defecto todos nulos
        cmd.setNull(idxAdmin, Types.INTEGER);
        cmd.setNull(idxVend, Types.INTEGER);
        cmd.setNull(idxTec, Types.INTEGER);

        if (destinatario instanceof Administrador) {
            cmd.setInt(idxAdmin, destinatario.getIdUsuario());
        } else if (destinatario instanceof Vendedor) {
            cmd.setInt(idxVend, destinatario.getIdUsuario());
        } else if (destinatario instanceof TecnicoInstrumentista) {
            cmd.setInt(idxTec, destinatario.getIdUsuario());
        }
    }
}