package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.usuariosPermisos.TecnicoInstrumentista;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;

import java.sql.SQLException;
import java.util.List;

public interface TecnicoInstrumentistaDAO extends DAO <TecnicoInstrumentista, Integer>{
    TecnicoInstrumentista findByUsername(String username) throws SQLException;

    TecnicoInstrumentista findByEmail(String email) throws SQLException;

}
