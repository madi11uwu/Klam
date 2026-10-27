package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;

import java.util.List;

public interface EquipoDAO extends DAO<Equipo,Integer> {
    List<Equipo> listarDisponibles();
    void registrarRetorno(int idEquipo);
}
