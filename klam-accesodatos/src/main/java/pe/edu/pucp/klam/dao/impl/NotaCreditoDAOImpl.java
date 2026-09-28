package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.dao.NotaCreditoDAO;
import pe.edu.pucp.klam.dao.impl.ventas.LineaNotaCreditoDAO;
import pe.edu.pucp.klam.dao.impl.ventas.LineaNotaCreditoDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotaCreditoDAOImpl implements NotaCreditoDAO {
    @Override
    public List<NotaCredito> findAll() throws SQLException {
        String sql = "{call listar_notas_credito}";

        try( Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()){

            List<NotaCredito> notaCreditos = new ArrayList<>();
            while(rs.next()) {
                notaCreditos.add(mapear(rs, new NotaCredito()));
            }
            return notaCreditos;
        }
    }

    @Override
    public NotaCredito findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_nota_credito_por_id(?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new NotaCredito()) : null;
            }
        }
    }

    @Override
    public List<NotaCredito> findByFacturaId(int idFactura) throws SQLException {
        String sql = "{call listar_notas_credito_por_factura(?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_factura", idFactura);
            try (ResultSet rs = cmd.executeQuery()) {
                List<NotaCredito> notas = new ArrayList<>();
                while (rs.next()) {
                    notas.add(mapear(rs, new NotaCredito()));
                }
                return notas;
            }
        }
    }

    @Override
    public List<NotaCredito> findByBoletaId(int idBoleta) throws SQLException {
        String sql = "{call listar_notas_credito_por_boleta(?)}";

        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id_boleta", idBoleta);
            try (ResultSet rs = cmd.executeQuery()) {
                List<NotaCredito> notas = new ArrayList<>();
                while (rs.next()) {
                    notas.add(mapear(rs, new NotaCredito()));
                }
                return notas;
            }
        }
    }


    @Override
    public void insert(NotaCredito nota) throws SQLException {
        if (nota == null) {
            throw new IllegalArgumentException("La nota de credito no puede ser nula");
        }

        Connection conn = TransactionsManager.getConnection();

        nota.calcularMontoTotal();

        String sql = "{call modificar_nota_credito(?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            asignarDocumentoOriginal(cmd, nota);
            cmd.setString("p_motivo", nota.getMotivo());
            cmd.setTimestamp("p_fecha_emision", Timestamp.valueOf(nota.getFechaEmision()));
            cmd.setDouble("p_monto_total", nota.getMontoTotal());
            cmd.setBoolean("p_activo", nota.isActivo());
            cmd.setInt("p_id", nota.getIdNotaCredito());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la nota de credito");
            }
        }

        LineaNotaCreditoDAO lineaDAO = new LineaNotaCreditoDAOImpl();
        lineaDAO.deleteLineas(nota.getIdNotaCredito());
        lineaDAO.insertLineas(nota.getIdNotaCredito(), nota.getLineas());

    }

    @Override
    public void update(NotaCredito nota) throws SQLException {
        if (nota == null) {
            throw new IllegalArgumentException("La nota de credito no puede ser nula");
        }

        Connection conn = TransactionsManager.getConnection();

        nota.calcularMontoTotal();

        String sql = "{call modificar_nota_credito(?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            asignarDocumentoOriginal(cmd, nota);
            cmd.setString("p_motivo", nota.getMotivo());
            cmd.setTimestamp("p_fecha_emision", Timestamp.valueOf(nota.getFechaEmision()));
            cmd.setDouble("p_monto_total", nota.getMontoTotal());
            cmd.setBoolean("p_activo", nota.isActivo());
            cmd.setInt("p_id", nota.getIdNotaCredito());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la nota de credito");
            }
        }

        LineaNotaCreditoDAO lineaDAO = new LineaNotaCreditoDAOImpl();
        lineaDAO.deleteLineas(nota.getIdNotaCredito());
        lineaDAO.insertLineas(nota.getIdNotaCredito(), nota.getLineas());

    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        Connection conn = TransactionsManager.getConnection();

        String sql = "{call eliminar_nota_credito(?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", id);
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo dar de baja la nota de credito");
            }
        }

    }

    private void asignarDocumentoOriginal(CallableStatement cmd, NotaCredito nota) throws SQLException {
        DocumentoFacturacion original = nota.getDocumentoOriginal();
        if (original == null || original.getIdDocumento() <= 0) {
            throw new SQLException("La nota de credito requiere un documento original ya persistido");
        }

        if (original instanceof Factura) {
            cmd.setInt("p_id_factura_original", original.getIdDocumento());
            cmd.setNull("p_id_boleta_original", Types.INTEGER);
        } else if (original instanceof Boleta) {
            cmd.setNull("p_id_factura_original", Types.INTEGER);
            cmd.setInt("p_id_boleta_original", original.getIdDocumento());
        } else {
            throw new SQLException("Tipo de documento original no soportado: "
                    + original.getClass().getSimpleName());
        }
    }


    private NotaCredito mapear(ResultSet rs, NotaCredito nota) throws SQLException {
        // Se leen todas las columnas antes de hacer consultas anidadas.
        int idFactura = rs.getInt("id_factura_original");
        boolean sinFactura = rs.wasNull();
        int idBoleta = rs.getInt("id_boleta_original");
        boolean sinBoleta = rs.wasNull();

        nota.setIdNotaCredito(rs.getInt("id_nota_credito"));
        nota.setMotivo(rs.getString("motivo"));
        nota.setFechaEmision(rs.getTimestamp("fecha_emision").toLocalDateTime());
        nota.setActivo(rs.getBoolean("activo"));

        if (!sinFactura) {
            nota.setDocumentoOriginal(new FacturaDAOImpl().findById(idFactura));
        } else if (!sinBoleta) {
            nota.setDocumentoOriginal(new BoletaDAOImpl().findById(idBoleta));
        }

        List<LineaNotaCredito> lineas = new LineaNotaCreditoDAOImpl()
                .findByNotaCreditoId(nota.getIdNotaCredito());
        nota.setLineas(lineas);   // recalcula montoTotal

        return nota;
    }

}
