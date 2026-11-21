package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.BandejaInstrumentalBL;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.ConsumoBL;
import pe.edu.pucp.klam.bl.DocumentoIngresoBL;
import pe.edu.pucp.klam.bl.NoEncontradoException;
import pe.edu.pucp.klam.bl.ValidacionException;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.TipoDocumentoIngreso;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConsumoBLImpl implements ConsumoBL {
    private static final String CLINICA = "CLINICA_HOSPITAL";

    private static final String OC_NO_APLICA = "NO_APLICA";
    private static final String OC_POR_ADJUNTAR = "POR_ADJUNTAR";

    private static final String CONSUMO_NO_DISPONIBLE = "NO_DISPONIBLE";
    private static final String CONSUMO_POR_REGISTRAR = "POR_REGISTRAR";
    private static final String CONSUMO_COMPLETO = "COMPLETO";

    private final CirugiaBL cirugiaBL = new CirugiaBLImpl();
    private final BandejaInstrumentalBL bandejaBL = new BandejaInstrumentalBLImpl();
    private final DocumentoIngresoBL documentoBL = new DocumentoIngresoBLImpl();

    @Override
    public List<Map<String, Object>> listar(String texto, LocalDateTime desde, LocalDateTime hasta) throws BLException {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ValidacionException("El inicio del rango no puede ser posterior al fin");
        }
        String filtro = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);

        List<Map<String, Object>> filas = new ArrayList<>();
        for (Map<String, Object> cirugia : cirugiaBL.buscarDetalle(desde, hasta, null, null, null, EstadoCirugia.FINALIZADA)) {
            if (!coincide(cirugia, filtro)) {
                continue;
            }
            filas.add(resumir(cirugia));
        }
        return filas;
    }

    @Override
    public Map<String, Object> obtener(int idCirugia) throws BLException {
        validarId(idCirugia);
        Cirugia existente = cirugiaBL.findById(idCirugia);
        if (existente == null) {
            throw new NoEncontradoException("No existe una cirugía con id " + idCirugia);
        }
        if (existente.getEstado() != EstadoCirugia.FINALIZADA) {
            throw new ValidacionException("El consumo solo se consulta con la cirugía FINALIZADA (estado actual: "
                    + existente.getEstado().name() + ")");
        }

        Map<String, Object> detalle = cirugiaBL.obtenerDetalle(idCirugia);
        Map<String, Object> fila = resumir(detalle);
        fila.put("doctorNombre", detalle.get("doctorNombre"));
        fila.put("pacienteNombre", detalle.get("pacienteNombre"));
        fila.put("clinicaNombre", detalle.get("clinicaNombre"));

        DocumentoIngreso oc = ordenCompraVigente(idCirugia);
        fila.put("archivoOrdenCompra", oc == null ? null : oc.getArchivoPath());
        fila.put("motivoRechazoOrdenCompra", oc == null ? null : oc.getMotivoRechazo());

        List<Map<String, Object>> consumibles = new ArrayList<>();
        long despachado = 0;
        long consumido = 0;
        for (Map<String, Object> c : bandejaBL.consultarConsumosPorCirugia(idCirugia)) {
            int cantDespachada = entero(c.get("cantidadDespachada"));
            int cantConsumida = entero(c.get("cantidadConsumida"));
            Map<String, Object> linea = new LinkedHashMap<>();
            linea.put("idConsumible", c.get("idConsumible"));
            linea.put("nombre", c.get("nombreComercial"));
            linea.put("medida", c.get("medida"));
            linea.put("cantidadDespachada", cantDespachada);
            linea.put("cantidadConsumida", cantConsumida);
            linea.put("diferencia", cantDespachada - cantConsumida);
            consumibles.add(linea);
            despachado += cantDespachada;
            consumido += cantConsumida;
        }
        fila.put("consumibles", consumibles);
        fila.put("totalDespachado", despachado);
        fila.put("totalConsumido", consumido);
        fila.put("diferencia", despachado - consumido);
        return fila;
    }

    @Override
    public void guardar(int idCirugia, Map<Integer, Integer> consumosPorConsumible, boolean confirmar) throws BLException {
        validarId(idCirugia);
        if (cirugiaBL.findById(idCirugia) == null) {
            throw new NoEncontradoException("No existe una cirugía con id " + idCirugia);
        }
        bandejaBL.guardarConsumo(idCirugia, consumosPorConsumible, confirmar);
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    // Fila de listado a partir de una fila de CirugiaBL.buscarDetalle / obtenerDetalle
    private Map<String, Object> resumir(Map<String, Object> cirugia) throws BLException {
        int idCirugia = entero(cirugia.get("idCirugia"));
        boolean clinica = CLINICA.equals(cirugia.get("tipoCliente"));
        boolean documentacionCompleta = documentoBL.verificarDocumentacionCierre(idCirugia);
        boolean consumoCompleto = Boolean.TRUE.equals(cirugia.get("consumoCompleto"))
                || (cirugia.get("consumoCompleto") instanceof Number n && n.intValue() == 1);

        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("idCirugia", idCirugia);
        fila.put("fechaHoraInicio", cirugia.get("fechaHoraInicio"));
        fila.put("tipoProcedimiento", cirugia.get("tipoProcedimiento"));
        fila.put("tipoCliente", cirugia.get("tipoCliente"));
        fila.put("clienteNombre", cirugia.get("clienteNombre"));
        fila.put("idBandeja", cirugia.get("idBandeja"));
        fila.put("requiereOrdenCompra", clinica);
        fila.put("estadoOrdenCompra", clinica ? estadoOrdenCompra(idCirugia) : OC_NO_APLICA);
        fila.put("documentacionCierreCompleta", documentacionCompleta);
        boolean tieneDespachoConsumibles = cirugia.get("idBandeja") != null
                && !bandejaBL.consultarConsumosPorCirugia(idCirugia).isEmpty();
        fila.put("estadoConsumo", estadoConsumo(consumoCompleto, documentacionCompleta, tieneDespachoConsumibles));
        return fila;
    }

    private String estadoConsumo(boolean completo, boolean documentacionCompleta, boolean tieneBandeja) {
        if (completo) {
            return CONSUMO_COMPLETO;
        }
        return documentacionCompleta && tieneBandeja ? CONSUMO_POR_REGISTRAR : CONSUMO_NO_DISPONIBLE;
    }

    private String estadoOrdenCompra(int idCirugia) throws BLException {
        DocumentoIngreso oc = ordenCompraVigente(idCirugia);
        return oc == null ? OC_POR_ADJUNTAR : oc.getEstadoValidacion().name();
    }

    // La orden de compra vigente es la de mayor versión (una OC rechazada se vuelve a subir como versión nueva)
    private DocumentoIngreso ordenCompraVigente(int idCirugia) throws BLException {
        return documentoBL.listarPorCirugia(idCirugia).stream()
                .filter(d -> d.getTipoDocumento() == TipoDocumentoIngreso.ORDEN_COMPRA)
                .max(Comparator.comparingInt(DocumentoIngreso::getVersion))
                .orElse(null);
    }

    private boolean coincide(Map<String, Object> cirugia, String filtro) {
        if (filtro.isEmpty()) {
            return true;
        }
        String codigo = String.format("CIR-%03d", entero(cirugia.get("idCirugia"))).toLowerCase(Locale.ROOT);
        String cliente = String.valueOf(cirugia.get("clienteNombre")).toLowerCase(Locale.ROOT);
        return codigo.contains(filtro) || cliente.contains(filtro);
    }

    private void validarId(int idCirugia) throws BLException {
        if (idCirugia <= 0) {
            throw new ValidacionException("El id de la cirugía no es válido");
        }
    }

    private static int entero(Object valor) {
        return valor instanceof Number n ? n.intValue() : 0;
    }
}
