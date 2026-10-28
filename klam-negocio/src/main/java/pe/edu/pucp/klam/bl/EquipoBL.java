package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
import java.util.List;
import java.util.Map;

/**
 * Despacho y retorno (RF14): las consultas devuelven mapas con las claves
 * idDespacho, codigo (DSP-0001), idCirugia, idEquipo, equipoNombre, idBandeja,
 * bandejaTipo (kit), fechaSalida, fechaRetorno y estado (DESPACHADO / RETORNADO).
 */
public interface EquipoBL extends BaseBL<Equipo, Integer> {
    List<Equipo> listarDisponibles() throws BLException;

    /** Registra el retorno del equipo a traves de su despacho pendiente. */
    void registrarRetorno(int idEquipo) throws BLException;

    /**
     * Despacha el equipo y la bandeja asociados a la cirugia, marca el equipo como
     * no disponible y devuelve el codigo del despacho (DSP-0001). Es transaccional.
     */
    String registrarDespacho(int idCirugia) throws BLException;

    void registrarRetornoPorCodigo(String codigo) throws BLException;
    void registrarRetornoDespacho(int idDespacho) throws BLException;

    Map<String, Object> obtenerDespachoPorCodigo(String codigo) throws BLException;
    Map<String, Object> obtenerDespachoPorCirugia(int idCirugia) throws BLException;
    List<Map<String, Object>> listarDespachos() throws BLException;

    /** Despachos sin fecha de retorno (no depende de equipo.disponible). */
    List<Map<String, Object>> listarRetornosPendientes() throws BLException;
}
