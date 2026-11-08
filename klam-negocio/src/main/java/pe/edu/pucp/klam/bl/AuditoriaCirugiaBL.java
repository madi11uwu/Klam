package pe.edu.pucp.klam.bl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Consultas de la bitácora de cirugías. La escritura acompaña la transacción de CirugiaBL. */
public interface AuditoriaCirugiaBL {
    // ---- Bitacora de auditoria (RF16) ----
    /** Claves de negocio: idAuditoria, fechaHora, idCirugia, accion, responsable y detalle. */
    List<Map<String, Object>> listarBitacora() throws BLException;
    List<Map<String, Object>> listarBitacoraPorCirugia(int idCirugia) throws BLException;
    /** Todos los parametros son opcionales. */
    List<Map<String, Object>> buscarBitacora(Integer idCirugia, LocalDateTime desde, LocalDateTime hasta,
                                             String accion, String responsable) throws BLException;
    /** Una entrada de la bitácora con valoresAnteriores y valoresPosteriores, o null. */
    Map<String, Object> obtenerEntradaBitacora(int idAuditoria) throws BLException;
    /** Diferencias registradas: campo, valorAnterior, valorPosterior. */
    List<Map<String, Object>> obtenerDetalleBitacora(int idAuditoria) throws BLException;
}
