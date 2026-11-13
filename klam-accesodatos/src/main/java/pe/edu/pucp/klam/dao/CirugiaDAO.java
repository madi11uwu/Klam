package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DAO de cirugias. Tambien da acceso a la auditoria de cirugias,
 * cuyas consultas devuelven {@code List<Map<String,Object>>}
 * (claves en camelCase, ej. idCirugia).
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
    Map<Integer, Integer> buscarConsumiblesPrevistos(int idCirugia) throws SQLException;
    void reemplazarConsumiblesPrevistos(int idCirugia, Map<Integer, Integer> cantidades) throws SQLException;
    boolean existeCruceEquipo(int idEquipo, LocalDateTime inicio, LocalDateTime fin,
                              Integer idCirugiaExcluir) throws SQLException;
    void cambiarEstado(int idCirugia, EstadoCirugia estado, LocalDateTime fechaHoraFin) throws SQLException;
    void marcarConsumoCompleto(int idCirugia) throws SQLException;
    /** Id de la cirugia activa (no cancelada) creada desde la solicitud, o null. */
    Integer buscarCirugiaVigentePorSolicitud(int idSolicitud) throws SQLException;

    // ---- Auditoria (RF16) ----
    int insertAuditoria(int idCirugia, LocalDateTime fechaHora, String accion, String responsable,
                        String detalle, String valoresAnteriores, String valoresPosteriores) throws SQLException;
    List<Map<String, Object>> buscarAuditoria(Integer idAuditoria, Integer idCirugia, LocalDateTime desde,
                                              LocalDateTime hasta, String accion,
                                              String responsable) throws SQLException;
}
