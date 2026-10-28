package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.EquipoBL;
import pe.edu.pucp.klam.dao.CirugiaDAO;
import pe.edu.pucp.klam.dao.EquipoDAO;
import pe.edu.pucp.klam.dao.impl.CirugiaDAOImpl;
import pe.edu.pucp.klam.dao.impl.inventario.EquipoDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EquipoBLImpl implements EquipoBL {
    private final EquipoDAO equipoDAO = new EquipoDAOImpl();
    private final CirugiaDAO cirugiaDAO = new CirugiaDAOImpl();

    @Override
    public List<Equipo> listarDisponibles() throws BLException {
        try {
            return equipoDAO.listarDisponibles();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los equipos disponibles", e);
        }
    }

    @Override
    public void registrarRetorno(int idEquipo) throws BLException {
        validarId(idEquipo, "equipo");
        Equipo equipo = buscarEquipo(idEquipo);

        // El retorno se registra siempre a traves del despacho pendiente del equipo
        List<Map<String, Object>> pendientes = new ArrayList<>();
        for (Map<String, Object> d : listarRetornosPendientes()) {
            if (d.get("idEquipo") != null && ((Number) d.get("idEquipo")).intValue() == idEquipo) {
                pendientes.add(d);
            }
        }
        if (pendientes.isEmpty()) {
            throw new BLException("El equipo " + equipo.getNombre()
                    + " no tiene un despacho pendiente de retorno");
        }
        registrarRetornoDespacho(((Number) pendientes.get(0).get("idDespacho")).intValue());
    }

    @Override
    public String registrarDespacho(int idCirugia) throws BLException {
        validarId(idCirugia, "cirugía");
        Cirugia cirugia = buscarCirugia(idCirugia);

        if (!cirugia.isActivo()) {
            throw new BLException("La cirugía " + idCirugia + " está dada de baja");
        }
        if (cirugia.getEstado() != EstadoCirugia.PROGRAMADA && cirugia.getEstado() != EstadoCirugia.EN_PROCESO) {
            throw new BLException("Solo se despacha para cirugías programadas o en curso (estado actual: "
                    + cirugia.getEstado().getTexto() + ")");
        }

        Equipo equipo = cirugia.getEquipo();
        BandejaInstrumental bandeja = cirugia.getBandejaInstrumental();
        if (equipo == null && bandeja == null) {
            throw new BLException("La cirugía no tiene equipo ni bandeja asociados para despachar");
        }
        if (equipo != null) {
            Equipo actual = buscarEquipo(equipo.getIdEquipo());
            if (!actual.isActivo()) {
                throw new BLException("El equipo " + actual.getNombre() + " está dado de baja");
            }
            if (!actual.isDisponible()) {
                throw new BLException("El equipo " + actual.getNombre() + " no está disponible");
            }
        }
        try {
            if (bandeja != null && cirugiaDAO.bandejaAsignadaAOtraCirugia(bandeja.getIdBandeja(), idCirugia)) {
                throw new BLException("La bandeja " + bandeja.getIdBandeja() + " está asignada a otra cirugía");
            }
            if (!equipoDAO.buscarDespachos(null, null, idCirugia, true).isEmpty()) {
                throw new BLException("La cirugía " + idCirugia + " ya tiene un despacho pendiente de retorno");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo validar el despacho de la cirugía " + idCirugia, e);
        }

        // Despacho + disponibilidad del equipo: o se guardan ambos o ninguno
        TransactionsManager.iniciar();
        try {
            int idDespacho = equipoDAO.insertarDespacho(idCirugia,
                    equipo != null ? equipo.getIdEquipo() : null,
                    bandeja != null ? bandeja.getIdBandeja() : null,
                    LocalDateTime.now());
            if (equipo != null) {
                equipoDAO.actualizarDisponibilidad(equipo.getIdEquipo(), false);
            }
            String codigo = (String) equipoDAO.buscarDespachos(idDespacho, null, null, false).get(0).get("codigo");
            TransactionsManager.commit();
            return codigo;
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo registrar el despacho: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public void registrarRetornoPorCodigo(String codigo) throws BLException {
        Map<String, Object> despacho = obtenerDespachoPorCodigo(codigo);
        registrarRetornoDespacho(((Number) despacho.get("idDespacho")).intValue());
    }

    @Override
    public void registrarRetornoDespacho(int idDespacho) throws BLException {
        validarId(idDespacho, "despacho");
        Map<String, Object> despacho;
        try {
            List<Map<String, Object>> encontrados = equipoDAO.buscarDespachos(idDespacho, null, null, false);
            if (encontrados.isEmpty()) {
                throw new BLException("No existe un despacho con id " + idDespacho);
            }
            despacho = encontrados.get(0);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el despacho " + idDespacho, e);
        }
        if (despacho.get("fechaRetorno") != null) {
            throw new BLException("El retorno del despacho " + despacho.get("codigo") + " ya fue registrado");
        }

        // Retorno + liberacion del equipo en una sola transaccion
        TransactionsManager.iniciar();
        try {
            equipoDAO.registrarRetornoDespacho(idDespacho, LocalDateTime.now());
            if (despacho.get("idEquipo") != null) {
                equipoDAO.registrarRetorno(((Number) despacho.get("idEquipo")).intValue());
            }
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo registrar el retorno: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public Map<String, Object> obtenerDespachoPorCodigo(String codigo) throws BLException {
        if (codigo == null || codigo.isBlank()) {
            throw new BLException("Debe indicar el código del despacho");
        }
        try {
            List<Map<String, Object>> encontrados = equipoDAO.buscarDespachos(null, codigo.trim(), null, false);
            if (encontrados.isEmpty()) {
                throw new BLException("No existe un despacho con código " + codigo);
            }
            return encontrados.get(0);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el despacho " + codigo, e);
        }
    }

    @Override
    public Map<String, Object> obtenerDespachoPorCirugia(int idCirugia) throws BLException {
        validarId(idCirugia, "cirugía");
        try {
            // el mas reciente primero
            List<Map<String, Object>> encontrados = equipoDAO.buscarDespachos(null, null, idCirugia, false);
            return encontrados.isEmpty() ? null : encontrados.get(0);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el despacho de la cirugía " + idCirugia, e);
        }
    }

    @Override
    public List<Map<String, Object>> listarDespachos() throws BLException {
        try {
            return equipoDAO.buscarDespachos(null, null, null, false);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los despachos", e);
        }
    }

    @Override
    public List<Map<String, Object>> listarRetornosPendientes() throws BLException {
        try {
            return equipoDAO.buscarDespachos(null, null, null, true);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los retornos pendientes", e);
        }
    }

    private void validarId(int id, String entidad) throws BLException {
        if (id <= 0) {
            throw new BLException("El id de " + entidad + " no es válido");
        }
    }

    private Equipo buscarEquipo(int idEquipo) throws BLException {
        try {
            Equipo equipo = equipoDAO.findById(idEquipo);
            if (equipo == null) {
                throw new BLException("No existe un equipo con id " + idEquipo);
            }
            return equipo;
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el equipo " + idEquipo, e);
        }
    }

    private Cirugia buscarCirugia(int idCirugia) throws BLException {
        try {
            Cirugia cirugia = cirugiaDAO.findById(idCirugia);
            if (cirugia == null) {
                throw new BLException("No existe una cirugía con id " + idCirugia);
            }
            return cirugia;
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la cirugía " + idCirugia, e);
        }
    }

    @Override
    public List<Equipo> findAll() throws BLException {
        try {
            return equipoDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los equipos", e);
        }
    }

    @Override
    public Equipo findById(Integer id) throws BLException {
        try {
            return equipoDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el equipo", e);
        }
    }

    @Override
    public void insert(Equipo equipo) throws BLException {
        validar(equipo);

        TransactionsManager.iniciar();
        try {
            equipoDAO.insert(equipo);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollback();
            throw new BLException("No se pudo registrar el equipo", e);
        }
    }

    @Override
    public void update(Equipo equipo) throws BLException {
        validar(equipo);
        validarExiste(equipo.getIdEquipo());

        TransactionsManager.iniciar();
        try {
            equipoDAO.update(equipo);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollback();
            throw new BLException("No se pudo actualizar el equipo", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarExiste(id);

        TransactionsManager.iniciar();
        try {
            equipoDAO.delete(id);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollback();
            throw new BLException("No se pudo eliminar el equipo", e);
        }
    }

    private void validar(Equipo equipo) throws BLException {
        if (equipo == null) {
            throw new BLException("El equipo no puede ser nulo");
        }
        if (equipo.getNombre() == null || equipo.getNombre().isBlank()) {
            throw new BLException("El nombre del equipo es obligatorio");
        }
        if (equipo.getCategoria() == null) {
            throw new BLException("La categoria del equipo es obligatoria");
        }
    }

    private void validarExiste(Integer id) throws BLException {
        try{
            if(equipoDAO.findById(id)==null){
                throw new BLException("No existe un equipo con id " + id);
            }
        }catch (SQLException e){
            throw new BLException("No se pudo verificar la existencia del equipo",e);
        }
    }
}