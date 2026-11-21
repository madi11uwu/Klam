package pe.edu.pucp.klam.dao;

import java.sql.SQLException;
import pe.edu.pucp.klam.modelo.usuariosPermisos.CuentaUsuario;

public interface CuentaUsuarioDAO {
    CuentaUsuario findByUsername(String username) throws SQLException;
    boolean esCuentaDeClinica(int idUsuario, int idClinica) throws SQLException;
    java.util.List<CuentaUsuario> findAll() throws SQLException;
    /** true si el cliente (clínica o paciente particular) ya tiene una cuenta de acceso. */
    boolean existePorCliente(String tipoCliente, int idCliente) throws SQLException;
    void insert(CuentaUsuario cuenta) throws SQLException;
    void cambiarPassword(int idUsuario, String passwordHash) throws SQLException;
}
