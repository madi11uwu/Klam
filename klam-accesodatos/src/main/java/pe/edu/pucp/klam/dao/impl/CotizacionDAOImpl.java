package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.dao.CotizacionDAO;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Cotizacion;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.EstadoCotizacion;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CotizacionDAOImpl implements CotizacionDAO {
    @Override
    public List<Cotizacion> findAll() throws SQLException {
        String sql = "{call listar_cotizaciones()}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql);
            ResultSet rs = cmd.executeQuery()) {

            List<Cotizacion> cotizaciones = new ArrayList<>();
            while (rs.next()) {
                cotizaciones.add(mapear(rs, new Cotizacion()));
            }
            return cotizaciones;
        }
    }

    @Override
    public Cotizacion findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_cotizacion_por_id(?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Cotizacion()) : null;
            }
        }
    }

    @Override
    public List<Cotizacion> findByCirugia(Integer idCirugia) throws SQLException {
        if (idCirugia == null) {
            throw new IllegalArgumentException("El id de la cirugia no puede ser nulo");
        }

        String sql = "{call listar_cotizaciones_por_cirugia(?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_cirugia", idCirugia);

            try (ResultSet rs = cmd.executeQuery()) {
                List<Cotizacion> cotizaciones = new ArrayList<>();
                while (rs.next()) {
                    cotizaciones.add(mapear(rs, new Cotizacion()));
                }
                return cotizaciones;
            }
        }
    }

    @Override
    public void insert(Cotizacion cotizacion) throws SQLException {
        if (cotizacion == null) {
            throw new IllegalArgumentException("La cotizacion no puede ser nula");
        }

        String sql = "{call insertar_cotizacion(?, ?, ?, ?, ?, ?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_cirugia", cotizacion.getCirugia().getIdCirugia());
            cmd.setDouble("p_precio_pactado", cotizacion.getPrecioPactado());
            cmd.setString("p_estado", cotizacion.getEstado().name());
            cmd.setTimestamp("p_fecha_emision", Timestamp.valueOf(cotizacion.getFechaEmision()));
            cmd.setBoolean("p_activo", cotizacion.isActivo());
            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la cotizacion");
            }

            cotizacion.setIdCotizacion(cmd.getInt("p_id"));
        }
    }

    @Override
    public void update(Cotizacion cotizacion) throws SQLException {
        if (cotizacion == null) {
            throw new IllegalArgumentException("La cotizacion no puede ser nula");
        }

        String sql = "{call modificar_cotizacion(?, ?, ?, ?, ?, ?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_cirugia", cotizacion.getCirugia().getIdCirugia());
            cmd.setDouble("p_precio_pactado", cotizacion.getPrecioPactado());
            cmd.setString("p_estado", cotizacion.getEstado().name());
            cmd.setTimestamp("p_fecha_emision", Timestamp.valueOf(cotizacion.getFechaEmision()));
            cmd.setBoolean("p_activo", cotizacion.isActivo());
            cmd.setInt("p_id", cotizacion.getIdCotizacion());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la cotizacion");
            }
        }
    }

    @Override
    public void actualizarEstado(Integer id, EstadoCotizacion estado) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }

        String sql = "{call actualizar_estado_cotizacion(?, ?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);
            cmd.setString("p_estado", estado.name());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar el estado de la cotizacion");
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call eliminar_cotizacion(?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar la cotizacion");
            }
        }
    }

    protected Cotizacion mapear(ResultSet rs, Cotizacion cotizacion) throws SQLException {
        cotizacion.setIdCotizacion(rs.getInt("id_cotizacion"));
        cotizacion.setPrecioPactado(rs.getDouble("precio_pactado"));
        cotizacion.setEstado(Enum.valueOf(EstadoCotizacion.class, rs.getString("estado")));
        cotizacion.setFechaEmision(rs.getTimestamp("fecha_emision").toLocalDateTime());
        cotizacion.setActivo(rs.getBoolean("activo"));
        cotizacion.setCirugia(new CirugiaDAOImpl().findById(rs.getInt("id_cirugia")));
        return cotizacion;
    }
}
