package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
import java.util.List;

public interface EquipoBL extends BaseBL<Equipo, Integer> {
    List<Equipo> listarDisponibles();
    void registrarRetorno(int idEquipo) throws Exception;
}