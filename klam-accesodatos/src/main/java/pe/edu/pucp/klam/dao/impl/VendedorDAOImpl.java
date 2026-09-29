package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.dao.VendedorDAO;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class VendedorDAOImpl extends UsuarioPlataformaDAOImpl<Vendedor> implements VendedorDAO {

    @Override
    public List<Vendedor> findAll() throws SQLException {
        String sql = "{call listar_vendedores()}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {

            List<Vendedor> vendedores = new ArrayList<>();

            while (rs.next()) {
                vendedores.add(mapear(rs, new Vendedor()));
            }

            return vendedores;
        }
    }

    @Override
    public Vendedor findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_vendedor_por_id(?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_usuario", id);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next()
                        ? mapear(rs, new Vendedor())
                        : null;
            }
        }
    }

    @Override
    public void insert(Vendedor vendedor) throws SQLException {
        if (vendedor == null) {
            throw new IllegalArgumentException(
                    "El vendedor no puede ser nulo"
            );
        }

        String sql = "{call insertar_vendedor(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setString("p_username", vendedor.getUsername());
            cmd.setString("p_password_hash", vendedor.getPasswordHash());
            cmd.setString("p_email", vendedor.getEmail());
            cmd.setString("p_nombres", vendedor.getNombres());
            cmd.setString("p_apellidos", vendedor.getApellidos());
            cmd.setString("p_rol", vendedor.getRol());
            cmd.setBoolean("p_activo", vendedor.isActivo());

            cmd.setDouble(
                    "p_comision_acumulada",
                    vendedor.getComisionAcumulada()
            );

            cmd.registerOutParameter("p_id_usuario", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo insertar el vendedor"
                );
            }

            vendedor.setIdUsuario(
                    cmd.getInt("p_id_usuario")
            );
        }
    }

    @Override
    public void update(Vendedor vendedor) throws SQLException {
        if (vendedor == null) {
            throw new IllegalArgumentException(
                    "El vendedor no puede ser nulo"
            );
        }

        String sql = "{call modificar_vendedor(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setString("p_username", vendedor.getUsername());
            cmd.setString("p_password_hash", vendedor.getPasswordHash());
            cmd.setString("p_email", vendedor.getEmail());
            cmd.setString("p_nombres", vendedor.getNombres());
            cmd.setString("p_apellidos", vendedor.getApellidos());
            cmd.setString("p_rol", vendedor.getRol());
            cmd.setBoolean("p_activo", vendedor.isActivo());

            cmd.setDouble("p_comision_acumulada", vendedor.getComisionAcumulada());

            cmd.setInt("p_id_usuario", vendedor.getIdUsuario());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo actualizar el vendedor"
                );
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call eliminar_vendedor(?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_usuario", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo eliminar el vendedor"
                );
            }
        }
    }

    @Override
    protected Vendedor mapear(ResultSet rs, Vendedor vendedor) throws SQLException {
        super.mapear(rs, vendedor);

        vendedor.setComisionAcumulada(
                rs.getDouble("comision_acumulada")
        );

        return vendedor;
    }
}
