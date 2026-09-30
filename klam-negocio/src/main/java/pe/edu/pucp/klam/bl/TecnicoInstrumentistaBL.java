package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.usuariosPermisos.TecnicoInstrumentista;

public interface TecnicoInstrumentistaBL extends BaseBL<TecnicoInstrumentista, Integer> {
    TecnicoInstrumentista findByUsername(String username) throws BLException;

    TecnicoInstrumentista findByEmail(String email) throws BLException;
}
