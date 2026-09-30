package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;

public interface VendedorBL extends BaseBL<Vendedor, Integer>{
    Vendedor findByUsername(String username) throws BLException;

    Vendedor findByEmail(String email) throws BLException;
}
