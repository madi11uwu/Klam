package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;

public interface BandejaInstrumentalBL extends BaseBL<BandejaInstrumental, Integer> {
    void cargarConsumibles(BandejaInstrumental bandeja) throws Exception;
    void registrarConsumoReal(int idBandeja, int idConsumible, int cantConsumida) throws Exception;
}