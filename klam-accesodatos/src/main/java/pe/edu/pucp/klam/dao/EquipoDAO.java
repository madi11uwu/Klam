package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;

import java.util.List;

public interface EquipoDAO {
    List<Equipo> findAll();
    Equipo findById(int id);
    void insert(Equipo equipo);
    void update(Equipo equipo);
    void delete(int id);
}
