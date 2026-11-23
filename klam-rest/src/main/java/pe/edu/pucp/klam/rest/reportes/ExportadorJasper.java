package pe.edu.pucp.klam.rest.reportes;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import pe.edu.pucp.klam.bl.reportes.CirugiaComparativa;
import pe.edu.pucp.klam.bl.reportes.LineaComparativa;
import pe.edu.pucp.klam.bl.reportes.ReporteComparativo;
import pe.edu.pucp.klam.bl.reportes.ReporteUsoInsumos;
import pe.edu.pucp.klam.bl.reportes.ResumenUsoInsumo;
import pe.edu.pucp.klam.db.DBManager;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Exporta RF18 con datos de BL y RF19 con su subreporte por cirugía. */
public final class ExportadorJasper {

    // Plantillas de Studio compiladas e incluidas en el módulo de reportes.
    private static final String USO_INSUMOS = "reportes/ReporteUso_Insumo.jrxml";
    private static final String COMPARATIVO = "reportes/Reporte_DespachoConsumoFinal.jasper";
    // El maestro RF19 requiere la ruta del subreporte ya compilado.
    private static final String COMPARATIVO_DETALLE = "reportes/Reporte_DetalleDespachoConsumoFinal.jasper";

    // Plantillas de respaldo para el caso sin datos.
    private static final String USO_INSUMOS_VACIO = "reportes/reporte_uso_insumos.jrxml";
    private static final String COMPARATIVO_VACIO = "reportes/reporte_comparativo.jrxml";
    private static final String COMPARATIVO_VACIO_LINEAS = "reportes/reporte_comparativo_lineas.jrxml";

    private static final String RUTA_LOGO = "imagenes/bioklam.png";

    private static final Map<String, JasperReport> CACHE = new ConcurrentHashMap<>();
    private static Image logoCache;

    static {
        DefaultJasperReportsContext.getInstance().setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
    }

    private ExportadorJasper() {
    }

    // ---------- RF18: uso de insumos ----------

