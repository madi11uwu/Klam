package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.dao.TecnicoInstrumentistaDAO;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.usuariosPermisos.TecnicoInstrumentista;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class TecnicoInstrumentistaDAOImpl extends UsuarioPlataformaDAOImpl<TecnicoInstrumentista> implements TecnicoInstrumentistaDAO {

    @Override
    public List<TecnicoInstrumentista> findAll() throws SQLException {
        String sql = "{call listar_tecnicos_instrumentistas()}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {

            List<TecnicoInstrumentista> tecnicos = new ArrayList<>();

            while (rs.next()) {
                tecnicos.add(mapear(rs, new TecnicoInstrumentista()));
            }

            return tecnicos;
        }
    }

    @Override
    public TecnicoInstrumentista findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_tecnico_instrumentista_por_id(?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_usuario", id);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next()
                        ? mapear(rs, new TecnicoInstrumentista())
                        : null;
            }
        }
    }

    @Override
    public void insert(TecnicoInstrumentista tecnico) throws SQLException {
        if (tecnico == null) {
            throw new IllegalArgumentException(
                    "El tecnico instrumentista no puede ser nulo"
            );
        }

        String sql = "{call insertar_tecnico_instrumentista(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setString("p_username", tecnico.getUsername());
            cmd.setString("p_password_hash", tecnico.getPasswordHash());
            cmd.setString("p_email", tecnico.getEmail());
            cmd.setString("p_nombres", tecnico.getNombres());
            cmd.setString("p_apellidos", tecnico.getApellidos());
            cmd.setString("p_rol", tecnico.getRol());
            cmd.setBoolean("p_activo", tecnico.isActivo());
            cmd.setString("p_especialidad", tecnico.getEspecialidad());
            cmd.registerOutParameter("p_id_usuario", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo insertar el tecnico instrumentista"
                );
            }

            tecnico.setIdUsuario(cmd.getInt("p_id_usuario"));
        }
    }

    @Override
    public void update(TecnicoInstrumentista tecnico) throws SQLException {
        if (tecnico == null) {
            throw new IllegalArgumentException(
                    "El tecnico instrumentista no puede ser nulo"
            );
        }

        String sql = "{call modificar_tecnico_instrumentista(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_username", tecnico.getUsername());
            cmd.setString("p_password_hash", tecnico.getPasswordHash());
            cmd.setString("p_email", tecnico.getEmail());
            cmd.setString("p_nombres", tecnico.getNombres());
            cmd.setString("p_apellidos", tecnico.getApellidos());
            cmd.setString("p_rol", tecnico.getRol());
            cmd.setBoolean("p_activo", tecnico.isActivo());
            cmd.setString("p_especialidad", tecnico.getEspecialidad());
            cmd.setInt("p_id_usuario", tecnico.getIdUsuario());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo actualizar el tecnico instrumentista"
                );
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        String sql = "{call eliminar_tecnico_instrumentista(?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_usuario", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo eliminar el tecnico instrumentista"
                );
            }
        }
    }

    @Override
    protected TecnicoInstrumentista mapear(ResultSet rs, TecnicoInstrumentista tecnico) throws SQLException {
        super.mapear(rs, tecnico);
        tecnico.setEspecialidad(
                rs.getString("especialidad")
        );
        return tecnico;
    }
}
