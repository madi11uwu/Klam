package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;

public interface PacienteParticularBL extends BaseBL<PacienteParticular, Integer> {
    PacienteParticular findByDNI(String dni) throws BLException;

}
