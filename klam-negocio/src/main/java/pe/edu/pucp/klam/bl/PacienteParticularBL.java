package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;

public interface PacienteParticularBL extends BaseBL<PacienteParticular, Integer> {

    /** Login de clientes: null si no existe. */
    pe.edu.pucp.klam.modelo.clientes.PacienteParticular findByUsername(String username) throws BLException;

    /** Guarda la contraseña del cliente; no valida formato: lo hace UsuarioPlataformaBL. */
    void guardarPasswordHash(int id, String username, String passwordHash) throws BLException;
}
