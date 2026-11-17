package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.ReporteBL;
import pe.edu.pucp.klam.bl.impl.ConsumibleBLImpl;
import pe.edu.pucp.klam.bl.impl.ReporteBLImpl;
import pe.edu.pucp.klam.bl.reportes.FiltroConsumo;
import pe.edu.pucp.klam.bl.reportes.ReporteComparativo;
import pe.edu.pucp.klam.bl.reportes.ReporteUsoInsumos;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;
import pe.edu.pucp.klam.rest.mapper.ReporteMapper;
import pe.edu.pucp.klam.rest.reportes.CabeceraReporte;
import pe.edu.pucp.klam.rest.reportes.ExportadorExcel;
import pe.edu.pucp.klam.rest.reportes.ExportadorPdf;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Reportes de consumo (RF18 y RF19). Solo administradores. Todos los endpoints usan los mismos
 * criterios, de modo que la vista y el archivo exportado muestran los mismos datos.
 *
 * GET /reportes/uso-insumos?desde=&hasta=&tipoCirugia=&idConsumible=&medida=          JSON (RF18)
 * GET /reportes/uso-insumos/exportar?formato=excel|pdf&...                              archivo (RF18)
 * GET /reportes/comparativo?desde=&hasta=&idCirugia=&tipoCliente=&idCliente=           JSON (RF19)
 * GET /reportes/comparativo/exportar?formato=excel|pdf&...                             archivo (RF19)
 *
 * Fechas en formato AAAA-MM-DD. tipoCliente: CLINICA_HOSPITAL o PACIENTE_PARTICULAR.
 */
@Path("/reportes")
public class ReportesResource {

    private static final String EXCEL_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String PDF_MIME = "application/pdf";

    @Context private HttpHeaders headers;

    private final ReporteBL reporteBL = new ReporteBLImpl();

