package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;

import java.util.List;

public interface ConsumibleBL extends RegistroBL<Consumible, Integer> {
    List<Consumible> findByNombre(String nombre) throws BLException;   // solo lo extra
}