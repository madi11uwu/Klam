package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import java.util.List;
import java.util.Map;

/** Operaciones del ciclo de vida de una solicitud de cirugía. */
public interface SolicitudCirugiaBL {
    String SOLICITUD_POR_REVISAR = "POR_REVISAR";
    String SOLICITUD_DOCUMENTACION_INCOMPLETA = "DOCUMENTACION_INCOMPLETA";
    String SOLICITUD_RECHAZADA = "RECHAZADA";
    String SOLICITUD_APROBADA = "APROBADA";

    int registrarSolicitud(Cirugia datos, ClinicaHospital clinicaDestino, String pacienteNombre,
                           String observaciones) throws BLException;

    /** Devuelve los datos de negocio de la solicitud, incluido idSolicitud y estadoSolicitud. */
    Map<String, Object> findSolicitudById(int idSolicitud) throws BLException;
    List<Map<String, Object>> listarSolicitudes(String estado) throws BLException;
    List<Map<String, Object>> listarSolicitudesPorCliente(Cliente cliente) throws BLException;

    /** Se bloquea si falta algun documento obligatorio validado. */
    void aprobarSolicitud(int idSolicitud) throws BLException;
    void rechazarSolicitud(int idSolicitud, String motivo) throws BLException;

    /** Tras subsanar o resubir documentos, la solicitud vuelve a POR_REVISAR. */
    void reenviarSolicitud(int idSolicitud) throws BLException;
    boolean verificarDocumentacionCompleta(int idSolicitud) throws BLException;

    /**
     * Crea una cirugia NUEVA desde una solicitud APROBADA (la solicitud no se modifica) en una
     * transaccion. Los datos no informados en {@code cirugia} se toman de la solicitud.
     */
    int registrarCirugiaDesdeSolicitud(int idSolicitud, Cirugia cirugia, String responsable) throws BLException;

}
