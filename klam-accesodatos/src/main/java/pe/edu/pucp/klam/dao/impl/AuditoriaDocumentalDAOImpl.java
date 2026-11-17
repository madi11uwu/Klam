package pe.edu.pucp.klam.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import pe.edu.pucp.klam.dao.AuditoriaDocumentalDAO;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;

public class AuditoriaDocumentalDAOImpl implements AuditoriaDocumentalDAO {
    @Override
    public void registrar(Integer idDocumento, Integer idSolicitud, Integer idCirugia,
            String tipoDocumento, String accion, String responsable, String rolResponsable,
            Integer idUsuarioResponsable, String estadoAnterior, String estadoNuevo,
            String descripcion) throws SQLException {
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cs = conn.prepareCall("{CALL insertar_auditoria_documental(?,?,?,?,?,?,?,?,?,?,?)}")) {
            entero(cs, 1, idDocumento);
            entero(cs, 2, idSolicitud);
            entero(cs, 3, idCirugia);
            cs.setString(4, tipoDocumento);
            cs.setString(5, accion);
            cs.setString(6, responsable);
            cs.setString(7, rolResponsable);
            entero(cs, 8, idUsuarioResponsable);
            cs.setString(9, estadoAnterior);
            cs.setString(10, estadoNuevo);
            cs.setString(11, descripcion);
            cs.executeUpdate();
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public List<Map<String, Object>> buscar(String cliente, String paciente, LocalDateTime desde,
            LocalDateTime hasta, String tipoDocumento, String accion, String responsable,
            Integer idSolicitud, Integer idCirugia, int limite, int desplazamiento) throws SQLException {
        return consultar(null, cliente, paciente, desde, hasta, tipoDocumento, accion,
                responsable, idSolicitud, idCirugia, limite, desplazamiento);
    }

    @Override
    public Map<String, Object> obtener(int idEvento) throws SQLException {
        List<Map<String, Object>> filas = consultar(idEvento, null, null, null, null,
                null, null, null, null, null, 1, 0);
        return filas.isEmpty() ? null : filas.get(0);
    }

    private List<Map<String, Object>> consultar(Integer idEvento, String cliente, String paciente,
            LocalDateTime desde, LocalDateTime hasta, String tipoDocumento, String accion,
            String responsable, Integer idSolicitud, Integer idCirugia, int limite, int desplazamiento)
            throws SQLException {
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cs = conn.prepareCall("{CALL buscar_auditoria_documental(?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            entero(cs, 1, idEvento);
            cs.setString(2, cliente);
            cs.setString(3, paciente);
            fecha(cs, 4, desde);
            fecha(cs, 5, hasta);
            cs.setString(6, tipoDocumento);
            cs.setString(7, accion);
            cs.setString(8, responsable);
            entero(cs, 9, idSolicitud);
            entero(cs, 10, idCirugia);
            cs.setInt(11, limite);
            cs.setInt(12, desplazamiento);
            List<Map<String, Object>> filas = new ArrayList<>();
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("idEvento", rs.getInt("id_evento"));
                    fila.put("idDocumento", rs.getObject("id_documento"));
                    fila.put("idSolicitud", rs.getObject("id_solicitud"));
                    fila.put("idCirugia", rs.getObject("id_cirugia"));
                    fila.put("fechaHora", rs.getTimestamp("fecha_hora").toLocalDateTime().toString());
                    fila.put("tipoDocumento", rs.getString("tipo_documento"));
                    fila.put("accion", rs.getString("accion"));
                    fila.put("responsable", rs.getString("responsable"));
                    fila.put("rolResponsable", rs.getString("rol_responsable"));
                    fila.put("idUsuarioResponsable", rs.getObject("id_usuario_responsable"));
                    fila.put("estadoAnterior", rs.getString("estado_anterior"));
                    fila.put("estadoNuevo", rs.getString("estado_nuevo"));
                    fila.put("descripcion", rs.getString("descripcion"));
                    fila.put("referencia", rs.getString("archivo_path"));
                    fila.put("cliente", rs.getString("cliente"));
                    fila.put("paciente", rs.getString("paciente"));
                    filas.add(fila);
                }
            }
            return filas;
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    private static void entero(CallableStatement cs, int indice, Integer valor) throws SQLException {
        if (valor == null) cs.setNull(indice, Types.INTEGER);
        else cs.setInt(indice, valor);
    }

    private static void fecha(CallableStatement cs, int indice, LocalDateTime valor) throws SQLException {
        if (valor == null) cs.setNull(indice, Types.TIMESTAMP);
        else cs.setTimestamp(indice, Timestamp.valueOf(valor));
    }
}
