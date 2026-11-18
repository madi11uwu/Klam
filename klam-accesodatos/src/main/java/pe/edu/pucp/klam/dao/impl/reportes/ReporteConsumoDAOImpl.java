package pe.edu.pucp.klam.dao.impl.reportes;

import pe.edu.pucp.klam.dao.ReporteConsumoDAO;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.db.DBManager;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ReporteConsumoDAOImpl implements ReporteConsumoDAO {

    @Override
    public List<Map<String, Object>> buscarFilasConsumo(Integer idCirugia, String tipoCliente, Integer idCliente,
                                                        LocalDateTime desde, LocalDateTime hasta,
                                                        String tipoProcedimiento, Integer idConsumible,
                                                        String medida) throws SQLException {
        String sql = "{call buscar_consumos_reporte(?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            DBManager.setIntONulo(cmd, "p_id_cirugia", idCirugia);
            DBManager.setStringONulo(cmd, "p_tipo_cliente", tipoCliente);
            DBManager.setIntONulo(cmd, "p_id_cliente", idCliente);
            DBManager.setFechaONula(cmd, "p_desde", desde);
            DBManager.setFechaONula(cmd, "p_hasta", hasta);
            DBManager.setStringONulo(cmd, "p_tipo_procedimiento", tipoProcedimiento);
            DBManager.setIntONulo(cmd, "p_id_consumible", idConsumible);
            DBManager.setStringONulo(cmd, "p_medida", medida);
            try (ResultSet rs = cmd.executeQuery()) {
                return DBManager.filasAMapas(rs);
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }
}