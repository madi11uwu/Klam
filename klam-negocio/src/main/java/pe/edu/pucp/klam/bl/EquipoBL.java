package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
import java.util.List;

public interface EquipoBL {
    List<Equipo> findAll() throws BLException;
    Equipo findById(Integer id) throws BLException;
    void insert(Equipo equipo) throws BLException;
    void update(Equipo equipo) throws BLException;
    void delete(Integer id) throws BLException;
}