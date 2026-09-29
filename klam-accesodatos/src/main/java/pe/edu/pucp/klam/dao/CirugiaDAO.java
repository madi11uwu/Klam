package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface CirugiaDAO extends DAO<Cirugia, Integer> {
    List<Cirugia> findByEstado(EstadoCirugia estado) throws SQLException;
    List<Cirugia> findByRangoFechas(LocalDateTime desde, LocalDateTime hasta) throws SQLException;
    void cancelar(Integer id, String motivo) throws SQLException;
}
