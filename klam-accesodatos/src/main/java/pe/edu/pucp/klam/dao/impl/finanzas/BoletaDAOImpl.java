package pe.edu.pucp.klam.dao.impl.finanzas;

import pe.edu.pucp.klam.dao.BoletaDAO;
import pe.edu.pucp.klam.dao.impl.finanzas.ventas.LineaBoletaDAO;
import pe.edu.pucp.klam.dao.impl.finanzas.ventas.LineaBoletaDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Boleta;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.EstadoPago;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaBoleta;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaDocumento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BoletaDAOImpl implements BoletaDAO {

    @Override
    public List<Boleta> findAll() throws SQLException {
        String sql = "{call listar_boletas()}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {

            List<Boleta> boletas = new ArrayList<>();
            while (rs.next()) {
                boletas.add(mapear(rs, new Boleta()));
            }
            return boletas;
        }

    }

    @Override
    public Boleta findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_boleta_por_id(?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Boleta()) : null;
            }
        }

    }

    @Override
    public void insert(Boleta boleta) throws SQLException {
        if (boleta == null) {
            throw new IllegalArgumentException("La boleta no puede ser nula");
        }

        Connection conn = TransactionsManager.getConnection();

        // Nunca se confia en montos externos: se recalculan desde las lineas
        // antes de armar el INSERT.
        boleta.calcularMontoTotal();

        String sql = "{call insertar_boleta(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_cirugia", boleta.getCirugia().getId_cirugia());
            cmd.setTimestamp("p_fecha_emision", Timestamp.valueOf(boleta.getFechaEmision()));
            cmd.setDouble("p_monto_base", boleta.getMontoBase());
            cmd.setDouble("p_tasa_igv", boleta.getTasaIgv());
            cmd.setDouble("p_igv", boleta.getIgv());
            cmd.setDouble("p_monto_total", boleta.getMontoTotal());
            cmd.setString("p_estado_pago", boleta.getEstadoPago().name());
            cmd.setBoolean("p_activo", boleta.isActivo());
            cmd.setString("p_dni_receptor", boleta.getDniReceptor());
            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la boleta");
            }

            boleta.setIdDocumento(cmd.getInt("p_id"));
        }

        LineaBoletaDAO lineaBoletaDAO = new LineaBoletaDAOImpl();
        lineaBoletaDAO.insertLineas(boleta.getIdDocumento(), castLineas(boleta.getLineas()));
    }

    @Override
    public void update(Boleta boleta) throws SQLException {
        if (boleta == null) {
            throw new IllegalArgumentException("La boleta no puede ser nula");
        }

        Connection conn = TransactionsManager.getConnection();

        boleta.calcularMontoTotal();

        String sql = "{call modificar_boleta(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_cirugia", boleta.getCirugia().getId_cirugia());
            cmd.setTimestamp("p_fecha_emision", Timestamp.valueOf(boleta.getFechaEmision()));
            cmd.setDouble("p_monto_base", boleta.getMontoBase());
            cmd.setDouble("p_tasa_igv", boleta.getTasaIgv());
            cmd.setDouble("p_igv", boleta.getIgv());
            cmd.setDouble("p_monto_total", boleta.getMontoTotal());
            cmd.setString("p_estado_pago", boleta.getEstadoPago().name());
            cmd.setBoolean("p_activo", boleta.isActivo());
            cmd.setString("p_dni_receptor", boleta.getDniReceptor());
            cmd.setInt("p_id", boleta.getIdDocumento());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la boleta");
            }
        }

        LineaBoletaDAO lineaBoletaDAO = new LineaBoletaDAOImpl();
        lineaBoletaDAO.deleteLineas(boleta.getIdDocumento());
        lineaBoletaDAO.insertLineas(boleta.getIdDocumento(), castLineas(boleta.getLineas()));

    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        Connection conn = TransactionsManager.getConnection();

        // Baja logica: la boleta ya emitida no se borra, se desactiva.
        String sql = "{call eliminar_boleta(?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", id);
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo dar de baja la boleta");
            }
        }
    }

    private Boleta mapear(ResultSet rs, Boleta boleta) throws SQLException {
        boleta.setIdDocumento(rs.getInt("id_documento"));
        //Cirugia DAOIMPL
        //boleta.setCirugia(new CirugiaDAOImpl().findById(rs.getInt("id_cirugia")));
        boleta.setFechaEmision(rs.getTimestamp("fecha_emision").toLocalDateTime());
        boleta.setTasaIgv(rs.getDouble("tasa_igv"));
        boleta.setEstadoPago(EstadoPago.valueOf(rs.getString("estado_pago")));
        boleta.setActivo(rs.getBoolean("activo"));
        boleta.setDniReceptor(rs.getString("dni_receptor"));

        List<LineaBoleta> lineas = new LineaBoletaDAOImpl().findByDocumentoId(boleta.getIdDocumento());
        boleta.setLineas(new ArrayList<>(lineas));

        return boleta;
    }

    private List<LineaBoleta> castLineas(List<LineaDocumento> lineas) {
        List<LineaBoleta> resultado = new ArrayList<>();
        for (LineaDocumento linea : lineas) {
            resultado.add((LineaBoleta) linea);
        }
        return resultado;
    }

}
