package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;
import pe.edu.pucp.klam.modelo.clientes.Cliente;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Operaciones de cirugía, calendario y consultas integradas.
 * Las solicitudes y las consultas de auditoría tienen contratos BL propios.
 */
public interface CirugiaBL extends BaseBL<Cirugia, Integer> {
    // ---- Tipos de cliente (los ids de clinicas y pacientes se solapan, por eso se indica el tipo) ----
    String TIPO_CLIENTE_CLINICA = "CLINICA_HOSPITAL";
    String TIPO_CLIENTE_PARTICULAR = "PACIENTE_PARTICULAR";

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
     * Mismos filtros, con los datos de negocio: idCirugia, fechaHoraInicio, fechaHoraFin,
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

}
