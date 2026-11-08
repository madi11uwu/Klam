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

    // ---- Solicitudes de cirugia (RF05, RF07, RF08) ----
    /**
     * Registra la solicitud POR_REVISAR y la notificación interna en la misma transacción. Devuelve su id.
     * El envío externo de correo queda pendiente de un mecanismo compatible.
     *
     * @param datos          cliente solicitante, tipoProcedimiento, doctorNombre, diagnostico y
     *                       fechaHoraInicio (= fecha y hora solicitadas)
     * @param clinicaDestino clinica u hospital donde se realizara (null = la del cliente clinica)
     */
    int registrarSolicitud(Cirugia datos, ClinicaHospital clinicaDestino, String pacienteNombre,
                           String observaciones) throws BLException;

    /** Devuelve los datos de negocio de la solicitud, incluido idSolicitud y estadoSolicitud. */
    Map<String, Object> findSolicitudById(int idSolicitud) throws BLException;
    List<Map<String, Object>> listarSolicitudes(String estado) throws BLException;
    List<Map<String, Object>> listarSolicitudesPorCliente(Cliente cliente) throws BLException;

    /** Se bloquea si falta algun documento obligatorio validado. */
    void aprobarSolicitud(int idSolicitud, String responsable) throws BLException;
    void rechazarSolicitud(int idSolicitud, String motivo, String responsable) throws BLException;

    /** Tras subsanar o resubir documentos, la solicitud vuelve a POR_REVISAR. */
    void reenviarSolicitud(int idSolicitud) throws BLException;
    boolean verificarDocumentacionCompleta(int idSolicitud) throws BLException;

    /**
     * Crea una cirugia NUEVA desde una solicitud APROBADA (la solicitud no se modifica) en una
     * transaccion. Los datos no informados en {@code cirugia} se toman de la solicitud.
     */
    int registrarCirugiaDesdeSolicitud(int idSolicitud, Cirugia cirugia, String responsable) throws BLException;

}