    private Response acceso() {
        SesionesRest.Sesion sesion = SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (sesion == null) return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "Sesión inválida o expirada")).build();
        if (!"ADMINISTRADOR".equals(sesion.rol())) return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("error", "Acceso denegado")).build();
        return null;
    }

    // ---------- RF18: uso de insumos ----------

    @GET
    @Path("/uso-insumos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response usoInsumos(@QueryParam("desde") String desde, @QueryParam("hasta") String hasta,
                               @QueryParam("tipoCirugia") String tipoCirugia,
                               @QueryParam("idConsumible") String idConsumible,
                               @QueryParam("medida") String medida) {
        Response acceso = acceso();
        if (acceso != null) return acceso;
        try {
            FiltroConsumo filtro = filtroUso(desde, hasta, tipoCirugia, idConsumible, medida);
            ReporteUsoInsumos reporte = reporteBL.usoInsumos(filtro);
            return Response.ok(ReporteMapper.toDTO(reporte, nombreConsumible(filtro.idConsumible())))
                    .header("Cache-Control", "no-store").build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    @GET
    @Path("/uso-insumos/exportar")
    public Response exportarUsoInsumos(@QueryParam("formato") String formato,
                                       @QueryParam("desde") String desde, @QueryParam("hasta") String hasta,
                                       @QueryParam("tipoCirugia") String tipoCirugia,
                                       @QueryParam("idConsumible") String idConsumible,
                                       @QueryParam("medida") String medida) {
        Response acceso = acceso();
        if (acceso != null) return acceso;
        try {
            boolean excel = esExcel(formato);
            FiltroConsumo filtro = filtroUso(desde, hasta, tipoCirugia, idConsumible, medida);
            ReporteUsoInsumos reporte = reporteBL.usoInsumos(filtro);
            CabeceraReporte cabecera = ReporteMapper.cabeceraUso(reporte, nombreConsumible(filtro.idConsumible()));
            return archivo(excel, "uso-insumos", filtro.desde(), filtro.hasta(),
                    () -> ExportadorExcel.usoInsumos(reporte, cabecera),
                    () -> ExportadorPdf.usoInsumos(reporte, cabecera));
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    // ---------- RF19: comparativo por cirugía ----------

    @GET
    @Path("/comparativo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response comparativo(@QueryParam("desde") String desde, @QueryParam("hasta") String hasta,
                                @QueryParam("idCirugia") String idCirugia,
                                @QueryParam("tipoCliente") String tipoCliente,
                                @QueryParam("idCliente") String idCliente) {
        Response acceso = acceso();
        if (acceso != null) return acceso;
        try {
            FiltroConsumo filtro = filtroComparativo(desde, hasta, idCirugia, tipoCliente, idCliente);
            return Response.ok(ReporteMapper.toDTO(reporteBL.comparativo(filtro)))
                    .header("Cache-Control", "no-store").build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    @GET
    @Path("/comparativo/exportar")
    public Response exportarComparativo(@QueryParam("formato") String formato,
                                        @QueryParam("desde") String desde, @QueryParam("hasta") String hasta,
                                        @QueryParam("idCirugia") String idCirugia,
                                        @QueryParam("tipoCliente") String tipoCliente,
                                        @QueryParam("idCliente") String idCliente) {
        Response acceso = acceso();
        if (acceso != null) return acceso;
        try {
            boolean excel = esExcel(formato);
            FiltroConsumo filtro = filtroComparativo(desde, hasta, idCirugia, tipoCliente, idCliente);
            ReporteComparativo reporte = reporteBL.comparativo(filtro);
            CabeceraReporte cabecera = ReporteMapper.cabeceraComparativo(reporte,
                    nombreCliente(filtro.idCliente(), reporte));
            return archivo(excel, "comparativo-consumo", filtro.desde(), filtro.hasta(),
                    () -> ExportadorExcel.comparativo(reporte, cabecera),
                    () -> ExportadorPdf.comparativo(reporte, cabecera));
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    // ---------- Auxiliares ----------

    private FiltroConsumo filtroUso(String desde, String hasta, String tipoCirugia,
                                    String idConsumible, String medida) throws BLException {
        return new FiltroConsumo(
                fecha(desde, "desde"), fecha(hasta, "hasta"),
                RespuestaRest.vacioANull(tipoCirugia),
                RespuestaRest.enteroOpcional(idConsumible, "idConsumible"),
                RespuestaRest.vacioANull(medida),
                null, null, null);
    }

    private FiltroConsumo filtroComparativo(String desde, String hasta, String idCirugia,
                                            String tipoCliente, String idCliente) throws BLException {
        return new FiltroConsumo(
                fecha(desde, "desde"), fecha(hasta, "hasta"),
                null, null, null,
                RespuestaRest.enteroOpcional(idCirugia, "idCirugia"),
                RespuestaRest.vacioANull(tipoCliente),
                RespuestaRest.enteroOpcional(idCliente, "idCliente"));
    }

    private static LocalDate fecha(String texto, String campo) throws BLException {
        String limpio = RespuestaRest.vacioANull(texto);
        if (limpio == null) {
            return null;
        }
        try {
            return LocalDate.parse(limpio);
        } catch (DateTimeParseException e) {
            throw new BLException(campo + " debe tener el formato AAAA-MM-DD", e);
        }
    }

    private static boolean esExcel(String formato) throws BLException {
        if ("excel".equalsIgnoreCase(formato)) return true;
        if ("pdf".equalsIgnoreCase(formato)) return false;
        throw new BLException("El formato debe ser 'excel' o 'pdf'");
    }

    private static Response archivo(boolean excel, String nombreBase, LocalDate desde, LocalDate hasta,
                                    Fabrica excelFabrica, Fabrica pdfFabrica) {
        try {
            byte[] datos = excel ? excelFabrica.generar() : pdfFabrica.generar();
            String extension = excel ? "xlsx" : "pdf";
            String periodo = (desde == null ? "inicio" : desde.toString()) + "_"
                    + (hasta == null ? "hoy" : hasta.toString());
            return Response.ok(datos, excel ? EXCEL_MIME : PDF_MIME)
                    .header("Content-Disposition", "attachment; filename=\"" + nombreBase + "_" + periodo + "." + extension + "\"")
                    .header("Cache-Control", "no-store")
                    .build();
        } catch (IOException e) {
            return Errores.error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo generar el archivo del reporte");
        }
    }

    /** Nombre del cliente para el encabezado: se toma de la primera cirugia del reporte. */
    private static String nombreCliente(Integer idCliente, ReporteComparativo reporte) {
        if (idCliente == null) return null;
        return reporte.cirugias().isEmpty() ? "ID " + idCliente : reporte.cirugias().get(0).cliente();
    }

    private String nombreConsumible(Integer idConsumible) {
        if (idConsumible == null) return null;
        try {
            Consumible c = new ConsumibleBLImpl().findById(idConsumible);
            return c == null ? "ID " + idConsumible : c.getNombreComercial();
        } catch (BLException e) {
            return "ID " + idConsumible;
        }
    }

    @FunctionalInterface
    private interface Fabrica {
        byte[] generar() throws IOException;
    }
}