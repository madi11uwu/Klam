package pe.edu.pucp.klam.rest.reportes;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
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

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Genera los reportes RF18 y RF19 en PDF usando JasperReports 7.0.3.
 */
public final class ExportadorJasper {

    private static final String RUTA_USO_INSUMOS = "reportes/reporte_uso_insumos.jrxml";
    private static final String RUTA_COMPARATIVO = "reportes/reporte_comparativo.jrxml";
    private static final String RUTA_COMPARATIVO_LINEAS = "reportes/reporte_comparativo_lineas.jrxml";
    private static final String RUTA_LOGO = "imagenes/bioklam-logo.png";

    private static Image logoCache;
    private static JasperReport reporteUsoCache;
    private static JasperReport reporteComparativoCache;
    private static JasperReport reporteComparativoLineasCache;

    static {
        DefaultJasperReportsContext.getInstance().setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
    }

    private ExportadorJasper() {
    }

    public static byte[] usoInsumos(ReporteUsoInsumos reporte, CabeceraReporte cabecera) throws IOException {
        try {
            JasperReport jr = obtenerReporteUso();
            Map<String, Object> parametros = construirParametrosCabecera(cabecera);

            Collection<Map<String, ?>> filas = new ArrayList<>();
            if (reporte.insumos() != null) {
                for (ResumenUsoInsumo insumo : reporte.insumos()) {
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("nombre", insumo.nombre());
                    fila.put("medida", insumo.medida());
                    fila.put("tipoProcedimiento", insumo.tipoProcedimiento());
                    fila.put("cantidadUtilizada", insumo.cantidadUtilizada());
                    fila.put("frecuencia", insumo.frecuencia());
                    filas.add(fila);
                }
            }

            JRMapCollectionDataSource ds = new JRMapCollectionDataSource(filas);
            JasperPrint jp = JasperFillManager.fillReport(jr, parametros, ds);
            return JasperExportManager.exportReportToPdf(jp);
        } catch (JRException e) {
            throw new IOException("Error al generar el PDF de uso de insumos con JasperReports: " + e.getMessage(), e);
        }
    }

    public static byte[] comparativo(ReporteComparativo reporte, CabeceraReporte cabecera) throws IOException {
        try {
            JasperReport jrMaestro = obtenerReporteComparativo();
            JasperReport jrLineas = obtenerReporteComparativoLineas();

            Map<String, Object> parametros = construirParametrosCabecera(cabecera);
            parametros.put("subreporteLineas", jrLineas);

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

            JRMapCollectionDataSource ds = new JRMapCollectionDataSource(filas);
            JasperPrint jp = JasperFillManager.fillReport(jrMaestro, parametros, ds);
            return JasperExportManager.exportReportToPdf(jp);
        } catch (JRException e) {
            throw new IOException("Error al generar el PDF comparativo con JasperReports: " + e.getMessage(), e);
        }
    }

    private static Map<String, Object> construirParametrosCabecera(CabeceraReporte cabecera) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("titulo", cabecera.titulo());
        parametros.put("generado", cabecera.generado());
        parametros.put("periodo", cabecera.periodo());
        parametros.put("criterios", String.join("\n", cabecera.criterios()));
        parametros.put("logo", obtenerLogo());
        return parametros;
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

    private static synchronized JasperReport obtenerReporteUso() throws JRException, IOException {
        if (reporteUsoCache == null) {
            reporteUsoCache = compilarReporte(RUTA_USO_INSUMOS);
        }
        return reporteUsoCache;
    }

    private static synchronized JasperReport obtenerReporteComparativo() throws JRException, IOException {
        if (reporteComparativoCache == null) {
            reporteComparativoCache = compilarReporte(RUTA_COMPARATIVO);
        }
        return reporteComparativoCache;
    }

    private static synchronized JasperReport obtenerReporteComparativoLineas() throws JRException, IOException {
        if (reporteComparativoLineasCache == null) {
            reporteComparativoLineasCache = compilarReporte(RUTA_COMPARATIVO_LINEAS);
        }
        return reporteComparativoLineasCache;
    }

    private static JasperReport compilarReporte(String ruta) throws JRException, IOException {
        try (InputStream is = ExportadorJasper.class.getClassLoader().getResourceAsStream(ruta)) {
            if (is == null) {
                throw new FileNotFoundException("No se encontró el recurso del reporte en classpath: " + ruta);
            }
            return JasperCompileManager.compileReport(is);
        }
    }

    private static String formatearFecha(LocalDateTime f) {
        return f == null ? "—" : String.format("%02d/%02d/%d", f.getDayOfMonth(), f.getMonthValue(), f.getYear());
    }
}
