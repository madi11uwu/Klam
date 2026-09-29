package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.dao.AdministradorDAO;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Administrador;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class AdministradorDAOImpl extends UsuarioPlataformaDAOImpl<Administrador>  implements AdministradorDAO {

    @Override
    public List<Administrador>findAll() throws SQLException {
        String sql = "{call listar_administradores()}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {

            List administradores = new ArrayList<>();

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

            cmd.setInt("p_id", id);

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
            throw new IllegalArgumentException("El administrador no puede ser nulo");
        }

        String sql = "{call insertar_administrador(?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.setString("p_username", admin.getUsername());
            cmd.setString("p_password_hash", admin.getPasswordHash());
            cmd.setString("p_email", admin.getEmail());
            cmd.setString("p_nombres", admin.getNombres());
            cmd.setString("p_apellidos", admin.getApellidos());
            cmd.setBoolean("p_activo", admin.isActivo());

            cmd.executeUpdate();
            admin.setIdUsuario(cmd.getInt("p_id"));
        }
    }

    @Override
    public void update(Administrador admin) throws SQLException {
        if (admin == null) {
            throw new IllegalArgumentException("El administrador no puede ser nulo");
        }

        String sql = "{call modificar_administrador(?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", admin.getIdUsuario());
            cmd.setString("p_username", admin.getUsername());
            cmd.setString("p_password_hash", admin.getPasswordHash());
            cmd.setString("p_email", admin.getEmail());
            cmd.setString("p_nombres", admin.getNombres());
            cmd.setString("p_apellidos", admin.getApellidos());
            cmd.setBoolean("p_activo", admin.isActivo());

            cmd.executeUpdate();
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

            cmd.setInt("p_id", id);
            cmd.executeUpdate();
        }
    }
}