package pe.edu.pucp.klam.bl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** RF15: consulta y cierre del consumo real por cirugía. */
public interface ConsumoBL {

    /** Lista cirugías finalizadas con filtros opcionales y estado de cierre. */
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
