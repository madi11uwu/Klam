package pe.edu.pucp.klam.dao.impl.inventario;

import pe.edu.pucp.klam.dao.EquipoDAO;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.CategoriaEquipo;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EquipoDAOImpl implements EquipoDAO {

    @Override
    public List<Equipo> listarDisponibles() throws SQLException {
        String sql = "{call listar_equipos_disponibles()}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cs = conn.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            List<Equipo> equipos = new ArrayList<>();
            while (rs.next()) {
                equipos.add(mapear(rs));
            }
            return equipos;
        }
    }

    @Override
    public void registrarRetorno(int idEquipo) throws SQLException {
        actualizarDisponibilidad(idEquipo, true);
    }

    @Override
    public void actualizarDisponibilidad(int idEquipo, boolean disponible) throws SQLException {
        String sql = "{call actualizar_disponibilidad_equipo(?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt("p_id_equipo", idEquipo);
            cs.setBoolean("p_disponible", disponible);

            if (cs.executeUpdate() == 0) {
                throw new SQLException("No existe el equipo " + idEquipo);
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public int insertarDespacho(int idCirugia, Integer idEquipo, Integer idBandeja,
                                LocalDateTime fechaSalida) throws SQLException {
        String sql = "{call insertar_despacho(?, ?, ?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.registerOutParameter("p_id", Types.INTEGER);
            cs.setInt("p_id_cirugia", idCirugia);
            DBManager.setIntONulo(cs, "p_id_equipo", idEquipo);
            DBManager.setIntONulo(cs, "p_id_bandeja", idBandeja);
            cs.setTimestamp("p_fecha_salida", Timestamp.valueOf(fechaSalida));

            if (cs.executeUpdate() == 0) {
                throw new SQLException("No se pudo registrar el despacho de la cirugia " + idCirugia);
            }
            return cs.getInt("p_id");
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public void registrarRetornoDespacho(int idDespacho, LocalDateTime fechaRetorno) throws SQLException {
        String sql = "{call registrar_retorno_despacho(?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt("p_id_despacho", idDespacho);
            cs.setTimestamp("p_fecha_retorno", Timestamp.valueOf(fechaRetorno));

            if (cs.executeUpdate() == 0) {
                throw new SQLException("No se pudo registrar el retorno del despacho " + idDespacho);
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public List<Map<String, Object>> buscarDespachos(Integer idDespacho, String codigo, Integer idCirugia,
                                                     boolean soloPendientes) throws SQLException {
        String sql = "{call buscar_despachos(?, ?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cs = conn.prepareCall(sql)) {
            DBManager.setIntONulo(cs, "p_id_despacho", idDespacho);
            DBManager.setStringONulo(cs, "p_codigo", codigo);
            DBManager.setIntONulo(cs, "p_id_cirugia", idCirugia);
            cs.setBoolean("p_solo_pendientes", soloPendientes);
            try (ResultSet rs = cs.executeQuery()) {
                return DBManager.filasAMapas(rs);
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

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

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs):null;
            }

        }
    }

    @Override
    public void insert(Equipo equipo) throws SQLException {
        if (equipo == null) {
            throw new IllegalArgumentException("El equipo no puede ser nulo");
        }
        Connection conn = TransactionsManager.getConnection();
        String sql = "{call insertar_equipo(?,?,?,?)}";

        try (CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.setString("p_nombre", equipo.getNombre());
            cmd.setString("p_categoria", equipo.getCategoria().name());
            cmd.setBoolean("p_disponible", equipo.isDisponible());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar el equipo");
            }
            equipo.setIdEquipo(cmd.getInt("p_id"));

            EquipoEspecificacionDAO equipoEspecificacionDAO = new EquipoEspecificacionDAOImpl();
            equipoEspecificacionDAO.insertEspecificaciones(equipo.getIdEquipo(), equipo.getEspecificaciones());

        }
    }

    @Override
    public void update(Equipo equipo) throws SQLException {
        if (equipo == null) {
            throw new IllegalArgumentException("El equipo no puede ser nulo");
        }
        Connection conn = TransactionsManager.getConnection();
        String sql = "{call modificar_equipo(?,?,?,?,?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", equipo.getIdEquipo());
            cmd.setString("p_nombre", equipo.getNombre());
            cmd.setString("p_categoria", equipo.getCategoria().name());
            cmd.setBoolean("p_disponible", equipo.isDisponible());
            cmd.setBoolean("p_activo", equipo.isActivo());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar el equipo");
            }
            EquipoEspecificacionDAO equipoEspecificacionDAO = new EquipoEspecificacionDAOImpl();
            equipoEspecificacionDAO.deleteEspecificaciones(equipo.getIdEquipo());
            equipoEspecificacionDAO.insertEspecificaciones(equipo.getIdEquipo(), equipo.getEspecificaciones());
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        Connection conn = TransactionsManager.getConnection();
        String sql = "{call eliminar_equipo(?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar el equipo");
            }
        }
    }

    private Equipo mapear(ResultSet rs) throws SQLException {
        Equipo e = new Equipo();
        e.setIdEquipo(rs.getInt("id_equipo"));
        e.setNombre(rs.getString("nombre"));
        e.setCategoria(CategoriaEquipo.valueOf(rs.getString("categoria")));
        e.setDisponible(rs.getBoolean("disponible"));
        e.setActivo(rs.getBoolean("activo"));
        mapearEspecificaciones(e);
        return e;
    }
    private void mapearEspecificaciones(Equipo equipo) throws SQLException {
        EquipoEspecificacionDAO especificacionDAO = new EquipoEspecificacionDAOImpl();
        equipo.setEspecificaciones(especificacionDAO.findByEquipoId(equipo.getIdEquipo()));
    }
}