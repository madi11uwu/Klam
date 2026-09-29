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

public class VendedorDAOImpl
        extends UsuarioPlataformaDAOImpl<Vendedor>
        implements VendedorDAO {

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

            cmd.setInt("p_id", id);

            try (ResultSet rs = cmd.executeQuery()) {

                if (rs.next()) {
                    return mapear(rs, new Vendedor());
                }

                return null;
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

        String sql = "{call insertar_vendedor(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.registerOutParameter("p_id", Types.INTEGER);

            cmd.setString("p_username", vendedor.getUsername());
            cmd.setString("p_password_hash", vendedor.getPasswordHash());
            cmd.setString("p_email", vendedor.getEmail());
            cmd.setString("p_nombres", vendedor.getNombres());
            cmd.setString("p_apellidos", vendedor.getApellidos());
            cmd.setDouble(
                    "p_comision_acumulada",
                    vendedor.getComisionAcumulada()
            );
            cmd.setBoolean("p_activo", vendedor.isActivo());

            cmd.execute();

            vendedor.setIdUsuario(
                    cmd.getInt("p_id")
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

        String sql = "{call modificar_vendedor(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", vendedor.getIdUsuario());
            cmd.setString("p_username", vendedor.getUsername());
            cmd.setString("p_password_hash", vendedor.getPasswordHash());
            cmd.setString("p_email", vendedor.getEmail());
            cmd.setString("p_nombres", vendedor.getNombres());
            cmd.setString("p_apellidos", vendedor.getApellidos());
            cmd.setDouble(
                    "p_comision_acumulada",
                    vendedor.getComisionAcumulada()
            );
            cmd.setBoolean("p_activo", vendedor.isActivo());

            cmd.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El id no puede ser nulo"
            );
        }

        String sql = "{call eliminar_vendedor(?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);

            cmd.executeUpdate();
        }
    }

    @Override
    protected Vendedor mapear(
            ResultSet rs,
            Vendedor vendedor
    ) throws SQLException {

        super.mapear(rs, vendedor);

        vendedor.setComisionAcumulada(
                rs.getDouble("comision_acumulada")
        );

        return vendedor;
    }
}
