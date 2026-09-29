package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;
import java.sql.SQLException;
import java.util.List;

public interface VendedorDAO extends DAO<Vendedor, Integer>{
    Vendedor findByUsername(String username) throws SQLException;

    Vendedor findByEmail(String email) throws SQLException;
}
