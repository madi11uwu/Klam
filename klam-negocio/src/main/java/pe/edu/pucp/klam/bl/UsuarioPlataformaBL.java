package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;

public interface UsuarioPlataformaBL {
    UsuarioPlataforma autenticar(
            String username,
            String password
    ) throws BLException;
}
