package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;

public interface PacienteParticularBL extends BaseBL<PacienteParticular, Integer> {
    PacienteParticular findByDNI(String dni) throws BLException;

    /**
     * Registra el cliente CON su cuenta de acceso (RF01): si no se puede crear la cuenta, el cliente no se registra.
     * {@link #insert(PacienteParticular)} hace lo mismo con credenciales iniciales (usuario = documento, contraseña = Klam + documento).
     */
    void insert(PacienteParticular cliente, String username, String password) throws BLException;
}
