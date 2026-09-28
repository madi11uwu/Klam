package pe.edu.pucp.klam.dao.impl.ventas;

import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaNotaCredito;

import java.sql.SQLException;
import java.util.List;

public interface LineaNotaCreditoDAO {
    void insertLineas(int idNotaCredito, List<LineaNotaCredito> lineas) throws SQLException;
    void deleteLineas(int idNotaCredito) throws SQLException;
    List<LineaNotaCredito> findByNotaCreditoId(int idNotaCredito) throws SQLException;

}
