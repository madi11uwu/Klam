package pe.edu.pucp.klam.dao.impl.compras;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.List;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.LineaOrdenCompra;

public interface LineaOrdenCompraDAO {
    Integer insertar(LineaOrdenCompra linea, Integer idOrdenCompra, Connection conn) throws SQLException;
    void eliminarPorOrdenCompra(Integer idOrdenCompra, Connection conn) throws SQLException;
    List<LineaOrdenCompra> listarPorOrdenCompra(Integer idOrdenCompra, Connection conn) throws SQLException;
}