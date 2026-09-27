package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;

import java.time.LocalDateTime;
import java.util.List;

public interface CirugiaBL extends RegistroBL<Cirugia, Integer> {
    List<Cirugia> findByEstado(EstadoCirugia estado) throws BLException;
    List<Cirugia> findByRangoFechas(LocalDateTime desde, LocalDateTime hasta) throws BLException;
    void cancelar(Integer id, String motivo) throws BLException;
}
