package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Tambien da acceso a la tabla SQL despacho (RF14), que no tiene clase Java propia:
 * sus consultas devuelven {@code List<Map<String,Object>>} con las claves
 * idDespacho, codigo, idCirugia, idEquipo, equipoNombre, idBandeja, bandejaTipo,
 * fechaSalida, fechaRetorno y estado.
 */
public interface EquipoDAO extends DAO<Equipo,Integer> {
    List<Equipo> listarDisponibles() throws SQLException;

    /** Libera el equipo (disponible = 1). */
    void registrarRetorno(int idEquipo) throws SQLException;

    void actualizarDisponibilidad(int idEquipo, boolean disponible) throws SQLException;

    // ---- Despacho y retorno ----
    /** Registra el despacho de los recursos de la cirugia y devuelve su id. */
    int insertarDespacho(int idCirugia, Integer idEquipo, Integer idBandeja,
                         LocalDateTime fechaSalida) throws SQLException;

    void registrarRetornoDespacho(int idDespacho, LocalDateTime fechaRetorno) throws SQLException;

    /** Filtros opcionales: id, codigo (DSP-0001), cirugia y solo pendientes de retorno. */
    List<Map<String, Object>> buscarDespachos(Integer idDespacho, String codigo, Integer idCirugia,
                                              boolean soloPendientes) throws SQLException;
}
