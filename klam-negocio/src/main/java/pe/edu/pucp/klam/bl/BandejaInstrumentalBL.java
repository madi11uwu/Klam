package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Cada BandejaInstrumental es una bandeja fisica: no puede estar en dos cirugias,
 * aunque otra bandeja tenga el mismo tipo o los mismos consumibles. Las cantidades
 * despachadas y consumidas se asocian a (idBandeja, idConsumible).
 */
public interface BandejaInstrumentalBL extends BaseBL<BandejaInstrumental, Integer> {
    // ---- Plantillas y composicion (RF13) ----
    void cargarConsumibles(BandejaInstrumental bandeja) throws BLException;
    void asociarConsumible(int idBandeja, int idConsumible, int cantDespachada) throws BLException;
    void actualizarCantidadDespachada(int idBandeja, int idConsumible, int cantDespachada) throws BLException;
    void quitarConsumible(int idBandeja, int idConsumible) throws BLException;
    List<BandejaInstrumental> listarPorProcedimiento(String procedimiento) throws BLException;

    /** Bandejas activas, esterilizadas y sin cirugia asignada. */
    List<BandejaInstrumental> listarDisponibles() throws BLException;
    List<BandejaInstrumental> listarDisponibles(String procedimiento) throws BLException;

    /**
     * Detalle completo: idBandeja, tipo, procedimiento, esterilizado, activo, idCirugia
     * (cirugia asignada o null) y consumibles (lista de idConsumible, nombreComercial,
     * medida, cantidadDespachada).
     */
    Map<String, Object> consultarDetalle(int idBandeja) throws BLException;

    // ---- Consumo real (RF15) ----
    /**
     * Solo con la cirugia FINALIZADA y la documentacion de cierre validada (orden de compra
     * VALIDADA para clinicas, comprobante de pago VALIDADO para pacientes particulares).
     */
    void registrarConsumoReal(int idBandeja, int idConsumible, int cantConsumida) throws BLException;

    /** Registra todos los consumos indicados (idConsumible -> cantidad) y cierra el registro, en una transaccion. */
    void registrarConsumoCompleto(int idCirugia, Map<Integer, Integer> consumosPorConsumible) throws BLException;

    /** Marca el consumo de la cirugia como completo: despues ya no se puede modificar. */
    void marcarConsumoCompleto(int idCirugia) throws BLException;

    /** Claves: idConsumible, nombreComercial, medida, cantidadDespachada, cantidadConsumida, diferencia, ... */
    List<Map<String, Object>> consultarConsumosPorCirugia(int idCirugia) throws BLException;
    List<Map<String, Object>> buscarConsumosPorCliente(String tipoCliente, int idCliente) throws BLException;
    List<Map<String, Object>> buscarConsumosPorRangoFechas(LocalDateTime desde, LocalDateTime hasta) throws BLException;

    /** Totales despachado, consumido y diferencia de una cirugia, mas el detalle por consumible. */
    Map<String, Object> obtenerResumenConsumo(int idCirugia) throws BLException;
}
