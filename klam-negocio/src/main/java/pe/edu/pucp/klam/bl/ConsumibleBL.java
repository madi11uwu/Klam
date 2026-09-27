package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;

import java.util.List;

public interface ConsumibleBL {
    List<Consumible> findAll() throws BLException;
    List<Consumible> finByNombre(String nombre) throws BLException;
    Consumible findById(Integer id) throws BLException;
    void insert(Consumible consumible) throws BLException;
    void update(Consumible consumible) throws BLException;
    void delete(Integer id) throws BLException;
}
