package pe.edu.pucp.klam.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import pe.edu.pucp.klam.dao.CuentaUsuarioDAO;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.modelo.usuariosPermisos.CuentaUsuario;

public class CuentaUsuarioDAOImpl implements CuentaUsuarioDAO {
    @Override
    public CuentaUsuario findByUsername(String username) throws SQLException {
        String sql = "SELECT id_usuario, username, password_hash, email, nombres, apellidos, "
                + "activo, id_paciente, id_clinica FROM cuenta_usuario WHERE username = ?";
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, username);
            try (ResultSet rs = cmd.executeQuery()) {
                if (!rs.next()) return null;
                CuentaUsuario cuenta = new CuentaUsuario();
                cuenta.setIdUsuario(rs.getInt("id_usuario"));
                cuenta.setUsername(rs.getString("username"));
                cuenta.setPasswordHash(rs.getString("password_hash"));
                cuenta.setEmail(rs.getString("email"));
                cuenta.setNombres(rs.getString("nombres"));
                cuenta.setApellidos(rs.getString("apellidos"));
                cuenta.setActivo(rs.getBoolean("activo"));
                int idPaciente = rs.getInt("id_paciente");
                if (!rs.wasNull()) {
                    PacienteParticular paciente = new PacienteParticular();
                    paciente.setIdCliente(idPaciente);
                    cuenta.setCliente(paciente);
                    cuenta.setRol("PACIENTE_PARTICULAR");
                } else {
                    int idClinica = rs.getInt("id_clinica");
                    if (!rs.wasNull()) {
                        ClinicaHospital clinica = new ClinicaHospital();
                        clinica.setIdCliente(idClinica);
                        cuenta.setCliente(clinica);
                        cuenta.setRol("CLINICA_HOSPITAL");
                    }
                }
                return cuenta;
            }
        }
    }

    /** Todas las cuentas de clientes, con el nombre del cliente al que pertenecen. */
    @Override
    public java.util.List<CuentaUsuario> findAll() throws SQLException {
        String sql = "SELECT c.id_usuario, c.username, c.email, c.nombres, c.apellidos, c.activo, "
                + "c.id_paciente, c.id_clinica, p.nombre AS nombre_paciente, h.nombre AS nombre_clinica "
                + "FROM cuenta_usuario c "
                + "LEFT JOIN paciente_particular p ON p.id_cliente = c.id_paciente "
                + "LEFT JOIN clinica_hospital h ON h.id_cliente = c.id_clinica "
                + "ORDER BY c.id_usuario";
        java.util.List<CuentaUsuario> cuentas = new java.util.ArrayList<>();
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            while (rs.next()) {
                CuentaUsuario cuenta = new CuentaUsuario();
                cuenta.setIdUsuario(rs.getInt("id_usuario"));
                cuenta.setUsername(rs.getString("username"));
                cuenta.setEmail(rs.getString("email"));
                cuenta.setActivo(rs.getBoolean("activo"));
                String nombres = rs.getString("nombres");
                String apellidos = rs.getString("apellidos");
                int idPaciente = rs.getInt("id_paciente");
                if (!rs.wasNull()) {
                    PacienteParticular paciente = new PacienteParticular();
                    paciente.setIdCliente(idPaciente);
                    paciente.setNombre(rs.getString("nombre_paciente"));
                    cuenta.setCliente(paciente);
                    cuenta.setRol("PACIENTE_PARTICULAR");
                    if (nombres == null || nombres.isBlank()) nombres = rs.getString("nombre_paciente");
                } else {
                    int idClinica = rs.getInt("id_clinica");
                    ClinicaHospital clinica = new ClinicaHospital();
                    clinica.setIdCliente(idClinica);
                    clinica.setNombre(rs.getString("nombre_clinica"));
                    cuenta.setCliente(clinica);
                    cuenta.setRol("CLINICA_HOSPITAL");
                    if (nombres == null || nombres.isBlank()) nombres = rs.getString("nombre_clinica");
                }
                cuenta.setNombres(nombres == null ? "" : nombres);
                cuenta.setApellidos(apellidos == null ? "" : apellidos);
                cuentas.add(cuenta);
            }
        }
        return cuentas;
    }

    @Override
    public void insert(CuentaUsuario cuenta) throws SQLException {
        String sql = "INSERT INTO cuenta_usuario (username, password_hash, email, nombres, apellidos, "
                + "activo, id_paciente, id_clinica) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, cuenta.getUsername());
            cmd.setString(2, cuenta.getPasswordHash());
            cmd.setString(3, cuenta.getEmail());
            cmd.setString(4, cuenta.getNombres());
            cmd.setString(5, cuenta.getApellidos());
            cmd.setBoolean(6, cuenta.isActivo());
            if (cuenta.getCliente() instanceof PacienteParticular) {
                cmd.setInt(7, cuenta.getCliente().getIdCliente());
                cmd.setNull(8, java.sql.Types.INTEGER);
            } else if (cuenta.getCliente() instanceof ClinicaHospital) {
                cmd.setNull(7, java.sql.Types.INTEGER);
                cmd.setInt(8, cuenta.getCliente().getIdCliente());
            } else {
                throw new SQLException("La cuenta debe estar vinculada a un cliente");
            }
            cmd.executeUpdate();
            try (ResultSet rs = cmd.getGeneratedKeys()) {
                if (rs.next()) cuenta.setIdUsuario(rs.getInt(1));
            }
        }
    }

    @Override
    public void cambiarPassword(int idUsuario, String passwordHash) throws SQLException {
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement cmd = conn.prepareStatement(
                     "UPDATE cuenta_usuario SET password_hash = ? WHERE id_usuario = ?")) {
            cmd.setString(1, passwordHash);
            cmd.setInt(2, idUsuario);
            if (cmd.executeUpdate() != 1) throw new SQLException("Cuenta inexistente");
        }
    }
}
