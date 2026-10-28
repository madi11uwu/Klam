package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DAO de cirugias. Tambien da acceso a las tablas SQL de solicitud de cirugia
 * y auditoria, que no tienen clase Java propia: sus consultas devuelven
 * {@code List<Map<String,Object>>} (claves en camelCase, ej. idCirugia).
 */
public interface CirugiaDAO extends DAO<Cirugia, Integer> {
    List<Cirugia> findByEstado(EstadoCirugia estado) throws SQLException;
    List<Cirugia> findByRangoFechas(LocalDateTime desde, LocalDateTime hasta) throws SQLException;
    void cancelar(Integer id, String motivo) throws SQLException;

    // ---- Filtros de calendario, lista e historial (RF06, RF10, RF17) ----
    // Todos los parametros son opcionales (null = sin filtrar).
    // tipoCliente: "CLINICA_HOSPITAL" o "PACIENTE_PARTICULAR"; idCliente requiere tipoCliente.
    List<Cirugia> buscar(LocalDateTime desde, LocalDateTime hasta, String tipoCliente,
                         Integer idCliente, Integer idEquipo, EstadoCirugia estado) throws SQLException;

    /** Mismos filtros, con los datos que muestra la interfaz (cliente, equipo, bandeja, cotizacion...). */
    List<Map<String, Object>> buscarDetalle(LocalDateTime desde, LocalDateTime hasta, String tipoCliente,
                                            Integer idCliente, Integer idEquipo, EstadoCirugia estado) throws SQLException;

    // ---- Reglas de negocio con acceso a datos ----
    boolean bandejaAsignadaAOtraCirugia(int idBandeja, Integer idCirugiaExcluir) throws SQLException;
    boolean existeCruceEquipo(int idEquipo, LocalDateTime inicio, LocalDateTime fin,
                              Integer idCirugiaExcluir) throws SQLException;
    void cambiarEstado(int idCirugia, EstadoCirugia estado, LocalDateTime fechaHoraFin) throws SQLException;
    void marcarConsumoCompleto(int idCirugia) throws SQLException;
    /** Id de la cirugia activa (no cancelada) creada desde la solicitud, o null. */
    Integer buscarCirugiaVigentePorSolicitud(int idSolicitud) throws SQLException;

    // ---- Solicitudes de cirugia (RF05, RF07, RF08) ----
    /** Inserta la solicitud con estado POR_REVISAR y devuelve su id. */
    int insertSolicitud(Cirugia datos, int idClinicaDestino, String pacienteNombre,
                        String observaciones, LocalDateTime fechaRegistro) throws SQLException;
    List<Map<String, Object>> buscarSolicitudes(Integer idSolicitud, String estado,
                                                String tipoCliente, Integer idCliente) throws SQLException;
    void cambiarEstadoSolicitud(int idSolicitud, String estado, String motivoRechazo,
                                LocalDateTime fechaRevision) throws SQLException;

    // ---- Auditoria (RF16) ----
    int insertAuditoria(int idCirugia, LocalDateTime fechaHora, String accion, String responsable,
                        String detalle, String valoresAnteriores, String valoresPosteriores) throws SQLException;
    List<Map<String, Object>> buscarAuditoria(Integer idAuditoria, Integer idCirugia, LocalDateTime desde,
                                              LocalDateTime hasta, String accion,
                                              String responsable) throws SQLException;
}
