package pe.edu.pucp.klam.dao;

import java.sql.SQLException;
import pe.edu.pucp.klam.modelo.usuariosPermisos.CuentaUsuario;

public interface CuentaUsuarioDAO {
    CuentaUsuario findByUsername(String username) throws SQLException;
    void insert(CuentaUsuario cuenta) throws SQLException;
    void cambiarPassword(int idUsuario, String passwordHash) throws SQLException;
}