    public static byte[] usoInsumos(ReporteUsoInsumos reporte, CabeceraReporte cabecera) throws IOException {
        List<ResumenUsoInsumo> insumos = reporte.insumos() == null ? List.of() : reporte.insumos();
        if (insumos.isEmpty()) {
            return usoInsumosSinDatos(cabecera);
        }
        try {
            JasperReport jr = plantillaFuente(USO_INSUMOS);

            Collection<Map<String, ?>> filas = new ArrayList<>();
            for (ResumenUsoInsumo insumo : insumos) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("insumo", insumo.nombre());
                fila.put("medida", insumo.medida());
                fila.put("tipo_cirugia", insumo.tipoProcedimiento());
                fila.put("cantidad_utilizada", (long) insumo.cantidadUtilizada());
                fila.put("frecuencia", (long) insumo.frecuencia());
                filas.add(fila);
            }

            Map<String, Object> parametros = construirParametrosCabecera(cabecera);

            JasperPrint jp = JasperFillManager.fillReport(jr, parametros, new JRMapCollectionDataSource(filas));
            return JasperExportManager.exportReportToPdf(jp);
        } catch (JRException e) {
            throw new IOException("Error al generar el PDF de uso de insumos con JasperReports: " + e.getMessage(), e);
        }
    }

    // ---------- RF19: comparativo por cirugia ----------

    public static byte[] comparativo(ReporteComparativo reporte, CabeceraReporte cabecera) throws IOException {
        List<CirugiaComparativa> cirugias = reporte.cirugias() == null ? List.of() : reporte.cirugias();
        if (cirugias.isEmpty()) {
            return comparativoSinDatos(reporte, cabecera);
        }
        try {
            JasperReport maestro = plantillaFuente(COMPARATIVO);
            JasperPrint total = null;

            try (Connection conexion = conexion()) {
                for (CirugiaComparativa cirugia : cirugias) {
                    Map<String, Object> parametros = new HashMap<>();
                    parametros.put("id_cirugia", cirugia.idCirugia());
                    parametros.put("subreporte", COMPARATIVO_DETALLE);
                    parametros.put("logo", obtenerLogo());

                    JasperPrint jp = JasperFillManager.fillReport(maestro, parametros, conexion);
                    if (total == null) {
                        total = jp;
                    } else {
                        for (JRPrintPage pagina : jp.getPages()) {
                            total.addPage(pagina);
                        }
                    }
                }
            } catch (SQLException e) {
                throw new IOException("Error de base de datos al generar el PDF comparativo: " + e.getMessage(), e);
            }

            if (total == null || total.getPages().isEmpty()) {
                return comparativoSinDatos(reporte, cabecera);
            }
            return JasperExportManager.exportReportToPdf(total);
        } catch (JRException e) {
            throw new IOException("Error al generar el PDF comparativo con JasperReports: " + e.getMessage(), e);
        }
    }

    // ---------- Respaldo cuando el reporte sale vacio ----------

    private static byte[] usoInsumosSinDatos(CabeceraReporte cabecera) throws IOException {
        try {
            JasperReport jr = plantillaFuente(USO_INSUMOS_VACIO);
            JasperPrint jp = JasperFillManager.fillReport(jr, construirParametrosCabecera(cabecera),
                    new JRMapCollectionDataSource(new ArrayList<>()));
            return JasperExportManager.exportReportToPdf(jp);
        } catch (JRException e) {
            throw new IOException("Error al generar el PDF de uso de insumos sin datos: " + e.getMessage(), e);
        }
    }

    private static byte[] comparativoSinDatos(ReporteComparativo reporte, CabeceraReporte cabecera) throws IOException {
        try {
            JasperReport jrMaestro = plantillaFuente(COMPARATIVO_VACIO);
            Map<String, Object> parametros = construirParametrosCabecera(cabecera);
            parametros.put("subreporteLineas", plantillaFuente(COMPARATIVO_VACIO_LINEAS));

            Collection<Map<String, ?>> filas = new ArrayList<>();
            if (reporte.cirugias() != null) {
                for (CirugiaComparativa c : reporte.cirugias()) {
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("idCirugia", c.idCirugia());
                    fila.put("fecha", formatearFecha(c.fecha()));
                    fila.put("procedimiento", c.procedimiento());
                    fila.put("paciente", c.paciente());
                    fila.put("documentoPaciente", c.documentoPaciente());
                    fila.put("medico", c.medico());
                    fila.put("cliente", c.cliente());

                    List<Map<String, Object>> lineasMap = new ArrayList<>();
                    if (c.lineas() != null) {
                        for (LineaComparativa l : c.lineas()) {
                            Map<String, Object> filaLinea = new HashMap<>();
                            filaLinea.put("nombre", l.nombre());
                            filaLinea.put("medida", l.medida());
                            filaLinea.put("despachada", l.despachada());
                            filaLinea.put("consumida", l.consumida());
                            filaLinea.put("diferencia", l.diferencia());
                            lineasMap.add(filaLinea);
                        }
                    }
                    fila.put("lineas", lineasMap);
                    filas.add(fila);
                }
            }

            JasperPrint jp = JasperFillManager.fillReport(jrMaestro, parametros, new JRMapCollectionDataSource(filas));
            return JasperExportManager.exportReportToPdf(jp);
        } catch (JRException e) {
            throw new IOException("Error al generar el PDF comparativo sin datos: " + e.getMessage(), e);
        }
    }

    // ---------- Auxiliares ----------

    private static Map<String, Object> construirParametrosCabecera(CabeceraReporte cabecera) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("titulo", cabecera.titulo());
        parametros.put("generado", cabecera.generado());
        parametros.put("periodo", cabecera.periodo());
        parametros.put("criterios", String.join("\n", cabecera.criterios()));
        parametros.put("logo", obtenerLogo());
        return parametros;
    }

    /** DBManager lanza RuntimeException si no puede conectar; aqui se convierte para que quede en el log. */
    private static Connection conexion() throws IOException {
        try {
            return DBManager.getInstance().getConnection();
        } catch (RuntimeException e) {
            throw new IOException("No se pudo conectar a la base de datos para el reporte comparativo: "
                    + e.getMessage(), e);
        }
    }

    /** Plantilla en .jrxml, que se compila la primera vez que se usa. */
    private static JasperReport plantillaFuente(String ruta) throws JRException, IOException {
        JasperReport enCache = CACHE.get(ruta);
        if (enCache != null) return enCache;
        try (InputStream is = recurso(ruta)) {
            JasperReport jr = ruta.endsWith(".jasper")
                    ? (JasperReport) net.sf.jasperreports.engine.util.JRLoader.loadObject(is)
                    : JasperCompileManager.compileReport(is);
            CACHE.put(ruta, jr);
            return jr;
        }
    }

    private static java.util.Date aFecha(java.time.LocalDate d) {
        return java.util.Date.from(d.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
    }

    private static InputStream recurso(String ruta) throws IOException {
        InputStream is = ExportadorJasper.class.getClassLoader().getResourceAsStream(ruta);
        if (is == null) {
            throw new FileNotFoundException("No se encontró el recurso del reporte en classpath: " + ruta);
        }
        return is;
    }

    /**
     * Logo de la cabecera. Nunca devuelve null: un parametro de imagen vacio corta
     * el llenado del reporte y el PDF no llegaria a generarse, asi que si el recurso
     * falta se devuelve un pixel transparente y el reporte sale sin logo.
     */
    private static synchronized Image obtenerLogo() {
        if (logoCache == null) {
            try (InputStream is = ExportadorJasper.class.getClassLoader().getResourceAsStream(RUTA_LOGO)) {
                logoCache = is == null ? null : ImageIO.read(is);
            } catch (IOException e) {
                logoCache = null;
            }
            if (logoCache == null) {
                logoCache = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            }
        }
        return logoCache;
    }

    private static String formatearFecha(LocalDateTime f) {
        return f == null ? "—" : String.format("%02d/%02d/%d", f.getDayOfMonth(), f.getMonthValue(), f.getYear());
    }
}
