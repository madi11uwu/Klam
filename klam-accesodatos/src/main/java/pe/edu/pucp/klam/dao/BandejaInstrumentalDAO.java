package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Cada BandejaInstrumental es una bandeja FISICA: solo puede estar asignada a
 * una cirugia. Las cantidades despachadas/consumidas se guardan por
 * (id_bandeja, id_consumible).
 */
public interface BandejaInstrumentalDAO extends DAO<BandejaInstrumental, Integer> {
    // ---- Composicion de la bandeja ----
    /** Agrega un consumible; si ya esta en la bandeja el SQL lo rechaza (no se duplica). */
    void asociarConsumible(int idBandeja, int idConsumible, int cantDespachada) throws SQLException;
    void actualizarCantidadDespachada(int idBandeja, int idConsumible, int cantDespachada) throws SQLException;
    void quitarConsumible(int idBandeja, int idConsumible) throws SQLException;
    void cargarConsumiblesPorBandeja(BandejaInstrumental bandeja) throws SQLException;
    boolean tieneConsumoRegistrado(int idBandeja) throws SQLException;

    // ---- Consultas de bandejas ----
    List<BandejaInstrumental> listarPorProcedimiento(String procedimiento) throws SQLException;
    /** Bandejas activas, esterilizadas y sin cirugia asignada (procedimiento opcional: null = todas). */
    List<BandejaInstrumental> listarDisponibles(String procedimiento) throws SQLException;
    /** Id de la cirugia a la que esta asignada la bandeja, o null si esta libre. */
    Integer buscarCirugiaAsignada(int idBandeja) throws SQLException;

    // ---- Consumo real (RF15) ----
    void registrarConsumoReal(int idBandeja, int idConsumible, int cantConsumida) throws SQLException;

    /**
     * Despachado, consumido y diferencia por consumible de cirugias FINALIZADAS.
     * Todos los filtros son opcionales (cirugia, cliente, rango de fechas de la cirugia).
     */
    List<Map<String, Object>> buscarConsumos(Integer idCirugia, String tipoCliente, Integer idCliente,
                                             LocalDateTime desde, LocalDateTime hasta) throws SQLException;
}
