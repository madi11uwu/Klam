package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.ReporteBL;
import pe.edu.pucp.klam.bl.reportes.CirugiaComparativa;
import pe.edu.pucp.klam.bl.reportes.FiltroConsumo;
import pe.edu.pucp.klam.bl.reportes.LineaComparativa;
import pe.edu.pucp.klam.bl.reportes.ReporteComparativo;
import pe.edu.pucp.klam.bl.reportes.ReporteUsoInsumos;
import pe.edu.pucp.klam.bl.reportes.ResumenUsoInsumo;
import pe.edu.pucp.klam.dao.ReporteConsumoDAO;
import pe.edu.pucp.klam.dao.impl.reportes.ReporteConsumoDAOImpl;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReporteBLImpl implements ReporteBL {

    private static final String CLINICA = "CLINICA_HOSPITAL";
    private static final String PARTICULAR = "PACIENTE_PARTICULAR";

    private final ReporteConsumoDAO reporteDAO = new ReporteConsumoDAOImpl();

    @Override
    public ReporteUsoInsumos usoInsumos(FiltroConsumo filtro) throws BLException {
        validar(filtro);
        List<Map<String, Object>> filas = consultar(filtro);

        Map<String, Acumulado> grupos = new LinkedHashMap<>();
        Set<Integer> cirugias = new HashSet<>();
        for (Map<String, Object> fila : filas) {
            int idCirugia = entero(fila.get("idCirugia"));
            int consumida = entero(fila.get("cantidadConsumida"));
            int idConsumible = entero(fila.get("idConsumible"));
            String tipo = texto(fila.get("tipoProcedimiento"));
            cirugias.add(idCirugia);

            // Un grupo por insumo y tipo de cirugia. Se conservan los grupos con 0 consumido
            // (insumo despachado pero no utilizado) para que el reporte refleje todo lo despachado.
            Acumulado grupo = grupos.computeIfAbsent(idConsumible + "|" + tipo,
                    k -> new Acumulado(texto(fila.get("nombreComercial")), texto(fila.get("medida")), tipo));
            grupo.total += consumida;
            if (consumida > 0) {
                grupo.cirugias.add(idCirugia);
            }
        }

        List<ResumenUsoInsumo> insumos = grupos.values().stream()
                .map(g -> new ResumenUsoInsumo(g.nombre, g.medida, g.tipo, g.total, g.cirugias.size()))
                .sorted(Comparator.comparingInt(ResumenUsoInsumo::cantidadUtilizada).reversed()
                        .thenComparing(ResumenUsoInsumo::nombre))
                .toList();

        return new ReporteUsoInsumos(ahora(), filtro, cirugias.size(), insumos);
    }

    @Override
    public ReporteComparativo comparativo(FiltroConsumo filtro) throws BLException {
        validar(filtro);
        List<Map<String, Object>> filas = consultar(filtro);

        Map<Integer, Map<String, Object>> cabeceras = new LinkedHashMap<>();
        Map<Integer, List<LineaComparativa>> lineas = new LinkedHashMap<>();
        for (Map<String, Object> fila : filas) {
            int idCirugia = entero(fila.get("idCirugia"));
            cabeceras.putIfAbsent(idCirugia, fila);
            int despachada = entero(fila.get("cantidadDespachada"));
            int consumida = entero(fila.get("cantidadConsumida"));
            lineas.computeIfAbsent(idCirugia, k -> new ArrayList<>()).add(new LineaComparativa(
                    texto(fila.get("nombreComercial")),
                    texto(fila.get("medida")),
                    despachada,
                    consumida,
                    despachada - consumida));
        }

        List<CirugiaComparativa> cirugias = new ArrayList<>();
        for (Map.Entry<Integer, Map<String, Object>> entrada : cabeceras.entrySet()) {
            Map<String, Object> c = entrada.getValue();
            cirugias.add(new CirugiaComparativa(
                    entrada.getKey(),
                    (LocalDateTime) c.get("fechaHoraInicio"),
                    texto(c.get("tipoProcedimiento")),
                    texto(c.get("pacienteNombre")),
                    texto(c.get("pacienteDocumento")),
                    texto(c.get("doctorNombre")),
                    texto(c.get("tipoCliente")),
                    texto(c.get("clienteNombre")),
                    lineas.get(entrada.getKey())));
        }

        return new ReporteComparativo(ahora(), filtro, cirugias);
    }

    // ---------- Validacion y consulta ----------

    private void validar(FiltroConsumo f) throws BLException {
        if (f == null) {
            throw new BLException("Debe indicar los criterios del reporte");
        }
        if (f.desde() != null && f.hasta() != null && f.desde().isAfter(f.hasta())) {
            throw new BLException("El inicio del periodo no puede ser posterior al fin");
        }
        if (f.idCirugia() != null && f.idCirugia() <= 0) {
            throw new BLException("El id de cirugía no es válido");
        }
        if (f.idConsumible() != null && f.idConsumible() <= 0) {
            throw new BLException("El id de consumible no es válido");
        }
        if (f.tipoCliente() != null && !CLINICA.equals(f.tipoCliente()) && !PARTICULAR.equals(f.tipoCliente())) {
            throw new BLException("El tipo de cliente no es válido");
        }
        if (f.idCliente() != null) {
            if (f.tipoCliente() == null) {
                throw new BLException("Debe indicar el tipo de cliente junto con el cliente");
            }
            if (f.idCliente() <= 0) {
                throw new BLException("El id de cliente no es válido");
            }
        }
    }

    private List<Map<String, Object>> consultar(FiltroConsumo f) throws BLException {
        LocalDateTime desde = f.desde() == null ? null : f.desde().atStartOfDay();
        LocalDateTime hasta = f.hasta() == null ? null : f.hasta().atTime(23, 59, 59);
        String medida = f.medida() == null || f.medida().isBlank() ? null : f.medida().trim();
        String tipo = f.tipoProcedimiento() == null || f.tipoProcedimiento().isBlank()
                ? null : f.tipoProcedimiento().trim();
        try {
            return reporteDAO.buscarFilasConsumo(f.idCirugia(), f.tipoCliente(), f.idCliente(),
                    desde, hasta, tipo, f.idConsumible(), medida);
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar los consumos para el reporte", e);
        }
    }

    // ---------- Utilidades ----------

    private static LocalDateTime ahora() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    private static int entero(Object valor) {
        return valor instanceof Number n ? n.intValue() : 0;
    }

    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private static final class Acumulado {
        private final String nombre;
        private final String medida;
        private final String tipo;
        private int total;
        private final Set<Integer> cirugias = new HashSet<>();

        private Acumulado(String nombre, String medida, String tipo) {
            this.nombre = nombre;
            this.medida = medida;
            this.tipo = tipo;
        }
    }
}