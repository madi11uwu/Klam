package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;

public interface UsuarioPlataformaBL {
    UsuarioPlataforma autenticar(
            String username,
            String password
    ) throws BLException;

    /**
     * Cambia la contraseña del usuario (cualquier rol). Exige la contraseña actual, que la nueva cumpla la
     * política (8 a 64 caracteres, al menos una letra y un número, sin espacios) y que sea distinta.
     */
    void cambiarPassword(String username, String passwordActual, String passwordNueva) throws BLException;

    int habilitarCuentaCliente(String tipo, int idCliente, String username, String password) throws BLException;
}
