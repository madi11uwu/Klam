package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.usuariosPermisos.Administrador;

public interface AdministradorBL extends RegistroBL<Administrador, Integer>{
    Administrador findByUsername(String username) throws BLException;

    Administrador findByEmail(String email) throws BLException;
}
