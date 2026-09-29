package pe.edu.pucp.klam.dao.impl.finanzas.ventas;

import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaBoleta;

import java.sql.SQLException;
import java.util.List;

public interface LineaBoletaDAO {
    void insertLineas(int idDocumento, List<LineaBoleta> lineas) throws SQLException;
    void deleteLineas(int idDocumento) throws SQLException;
    List<LineaBoleta> findByDocumentoId(int idDocumento) throws SQLException;

}
