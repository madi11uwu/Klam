package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.dao.ConsumibleDAO;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsumibleDAOImpl implements ConsumibleDAO {


    @Override
    public List<Consumible> findByNombre(String nombre) throws SQLException {
        if(nombre==null){
            throw new IllegalArgumentException("El nombre no puede ser nulo");
        }
        String sql="{call buscar_consumibles_por_nombre(?)}";
        try(
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_nombre", nombre);

            try (ResultSet rs = cmd.executeQuery()) {
                List<Consumible> consumibles = new ArrayList<>();
                while (rs.next()) {
                    consumibles.add(mapear(rs));
                }
                return consumibles;
            }
        }
    }

    @Override
    public List<Consumible> findAll() throws SQLException {
        String sql = "{call listar_consumibles()}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            List<Consumible> consumibles = new ArrayList<>();
            while (rs.next()) {
                consumibles.add(mapear(rs));
            }
            return consumibles;
        }
    }

    @Override
    public Consumible findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        String sql = "{call buscar_consumible_por_id(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", id);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void insert(Consumible consumible) throws SQLException {
        if (consumible == null) {
            throw new IllegalArgumentException("El consumible no puede ser nulo");
        }
        String sql = "{call insertar_consumible(?,?,?,?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.setString("p_nombre_comercial", consumible.getNombreComercial());
            cmd.setString("p_marca", consumible.getMarca());
            cmd.setString("p_medida", consumible.getMedida());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar el consumible");
            }
            consumible.setId_consumible(cmd.getInt("p_id"));


        }
    }

    @Override
    public void update(Consumible consumible) throws SQLException {
        if (consumible == null) {
            throw new IllegalArgumentException("El consumible no puede ser nulo");
        }
        String sql = "{call modificar_consumible(?,?,?,?,?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", consumible.getId_consumible());
            cmd.setString("p_nombre_comercial", consumible.getNombreComercial());
            cmd.setString("p_marca", consumible.getMarca());
            cmd.setString("p_medida", consumible.getMedida());
            cmd.setBoolean("p_activo", consumible.isActivo());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar el consumible");
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call eliminar_consumible(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar el consumible");
            }
        }
    }

    private Consumible mapear(ResultSet rs) throws SQLException {
        Consumible c = new Consumible();
        c.setId_consumible(rs.getInt("id_consumible"));
        c.setNombreComercial(rs.getString("nombre_comercial"));
        c.setMarca(rs.getString("marca"));
        c.setMedida(rs.getString("medida"));
        c.setActivo(rs.getBoolean("activo"));
        return c;
    }



}
