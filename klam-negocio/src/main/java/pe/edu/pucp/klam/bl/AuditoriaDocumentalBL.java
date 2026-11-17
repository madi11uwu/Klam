package pe.edu.pucp.klam.bl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditoriaDocumentalBL {
    List<Map<String, Object>> buscar(String cliente, String paciente, LocalDateTime desde,
            LocalDateTime hasta, String tipoDocumento, String accion, String responsable,
            Integer idSolicitud, Integer idCirugia, int pagina, int tamano) throws BLException;

    Map<String, Object> obtener(int idEvento) throws BLException;
}
