package pe.edu.pucp.klam.bl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * RF15: consumo real y cierre por cirugía. Reúne en un solo punto lo que necesita la pantalla de
 * consumos (y los reportes RF18/RF19): cirugías finalizadas con el estado de su documentación de
 * cierre y de su consumo, el despachado/consumido/diferencia por consumible, y el guardado.
 */
public interface ConsumoBL {

    /**
     * Cirugías FINALIZADAS (clínicas/hospitales y pacientes particulares), más antiguas primero.
     * Filtros opcionales: texto (CIR-001 o parte del nombre del cliente) y rango de fechas de la cirugía.
     * Claves: idCirugia, fechaHoraInicio, tipoProcedimiento, tipoCliente, clienteNombre, idBandeja,
     * requiereOrdenCompra, estadoOrdenCompra (NO_APLICA | POR_ADJUNTAR | PENDIENTE | VALIDADO | RECHAZADO),
     * documentacionCierreCompleta, estadoConsumo (NO_DISPONIBLE | POR_REGISTRAR | COMPLETO).
     */
    List<Map<String, Object>> listar(String texto, LocalDateTime desde, LocalDateTime hasta) throws BLException;

    /**
     * Misma información que listar, más: doctorNombre, pacienteNombre, clinicaNombre, archivoOrdenCompra,
     * motivoRechazoOrdenCompra, totalDespachado, totalConsumido, diferencia y la lista consumibles
     * (idConsumible, nombre, medida, cantidadDespachada, cantidadConsumida, diferencia).
     * Lanza NoEncontradoException si la cirugía no existe y ValidacionException si no está finalizada.
     */
    Map<String, Object> obtener(int idCirugia) throws BLException;

    /**
     * Guarda las cantidades consumidas (idConsumible -> cantidad). Con confirmar=true el consumo queda
     * completo y ya no se puede modificar. Las reglas viven en BandejaInstrumentalBL.guardarConsumo.
     */
    void guardar(int idCirugia, Map<Integer, Integer> consumosPorConsumible, boolean confirmar) throws BLException;
}
