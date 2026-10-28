package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Cirugias, solicitudes de cirugia y auditoria.
 *
 * <p>Una solicitud NO es una cirugia: el administrador registra cirugias (RF09), directamente o
 * a partir de una solicitud aprobada. Como no hay clase Java de solicitud, sus datos viajan en
 * una {@link Cirugia} (cliente, procedimiento, doctorNombre, diagnostico y fechaHoraInicio =
 * fecha y hora solicitadas) y se consultan como {@code Map<String,Object>}.
 *
 * <p>Estados Java/SQL -> texto de Blazor (EstadoCirugia.getTexto()): PROGRAMADA -> "Programada",
 * EN_PROCESO -> "En curso", FINALIZADA -> "Culminada", CANCELADA -> "Cancelada".
 *
 * <p>Estados de solicitud -> texto de Blazor: POR_REVISAR -> "Por revisar", DOCUMENTACION_INCOMPLETA ->
 * "Documentación incompleta", RECHAZADA -> "Rechazada · Resubida habilitada", APROBADA -> "Aprobada".
 */
public interface CirugiaBL extends BaseBL<Cirugia, Integer> {
    // ---- Tipos de cliente (los ids de clinicas y pacientes se solapan, por eso se indica el tipo) ----
    String TIPO_CLIENTE_CLINICA = "CLINICA_HOSPITAL";
    String TIPO_CLIENTE_PARTICULAR = "PACIENTE_PARTICULAR";

    // ---- Estados internos de la solicitud ----
    String SOLICITUD_POR_REVISAR = "POR_REVISAR";
    String SOLICITUD_DOCUMENTACION_INCOMPLETA = "DOCUMENTACION_INCOMPLETA";
    String SOLICITUD_RECHAZADA = "RECHAZADA";
    String SOLICITUD_APROBADA = "APROBADA";

    // ---- Acciones de la bitacora de auditoria ----
    String ACCION_CREAR = "CREAR";
    String ACCION_EDITAR = "EDITAR";
    String ACCION_CAMBIAR_ESTADO = "CAMBIAR_ESTADO";
    String ACCION_CANCELAR = "CANCELAR";
    String ACCION_ELIMINAR = "ELIMINAR";
    String ACCION_ASIGNAR_EQUIPO = "ASIGNAR_EQUIPO";
    String ACCION_CAMBIAR_EQUIPO = "CAMBIAR_EQUIPO";
    String ACCION_ASIGNAR_BANDEJA = "ASIGNAR_BANDEJA";
    String ACCION_CAMBIAR_BANDEJA = "CAMBIAR_BANDEJA";

    /** Responsable que se registra mientras no exista autenticacion real. */
    String RESPONSABLE_SISTEMA = "SISTEMA";

    // ---- CRUD existente ----
    List<Cirugia> findByEstado(EstadoCirugia estado) throws BLException;
    List<Cirugia> findByRangoFechas(LocalDateTime desde, LocalDateTime hasta) throws BLException;
    void cancelar(Integer id, String motivo) throws BLException;

    // ---- CRUD del administrador con responsable para la auditoria (RF02, RF09, RF16) ----
    /** Registro directo (sin solicitud previa): idSolicitudOrigen queda en NULL. */
    void registrar(Cirugia cirugia, String responsable) throws BLException;
    void modificar(Cirugia cirugia, String responsable) throws BLException;
    void cancelar(Integer id, String motivo, String responsable) throws BLException;
    void eliminar(Integer id, String responsable) throws BLException;
    /** PROGRAMADA -> EN_PROCESO -> FINALIZADA (para cancelar use cancelar con motivo). */
    void cambiarEstado(Integer id, EstadoCirugia nuevoEstado, String responsable) throws BLException;

    // ---- Calendario, lista e historial (RF06, RF10, RF17) ----
    /** Todos los filtros son opcionales; tipoCliente es obligatorio si se indica idCliente. */
    List<Cirugia> buscar(LocalDateTime desde, LocalDateTime hasta, String tipoCliente, Integer idCliente,
                         Integer idEquipo, EstadoCirugia estado) throws BLException;

    /**
     * Mismos filtros, con los datos de pantalla: idCirugia, fechaHoraInicio, fechaHoraFin,
     * tipoProcedimiento, doctorNombre, diagnostico, estado, motivoCancelacion, tipoCliente,
     * idCliente, clienteNombre, idEquipo, equipoNombre, idBandeja, bandejaTipo, pacienteNombre,
     * clinicaNombre, precioPactado, estadoCotizacion, idSolicitudOrigen.
     */
    List<Map<String, Object>> buscarDetalle(LocalDateTime desde, LocalDateTime hasta, String tipoCliente,
                                            Integer idCliente, Integer idEquipo, EstadoCirugia estado) throws BLException;

    List<Cirugia> listarPorTipoCliente(String tipoCliente) throws BLException;
    List<Cirugia> listarPorCliente(Cliente cliente) throws BLException;
    List<Cirugia> listarPorEquipo(int idEquipo) throws BLException;

    /** Historial de un cliente (fecha, hora de inicio y fin, medico y tipo de cirugia). */
    List<Map<String, Object>> historialPorCliente(Cliente cliente) throws BLException;

    /** Cirugias del mes para el calendario mensual. */
    List<Map<String, Object>> buscarCalendarioMensual(int anio, int mes) throws BLException;

    /**
     * Detalle de una cirugia: datos propios, cliente, paciente y clinica (desde la solicitud),
     * equipo y bandeja, estado documental, despacho, cotizacion y, solo si la cirugia esta
     * FINALIZADA, los consumibles despachados y consumidos.
     */
    Map<String, Object> obtenerDetalle(int idCirugia) throws BLException;

    // ---- Solicitudes de cirugia (RF05, RF07, RF08) ----
    /**
     * Registra la solicitud (POR_REVISAR) y notifica a los administradores (notificacion interna
     * en la misma transaccion; correo despues, sin deshacer la solicitud si falla). Devuelve su id.
     *
     * @param datos          cliente solicitante, tipoProcedimiento, doctorNombre, diagnostico y
     *                       fechaHoraInicio (= fecha y hora solicitadas)
     * @param clinicaDestino clinica u hospital donde se realizara (null = la del cliente clinica)
     */
    int registrarSolicitud(Cirugia datos, ClinicaHospital clinicaDestino, String pacienteNombre,
                           String observaciones) throws BLException;

    /** Claves: idSolicitud, codigo, tipoCliente, idCliente, clienteNombre, dni, clinicaNombre, pacienteNombre, ... */
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

    // ---- Bitacora de auditoria (RF16) ----
    /** Claves: id, fechaHora, cirugia, accion, responsable, detalle. */
    List<Map<String, Object>> listarBitacora() throws BLException;
    List<Map<String, Object>> listarBitacoraPorCirugia(int idCirugia) throws BLException;
    /** Todos los parametros son opcionales. */
    List<Map<String, Object>> buscarBitacora(Integer idCirugia, LocalDateTime desde, LocalDateTime hasta,
                                             String accion, String responsable) throws BLException;
    /** Una entrada de la bitacora (mismas claves + valoresAnteriores y valoresPosteriores), o null. */
    Map<String, Object> obtenerEntradaBitacora(int idAuditoria) throws BLException;
    /** Filas de la vista de detalle: campo, valorAnterior, valorPosterior. */
    List<Map<String, Object>> obtenerDetalleBitacora(int idAuditoria) throws BLException;
}
