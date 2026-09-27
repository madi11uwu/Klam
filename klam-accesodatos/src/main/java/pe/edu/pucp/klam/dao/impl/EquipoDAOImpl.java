package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.dao.EquipoDAO;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.CategoriaEquipo;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipoDAOImpl implements EquipoDAO {

    @Override
    public List<Equipo> findAll() throws SQLException {
        String sql = "{call listar_equipos()}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            List<Equipo> equipos = new ArrayList<>();
            while (rs.next()) {
                equipos.add(mapear(rs));
            }
            return equipos;
        }
    }

    @Override
    public Equipo findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        String sql = "{call buscar_equipo_por_id(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", id);
            try(ResultSet rs= cmd.executeQuery()){
                return rs.next()? mapear(rs): null;
            }
        }
    }

    @Override
    public void insert(Equipo equipo) throws SQLException {
        if(equipo==null){
            throw new IllegalArgumentException("El equipo no puede ser nulo");
        }
        String sql = "{call insertar_equipo(?,?,?,?)}";

        try(Connection conn=DBManager.getInstance().getConnection();
            CallableStatement cmd=conn.prepareCall(sql)){
            cmd.registerOutParameter("p_id",Types.INTEGER);
            cmd.setString("p_nombre", equipo.getNombre());
            cmd.setString("p_categoria",equipo.getCategoria().name());
            cmd.setBoolean("p_disponible",equipo.isDisponible());

            if(cmd.executeUpdate()==0){
                throw new SQLException("No se pudo insetar el equipo");
            }
            equipo.setId_equipo(cmd.getInt("p_id"));
        }
    }

    @Override
    public void update(Equipo equipo) throws SQLException {
        if(equipo==null){
            throw new IllegalArgumentException("El equipo no puede ser nulo");
        }
        String sql= "{call modificar_equipo(?,?,?,?,?)}";
        try(Connection conn=DBManager.getInstance().getConnection();
            CallableStatement cmd=conn.prepareCall(sql)){
            cmd.setInt("p_id",equipo.getId_equipo());
            cmd.setString("p_nombre",equipo.getNombre());
            cmd.setString("p_categoria",equipo.getCategoria().name());
            cmd.setBoolean("p_disponible",equipo.isDisponible());
            cmd.setBoolean("p_activo",equipo.isActivo());

            if(cmd.executeUpdate()==0){
                throw new SQLException("No se pudo actualizar el equipo");
            }
        }

    }

    @Override
    public void delete(Integer id) throws SQLException {
        if(id==null){
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        String sql="{call eliminar_equipo(?)}";
        try(Connection conn=DBManager.getInstance().getConnection();
            CallableStatement cmd= conn.prepareCall(sql)){
            cmd.setInt("p_id",id);

            if(cmd.executeUpdate()==0){
                throw new SQLException("No se pudo eliminar el equipo");
            }
        }
    }

    private Equipo mapear(ResultSet rs) throws SQLException {
        Equipo e = new Equipo();
        e.setId_equipo(rs.getInt("id_equipo"));
        e.setNombre(rs.getString("nombre"));
        e.setCategoria(CategoriaEquipo.valueOf(rs.getString("categoria")));
        e.setDisponible(rs.getBoolean("disponible"));
        e.setActivo(rs.getBoolean("activo"));
        return e;
    }

    private void insertarEspecificaciones(Connection conn, Equipo equipo) throws SQLException {
        String sql = "{call insertar_especificacion_equipo(?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {

            for (Map.Entry<String, Object> esp : equipo.getEspecificaciones().entrySet()) {
                cmd.setInt("p_id_equipo", equipo.getId_equipo());
                cmd.setString("p_clave", esp.getKey());
                cmd.setString("p_valor", String.valueOf(esp.getValue()));

                if (cmd.executeUpdate() == 0) {
                    throw new SQLException("No se pudo insertar la especificacion " + esp.getKey());
                }
            }
        }
    }

    private void eliminarEspecificaciones(Connection conn, int idEquipo) throws SQLException {
        String sql = "{call eliminar_especificaciones_equipo(?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_equipo", idEquipo);
            cmd.executeUpdate();   // sin validar 0: el equipo puede no tener especificaciones
        }
    }

    private void cargarEspecificaciones(Connection conn, Equipo equipo) throws SQLException {
        String sql = "{call listar_especificaciones_equipo(?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_equipo", equipo.getId_equipo());

            try (ResultSet rs = cmd.executeQuery()) {
                Map<String, Object> especificaciones = new HashMap<>();
                while (rs.next()) {
                    especificaciones.put(rs.getString("clave"), rs.getString("valor"));
                }
                equipo.setEspecificaciones(especificaciones);
            }
        }
    }
}