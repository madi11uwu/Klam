package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;

public interface BandejaInstrumentalDAO extends DAO<BandejaInstrumental, Integer> {
    void asociarConsumible(int idBandeja, int idConsumible, int cantDespachada);
    void registrarConsumoReal(int idBandeja, int idConsumible, int cantConsumida);
    void cargarConsumiblesPorBandeja(BandejaInstrumental bandeja);
}