package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.impl.CirugiaBLImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;
import pe.edu.pucp.klam.rest.mapper.CirugiaConsultaMapper;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Consultas de cirugías (RF06 calendario y detalle, RF17 filtros, RF10 historial por cliente).
 *
 * GET /api/v1/cirugias                -> lista filtrada (CirugiaResumenDTO)
 * GET /api/v1/cirugias/calendario     -> cirugías de un mes (CirugiaResumenDTO)
 * GET /api/v1/cirugias/{id}           -> detalle (CirugiaDetalleDTO)
 */
@Path("/cirugias")
@Produces("application/json")
public class CirugiasResource {
    private final CirugiaBL cirugiaBL;

    public CirugiasResource() {
        this(new CirugiaBLImpl());
    }

    // Visibilidad de paquete a propósito: el contenedor REST solo usa el constructor público sin argumentos.
    CirugiasResource(CirugiaBL cirugiaBL) {
        this.cirugiaBL = cirugiaBL;
    }

    /**
     * Lista de cirugías con filtros combinables (todos opcionales):
     * desde / hasta (yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss), tipoCliente (CLINICA_HOSPITAL | PACIENTE_PARTICULAR),
     * idCliente (requiere tipoCliente), idEquipo y estado (PROGRAMADA, EN_PROCESO, FINALIZADA, CANCELADA
     * o su etiqueta: Programada, En curso, Culminada, Cancelada).
     * Sin resultados responde 200 con una lista vacía.
     */
    @GET
    public Response buscar(@QueryParam("desde") String desde,
                           @QueryParam("hasta") String hasta,
                           @QueryParam("tipoCliente") String tipoCliente,
                           @QueryParam("idCliente") Integer idCliente,
                           @QueryParam("idEquipo") Integer idEquipo,
                           @QueryParam("estado") String estado) {
        try {
            LocalDateTime inicio = parsearFecha(desde, false);
            LocalDateTime fin = parsearFecha(hasta, true);
            EstadoCirugia estadoCirugia = CirugiaConsultaMapper.parsearEstado(estado);
            String tipo = vacioANulo(tipoCliente);
            if (tipo != null) {
                tipo = tipo.toUpperCase(Locale.ROOT);
            }

            List<Map<String, Object>> filas =
                    cirugiaBL.buscarDetalle(inicio, fin, tipo, idCliente, idEquipo, estadoCirugia);
            return Response.ok(CirugiaConsultaMapper.toResumenDTOs(filas)).build();
        } catch (IllegalArgumentException ex) {
            return error(Response.Status.BAD_REQUEST, ex.getMessage());
        } catch (BLException ex) {
            return errorBL(ex);
        }
    }

    /** Cirugías de un mes. Si no se indican ano y mes se usa el mes actual. */
    @GET
    @Path("/calendario")
    public Response calendario(@QueryParam("anio") Integer anio,
                               @QueryParam("mes") Integer mes) {
        LocalDate hoy = LocalDate.now();
        int a = anio != null ? anio : hoy.getYear();
        int m = mes != null ? mes : hoy.getMonthValue();
        try {
            List<Map<String, Object>> filas = cirugiaBL.buscarCalendarioMensual(a, m);
            return Response.ok(CirugiaConsultaMapper.toResumenDTOs(filas)).build();
        } catch (DateTimeException ex) {
            return error(Response.Status.BAD_REQUEST, "Año o mes no válido: " + a + "/" + m);
        } catch (BLException ex) {
            return errorBL(ex);
        }
    }

    /** Detalle de una cirugía con su equipo (y marca), bandeja y consumibles. */
    @GET
    @Path("/{id: \\d+}")
    public Response detalle(@PathParam("id") int id) {
        try {
            // findById devuelve la cirugía con equipo, bandeja y cliente ya cargados; no filtra por
            // 'activo', así que una cirugía eliminada lógicamente se trata como inexistente.
            Cirugia cirugia = cirugiaBL.findById(id);
            if (cirugia == null || !cirugia.isActivo()) {
                return error(Response.Status.NOT_FOUND, "No existe una cirugía con id " + id);
            }
            Map<String, Object> detalle = cirugiaBL.obtenerDetalle(id);
            return Response.ok(CirugiaConsultaMapper.toDetalleDTO(
                    detalle, cirugia.getEquipo(), cirugia.getBandejaInstrumental())).build();
        } catch (BLException ex) {
            return errorBL(ex);
        }
    }

    private static LocalDateTime parsearFecha(String texto, boolean finDeDia) {
        String t = vacioANulo(texto);
        if (t == null) {
            return null;
        }
        try {
            if (t.length() <= 10) {
                LocalDate fecha = LocalDate.parse(t);
                return finDeDia ? fecha.atTime(23, 59, 59) : fecha.atStartOfDay();
            }
            return LocalDateTime.parse(t);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Fecha no válida: " + texto + " (use yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss)");
        }
    }

    private static String vacioANulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }


    private static Response errorBL(BLException ex) {
        Response.Status estado = ex.getCause() == null
                ? Response.Status.BAD_REQUEST
                : Response.Status.INTERNAL_SERVER_ERROR;
        return error(estado, ex.getMessage());
    }

    private static Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(Map.of("error", mensaje)).build();
    }
}
