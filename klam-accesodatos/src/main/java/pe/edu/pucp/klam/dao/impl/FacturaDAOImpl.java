package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.dao.FacturaDAO;
import pe.edu.pucp.klam.dao.impl.ventas.LineaFacturaDAO;
import pe.edu.pucp.klam.dao.impl.ventas.LineaFacturaDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.EstadoPago;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Factura;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaDocumento;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaFactura;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacturaDAOImpl implements FacturaDAO {
    @Override
    public List<Factura> findAll() throws SQLException {

        String sql = "{call listar_facturas}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {

            List<Factura> facturas = new ArrayList<>();
            while (rs.next()) {
                facturas.add(mapear(rs, new Factura()));
            }
            return facturas;
        }
    }

    @Override
    public Factura findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_factura_por_id(?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Factura()) : null;
            }
        }

    }

    @Override
    public void insert(Factura factura) throws SQLException {
        if (factura == null){
            throw new IllegalArgumentException("La factura no puede ser nula");
        }

        Connection conn = TransactionsManager.getConnection();

        String sql = "{call insertar_factura(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (CallableStatement cmd = conn.prepareCall(sql)){
            cmd.setInt("p_id_cirugia", factura.getCirugia().getId_cirugia());
            cmd.setTimestamp("p_fecha_emision", Timestamp.valueOf(factura.getFechaEmision()));
            cmd.setDouble("p_monto_base", factura.getMontoBase());
            cmd.setDouble("p_tasa_igv", factura.getTasaIgv());
            cmd.setDouble("p_igv", factura.getIgv());
            cmd.setDouble("p_monto_total", factura.getMontoTotal());
            cmd.setString("p_estado_pago", factura.getEstadoPago().name());
            cmd.setBoolean("p_activo", factura.isActivo());
            cmd.setString("p_ruc_receptor", factura.getRucReceptor());
            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la factura");
            }

            factura.setIdDocumento(cmd.getInt("p_id"));
        }

        LineaFacturaDAO lineaFacturaDAO = new LineaFacturaDAOImpl();
        lineaFacturaDAO.insertLineas(factura.getIdDocumento(), castLineas(factura.getLineas()));
    }

    @Override
    public void update(Factura factura) throws SQLException {
        if (factura == null) {
            throw new IllegalArgumentException("La factura no puede ser nula");
        }

        Connection conn = TransactionsManager.getConnection();

        factura.calcularMontoTotal();

        String sql = "{call modificar_factura(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_cirugia", factura.getCirugia().getId_cirugia());
            cmd.setTimestamp("p_fecha_emision", Timestamp.valueOf(factura.getFechaEmision()));
            cmd.setDouble("p_monto_base", factura.getMontoBase());
            cmd.setDouble("p_tasa_igv", factura.getTasaIgv());
            cmd.setDouble("p_igv", factura.getIgv());
            cmd.setDouble("p_monto_total", factura.getMontoTotal());
            cmd.setString("p_estado_pago", factura.getEstadoPago().name());
            cmd.setBoolean("p_activo", factura.isActivo());
            cmd.setString("p_ruc_receptor", factura.getRucReceptor());
            cmd.setInt("p_id", factura.getIdDocumento());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la factura");
            }
        }

        // El detalle se reemplaza completo: se borra el anterior y se
        // inserta el actual, dentro de la misma transaccion.
        LineaFacturaDAO lineaFacturaDAO = new LineaFacturaDAOImpl();
        lineaFacturaDAO.deleteLineas(factura.getIdDocumento());
        lineaFacturaDAO.insertLineas(factura.getIdDocumento(), castLineas(factura.getLineas()));
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        Connection conn = TransactionsManager.getConnection();

        String sql = "{call eliminar_factura(?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", id);
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo dar de baja la factura");
            }
        }
    }


    private Factura mapear(ResultSet rs, Factura factura) throws SQLException {
        factura.setIdDocumento(rs.getInt("id_documento"));
        //falta el cirugiaDAO tdvia asjlkdfjasmdkl
        //factura.setCirugia(new CirugiaDAOImpl().findById(rs.getInt("id_cirugia")));
        factura.setFechaEmision(rs.getTimestamp("fecha_emision").toLocalDateTime());
        factura.setTasaIgv(rs.getDouble("tasa_igv"));
        factura.setEstadoPago(EstadoPago.valueOf(rs.getString("estado_pago")));
        factura.setActivo(rs.getBoolean("activo"));
        factura.setRucReceptor(rs.getString("ruc_receptor"));

        List<LineaFactura> lineas = new LineaFacturaDAOImpl().findByDocumentoId(factura.getIdDocumento());
        factura.setLineas(new ArrayList<>(lineas));

        return factura;
    }

    private List<LineaFactura> castLineas(List<LineaDocumento> lineas) {
        List<LineaFactura> resultado = new ArrayList<>();
        for (LineaDocumento linea : lineas) {
            resultado.add((LineaFactura) linea);
        }
        return resultado;
    }

}
