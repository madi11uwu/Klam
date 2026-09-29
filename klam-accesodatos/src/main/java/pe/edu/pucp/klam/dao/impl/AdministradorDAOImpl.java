package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.dao.AdministradorDAO;
import pe.edu.pucp.klam.dao.impl.UsuarioPlataformaDAOImpl;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Administrador;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class AdministradorDAOImpl extends UsuarioPlataformaDAOImpl<Administrador> implements AdministradorDAO {

    @Override
    public List<Administrador> findAll() throws SQLException {
        String sql = "{call listar_administradores()}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {

            List<Administrador> administradores = new ArrayList<>();

            while (rs.next()) {
                administradores.add(mapear(rs, new Administrador()));
            }

            return administradores;
        }
    }

    @Override
    public Administrador findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_administrador_por_id(?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_usuario", id);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next()
                        ? mapear(rs, new Administrador())
                        : null;
            }
        }
    }

    @Override
    public void insert(Administrador admin) throws SQLException {
        if (admin == null) {
            throw new IllegalArgumentException(
                    "El administrador no puede ser nulo"
            );
        }

        String sql = "{call insertar_administrador(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setString("p_username", admin.getUsername());
            cmd.setString("p_password_hash", admin.getPasswordHash());
            cmd.setString("p_email", admin.getEmail());
            cmd.setString("p_nombres", admin.getNombres());
            cmd.setString("p_apellidos", admin.getApellidos());
            cmd.setString("p_rol", admin.getRol());
            cmd.setBoolean("p_activo", admin.isActivo());
            cmd.registerOutParameter("p_id_usuario", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo insertar el administrador"
                );
            }

            admin.setIdUsuario(cmd.getInt("p_id_usuario"));
        }
    }

    @Override
    public void update(Administrador admin) throws SQLException {
        if (admin == null) {
            throw new IllegalArgumentException(
                    "El administrador no puede ser nulo"
            );
        }

        String sql = "{call modificar_administrador(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setString("p_username", admin.getUsername());
            cmd.setString("p_password_hash", admin.getPasswordHash());
            cmd.setString("p_email", admin.getEmail());
            cmd.setString("p_nombres", admin.getNombres());
            cmd.setString("p_apellidos", admin.getApellidos());
            cmd.setString("p_rol", admin.getRol());
            cmd.setBoolean("p_activo", admin.isActivo());
            cmd.setInt("p_id_usuario", admin.getIdUsuario());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo actualizar el administrador"
                );
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call eliminar_administrador(?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_usuario", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo eliminar el administrador"
                );
            }
        }
    }
}