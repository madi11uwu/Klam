package pe.edu.pucp.klam.dao.impl.ventas;

import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaFactura;

import java.sql.SQLException;
import java.util.List;

public interface LineaFacturaDAO {
    void insertLineas(int idDocumento, List<LineaFactura> lineas) throws SQLException;
    void deleteLineas(int idDocumento) throws SQLException;
    List<LineaFactura> findByDocumentoId(int idDocumento) throws SQLException;

}
