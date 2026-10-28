package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.BandejaInstrumentalBL;
import pe.edu.pucp.klam.bl.DocumentoIngresoBL;
import pe.edu.pucp.klam.dao.BandejaInstrumentalDAO;
import pe.edu.pucp.klam.dao.CirugiaDAO;
import pe.edu.pucp.klam.dao.ConsumibleDAO;
import pe.edu.pucp.klam.dao.impl.CirugiaDAOImpl;
import pe.edu.pucp.klam.dao.impl.inventario.BandejaInstrumentalDAOImpl;
import pe.edu.pucp.klam.dao.impl.inventario.ConsumibleDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BandejaInstrumentalBLImpl implements BandejaInstrumentalBL {
    private final BandejaInstrumentalDAO bandejaInstrumentalDAO = new BandejaInstrumentalDAOImpl();
    private final ConsumibleDAO consumibleDAO = new ConsumibleDAOImpl();
    private final CirugiaDAO cirugiaDAO = new CirugiaDAOImpl();
    // La regla de documentacion de cierre vive en DocumentoIngresoBL: aqui solo se reutiliza
    private final DocumentoIngresoBL documentoBL = new DocumentoIngresoBLImpl();

    @Override
    public List<BandejaInstrumental> findAll() throws BLException {
        try {
            return bandejaInstrumentalDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las bandejas instrumentales", e);
        }
    }

    @Override
    public BandejaInstrumental findById(Integer id) throws BLException {
        try {
            return bandejaInstrumentalDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la bandeja instrumental", e);
        }
    }

    @Override
    public void insert(BandejaInstrumental bandejaInstrumental) throws BLException {
        validar(bandejaInstrumental);

        TransactionsManager.iniciar();
        try {
            bandejaInstrumentalDAO.insert(bandejaInstrumental);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollback();
            throw new BLException("No se pudo registrar la bandeja instrumental", e);
        }
    }

    @Override
    public void update(BandejaInstrumental bandejaInstrumental) throws BLException {
        validar(bandejaInstrumental);
        validarExiste(bandejaInstrumental.getIdBandeja());
        validarComposicionNoCambiaConConsumo(bandejaInstrumental);

        TransactionsManager.iniciar();
        try {
            bandejaInstrumentalDAO.update(bandejaInstrumental);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollback();
            throw new BLException("No se pudo actualizar la bandeja instrumental", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarExiste(id);

        TransactionsManager.iniciar();
        try {
            bandejaInstrumentalDAO.delete(id);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollback();
            throw new BLException("No se pudo eliminar la bandeja instrumental", e);
        }
    }

    private void validarExiste(Integer id) throws BLException {
        try {
            if (bandejaInstrumentalDAO.findById(id) == null) {
                throw new BLException("No existe una bandeja instrumental con id " + id);
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de la bandeja instrumental", e);
        }
    }

    private void validar(BandejaInstrumental bandeja) throws BLException {
        if (bandeja == null) {
            throw new BLException("La bandeja no puede ser nula");
        }
        if (bandeja.getTipo() == null || bandeja.getTipo().isBlank()) {
            throw new BLException("El tipo de bandeja es obligatorio");
        }

        Map<Consumible, Integer> despachados = bandeja.getConsumibles();
        Map<Consumible, Integer> consumidos = bandeja.getConsumiblesConsumidos();

        for (Map.Entry<Consumible, Integer> e : despachados.entrySet()) {
            if (e.getValue() <= 0) {
                throw new BLException("La cantidad despachada debe ser mayor que cero");
            }
            validarConsumibleExiste(e.getKey().getIdConsumible());
        }

        for (Map.Entry<Consumible, Integer> e : consumidos.entrySet()) {
            int consumida = e.getValue();
            int despachada = despachados.getOrDefault(e.getKey(), 0);
            if (consumida < 0) {
                throw new BLException("La cantidad consumida no puede ser negativa");
            }
            if (consumida > despachada) {
                throw new BLException("No se puede consumir mas de lo despachado (consumible "
                        + e.getKey().getIdConsumible() + ")");
            }
        }
    }

    private void validarConsumibleExiste(int idConsumible) throws BLException {
        try {
            Consumible consumible = consumibleDAO.findById(idConsumible);
            if (consumible == null) {
                throw new BLException("No existe un consumible con id " + idConsumible);
            }
            if (!consumible.isActivo()) {
                throw new BLException("El consumible " + idConsumible + " está dado de baja");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar el consumible", e);
        }
    }

    // ------------------------------------------------------------------
    // Plantillas y composicion (RF13)
    // ------------------------------------------------------------------
    @Override
    public void cargarConsumibles(BandejaInstrumental bandeja) throws BLException {
        if (bandeja == null || bandeja.getIdBandeja() <= 0) {
            throw new BLException("Bandeja inválida para cargar consumibles");
        }
        validarExiste(bandeja.getIdBandeja());
        try {
            bandejaInstrumentalDAO.cargarConsumiblesPorBandeja(bandeja);
        } catch (SQLException e) {
            throw new BLException("No se pudieron cargar los consumibles de la bandeja " + bandeja.getIdBandeja(), e);
        }
    }

    @Override
    public void asociarConsumible(int idBandeja, int idConsumible, int cantDespachada) throws BLException {
        BandejaInstrumental bandeja = buscarBandejaActiva(idBandeja);
        validarConsumibleExiste(idConsumible);
        validarCantidadDespachada(cantDespachada);
        validarSinConsumo(idBandeja);
        if (bandeja.getConsumibles().containsKey(consumibleConId(idConsumible))) {
            throw new BLException("El consumible " + idConsumible + " ya está en la bandeja " + idBandeja
                    + "; use actualizarCantidadDespachada");
        }

        TransactionsManager.iniciar();
        try {
            bandejaInstrumentalDAO.asociarConsumible(idBandeja, idConsumible, cantDespachada);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo asociar el consumible a la bandeja" + ": " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public void actualizarCantidadDespachada(int idBandeja, int idConsumible, int cantDespachada) throws BLException {
        BandejaInstrumental bandeja = buscarBandejaActiva(idBandeja);
        validarCantidadDespachada(cantDespachada);
        validarSinConsumo(idBandeja);
        if (!bandeja.getConsumibles().containsKey(consumibleConId(idConsumible))) {
            throw new BLException("El consumible " + idConsumible + " no pertenece a la bandeja " + idBandeja);
        }

        TransactionsManager.iniciar();
        try {
            bandejaInstrumentalDAO.actualizarCantidadDespachada(idBandeja, idConsumible, cantDespachada);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo actualizar la cantidad despachada" + ": " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public void quitarConsumible(int idBandeja, int idConsumible) throws BLException {
        BandejaInstrumental bandeja = buscarBandejaActiva(idBandeja);
        validarSinConsumo(idBandeja);
        if (!bandeja.getConsumibles().containsKey(consumibleConId(idConsumible))) {
            throw new BLException("El consumible " + idConsumible + " no pertenece a la bandeja " + idBandeja);
        }

        TransactionsManager.iniciar();
        try {
            bandejaInstrumentalDAO.quitarConsumible(idBandeja, idConsumible);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo quitar el consumible de la bandeja" + ": " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public List<BandejaInstrumental> listarPorProcedimiento(String procedimiento) throws BLException {
        if (procedimiento == null || procedimiento.isBlank()) {
            throw new BLException("Debe indicar el procedimiento");
        }
        try {
            return bandejaInstrumentalDAO.listarPorProcedimiento(procedimiento.trim());
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las bandejas del procedimiento " + procedimiento, e);
        }
    }

    @Override
    public List<BandejaInstrumental> listarDisponibles() throws BLException {
        return listarDisponibles(null);
    }

    @Override
    public List<BandejaInstrumental> listarDisponibles(String procedimiento) throws BLException {
        try {
            return bandejaInstrumentalDAO.listarDisponibles(
                    (procedimiento == null || procedimiento.isBlank()) ? null : procedimiento.trim());
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las bandejas disponibles", e);
        }
    }

    @Override
    public Map<String, Object> consultarDetalle(int idBandeja) throws BLException {
        BandejaInstrumental bandeja = buscarBandeja(idBandeja);

        List<Map<String, Object>> consumibles = new ArrayList<>();
        for (Map.Entry<Consumible, Integer> e : bandeja.getConsumibles().entrySet()) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("idConsumible", e.getKey().getIdConsumible());
            fila.put("nombreComercial", e.getKey().getNombreComercial());
            fila.put("medida", e.getKey().getMedida());
            fila.put("cantidadDespachada", e.getValue());
            consumibles.add(fila);
        }

        Integer idCirugia;
        try {
            idCirugia = bandejaInstrumentalDAO.buscarCirugiaAsignada(idBandeja);
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar la cirugía de la bandeja " + idBandeja, e);
        }

        Map<String, Object> detalle = new LinkedHashMap<>();
        detalle.put("idBandeja", bandeja.getIdBandeja());
        detalle.put("tipo", bandeja.getTipo());
        detalle.put("procedimiento", bandeja.getProcedimiento());
        detalle.put("esterilizado", bandeja.isEsterilizado());
        detalle.put("activo", bandeja.isActivo());
        detalle.put("idCirugia", idCirugia);
        detalle.put("consumibles", consumibles);
        return detalle;
    }

    // ------------------------------------------------------------------
    // Consumo real (RF15)
    // ------------------------------------------------------------------
    @Override
    public void registrarConsumoReal(int idBandeja, int idConsumible, int cantConsumida) throws BLException {
        validarConsumoRegistrable(idBandeja, idConsumible, cantConsumida);

        TransactionsManager.iniciar();
        try {
            bandejaInstrumentalDAO.registrarConsumoReal(idBandeja, idConsumible, cantConsumida);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo registrar el consumo real" + ": " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public void registrarConsumoCompleto(int idCirugia, Map<Integer, Integer> consumosPorConsumible) throws BLException {
        Cirugia cirugia = buscarCirugiaCerrada(idCirugia);
        if (consumosPorConsumible == null || consumosPorConsumible.isEmpty()) {
            throw new BLException("Debe indicar los consumos de la cirugía " + idCirugia);
        }
        int idBandeja = cirugia.getBandejaInstrumental().getIdBandeja();
        for (Map.Entry<Integer, Integer> e : consumosPorConsumible.entrySet()) {
            validarConsumoRegistrable(idBandeja, e.getKey(), e.getValue());
        }

        // Todos los consumos + el cierre del registro: o se guardan todos o ninguno
        TransactionsManager.iniciar();
        try {
            for (Map.Entry<Integer, Integer> e : consumosPorConsumible.entrySet()) {
                bandejaInstrumentalDAO.registrarConsumoReal(idBandeja, e.getKey(), e.getValue());
            }
            cirugiaDAO.marcarConsumoCompleto(idCirugia);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo registrar el consumo completo: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public void marcarConsumoCompleto(int idCirugia) throws BLException {
        buscarCirugiaCerrada(idCirugia);

        TransactionsManager.iniciar();
        try {
            cirugiaDAO.marcarConsumoCompleto(idCirugia);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo marcar el consumo como completo" + ": " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> consultarConsumosPorCirugia(int idCirugia) throws BLException {
        if (idCirugia <= 0) {
            throw new BLException("El id de cirugía no es válido");
        }
        return buscarConsumos(idCirugia, null, null, null, null);
    }

    @Override
    public List<Map<String, Object>> buscarConsumosPorCliente(String tipoCliente, int idCliente) throws BLException {
        if (tipoCliente == null || tipoCliente.isBlank()) {
            throw new BLException("Debe indicar el tipo de cliente (los ids de clínicas y pacientes se solapan)");
        }
        if (idCliente <= 0) {
            throw new BLException("El id de cliente no es válido");
        }
        return buscarConsumos(null, tipoCliente, idCliente, null, null);
    }

    @Override
    public List<Map<String, Object>> buscarConsumosPorRangoFechas(LocalDateTime desde, LocalDateTime hasta) throws BLException {
        if (desde == null || hasta == null) {
            throw new BLException("Debe indicar el inicio y el fin del rango de fechas");
        }
        if (desde.isAfter(hasta)) {
            throw new BLException("El inicio del rango no puede ser posterior al fin");
        }
        return buscarConsumos(null, null, null, desde, hasta);
    }

    @Override
    public Map<String, Object> obtenerResumenConsumo(int idCirugia) throws BLException {
        List<Map<String, Object>> detalle = consultarConsumosPorCirugia(idCirugia);

        long despachado = 0;
        long consumido = 0;
        for (Map<String, Object> fila : detalle) {
            despachado += ((Number) fila.get("cantidadDespachada")).longValue();
            consumido += ((Number) fila.get("cantidadConsumida")).longValue();
        }

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("idCirugia", idCirugia);
        resumen.put("totalDespachado", despachado);
        resumen.put("totalConsumido", consumido);
        resumen.put("diferencia", despachado - consumido);
        resumen.put("detalle", detalle);
        return resumen;
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------
    private List<Map<String, Object>> buscarConsumos(Integer idCirugia, String tipoCliente, Integer idCliente,
                                                     LocalDateTime desde, LocalDateTime hasta) throws BLException {
        try {
            return bandejaInstrumentalDAO.buscarConsumos(idCirugia, tipoCliente, idCliente, desde, hasta);
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar los consumos", e);
        }
    }

    // Reglas del consumo real (una sola vez, para registrarConsumoReal y registrarConsumoCompleto)
    private void validarConsumoRegistrable(int idBandeja, int idConsumible, int cantConsumida) throws BLException {
        if (idBandeja <= 0 || idConsumible <= 0) {
            throw new BLException("Identificadores inválidos");
        }
        if (cantConsumida < 0) {
            throw new BLException("La cantidad consumida no puede ser negativa");
        }
        BandejaInstrumental bandeja = buscarBandeja(idBandeja);
        validarConsumibleExiste(idConsumible);

        Integer idCirugia;
        try {
            idCirugia = bandejaInstrumentalDAO.buscarCirugiaAsignada(idBandeja);
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar la cirugía de la bandeja " + idBandeja, e);
        }
        if (idCirugia == null) {
            throw new BLException("La bandeja " + idBandeja + " no está asignada a ninguna cirugía");
        }
        buscarCirugiaCerrada(idCirugia);

        Integer despachada = bandeja.getConsumibles().get(consumibleConId(idConsumible));
        if (despachada == null) {
            throw new BLException("El consumible " + idConsumible + " no pertenece a la bandeja " + idBandeja);
        }
        if (cantConsumida > despachada) {
            throw new BLException("No se puede consumir más de lo despachado (despachado " + despachada
                    + ", consumido " + cantConsumida + ")");
        }
    }

    // Cirugia FINALIZADA, con documentacion de cierre validada y consumo aun no completo
    private Cirugia buscarCirugiaCerrada(int idCirugia) throws BLException {
        Cirugia cirugia;
        try {
            cirugia = cirugiaDAO.findById(idCirugia);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la cirugía " + idCirugia, e);
        }
        if (cirugia == null) {
            throw new BLException("No existe una cirugía con id " + idCirugia);
        }
        if (cirugia.getEstado() != EstadoCirugia.FINALIZADA) {
            throw new BLException("El consumo solo se registra con la cirugía FINALIZADA (estado actual: "
                    + cirugia.getEstado().getTexto() + ")");
        }
        if (cirugia.getBandejaInstrumental() == null) {
            throw new BLException("La cirugía " + idCirugia + " no tiene bandeja asignada");
        }
        if (cirugia.isConsumoCompleto()) {
            throw new BLException("El consumo de la cirugía " + idCirugia + " ya fue marcado como completo");
        }
        // clinica/hospital: orden de compra VALIDADA; paciente particular: comprobante de pago VALIDADO
        documentoBL.validarDocumentacionCierre(idCirugia);
        return cirugia;
    }

    private void validarComposicionNoCambiaConConsumo(BandejaInstrumental nueva) throws BLException {
        try {
            if (!bandejaInstrumentalDAO.tieneConsumoRegistrado(nueva.getIdBandeja())) {
                return;
            }
            BandejaInstrumental actual = bandejaInstrumentalDAO.findById(nueva.getIdBandeja());
            if (!actual.getConsumibles().equals(nueva.getConsumibles())) {
                throw new BLException("No se puede modificar la composición despachada de una bandeja con consumo registrado");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar el consumo de la bandeja " + nueva.getIdBandeja(), e);
        }
    }

    private void validarSinConsumo(int idBandeja) throws BLException {
        try {
            if (bandejaInstrumentalDAO.tieneConsumoRegistrado(idBandeja)) {
                throw new BLException("No se puede modificar la composición despachada de una bandeja con consumo registrado");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar el consumo de la bandeja " + idBandeja, e);
        }
    }

    private void validarCantidadDespachada(int cantidad) throws BLException {
        if (cantidad <= 0) {
            throw new BLException("La cantidad despachada debe ser mayor que cero");
        }
    }

    private BandejaInstrumental buscarBandeja(int idBandeja) throws BLException {
        if (idBandeja <= 0) {
            throw new BLException("El id de bandeja no es válido");
        }
        try {
            BandejaInstrumental bandeja = bandejaInstrumentalDAO.findById(idBandeja);
            if (bandeja == null) {
                throw new BLException("No existe una bandeja instrumental con id " + idBandeja);
            }
            return bandeja;
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la bandeja " + idBandeja, e);
        }
    }

    private BandejaInstrumental buscarBandejaActiva(int idBandeja) throws BLException {
        BandejaInstrumental bandeja = buscarBandeja(idBandeja);
        if (!bandeja.isActivo()) {
            throw new BLException("La bandeja " + idBandeja + " está dada de baja");
        }
        return bandeja;
    }

    // Consumible solo con id: Consumible.equals compara unicamente el id
    private Consumible consumibleConId(int idConsumible) {
        Consumible c = new Consumible();
        c.setIdConsumible(idConsumible);
        return c;
    }
}
