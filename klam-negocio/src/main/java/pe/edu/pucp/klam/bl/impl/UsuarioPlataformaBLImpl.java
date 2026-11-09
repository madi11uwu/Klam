package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.*;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;

public class UsuarioPlataformaBLImpl implements UsuarioPlataformaBL {
    private final AdministradorBL administradorBL =
            new AdministradorBLImpl();

    private final VendedorBL vendedorBL =
            new VendedorBLImpl();

    private final TecnicoInstrumentistaBL tecnicoBL =
            new TecnicoInstrumentistaBLImpl();
    @Override
    public UsuarioPlataforma autenticar(
            String username,
            String password) throws BLException {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return null;
        }

        username = username.trim();

        UsuarioPlataforma usuario =
                administradorBL.findByUsername(username);

        if (usuario == null) {
            usuario = vendedorBL.findByUsername(username);
        }

        if (usuario == null) {
            usuario = tecnicoBL.findByUsername(username);
        }

        if (usuario == null || !usuario.isActivo()) {
            return null;
        }

        return usuario.getPasswordHash().equals(password)
                ? usuario
                : null;
    }

}
