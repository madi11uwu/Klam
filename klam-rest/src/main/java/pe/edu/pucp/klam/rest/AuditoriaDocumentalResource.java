package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import pe.edu.pucp.klam.bl.AuditoriaDocumentalBL;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.impl.AuditoriaDocumentalBLImpl;

/** RF04: consulta administrativa de eventos documentales; no admite escritura REST. */
@Path("/auditoria/documental")
@Produces(MediaType.APPLICATION_JSON)
public class AuditoriaDocumentalResource {
    @Context private HttpHeaders headers;
    private final AuditoriaDocumentalBL auditoriaBL = new AuditoriaDocumentalBLImpl();

    @GET
    public Response buscar(@QueryParam("cliente") String cliente,
            @QueryParam("paciente") String paciente,
            @QueryParam("desde") String desde, @QueryParam("hasta") String hasta,
            @QueryParam("tipoDocumento") String tipoDocumento,
            @QueryParam("accion") String accion,
            @QueryParam("responsable") String responsable,
            @QueryParam("idSolicitud") String idSolicitud,
            @QueryParam("idCirugia") String idCirugia,
            @QueryParam("pagina") String pagina, @QueryParam("tamano") String tamano) {
        Response acceso = acceso();
        if (acceso != null) return acceso;
        try {
            LocalDateTime inicio = RespuestaRest.fechaDesde(desde, "desde");
            LocalDateTime fin = RespuestaRest.fechaHasta(hasta, "hasta");
            return RespuestaRest.ok(auditoriaBL.buscar(
                    RespuestaRest.vacioANull(cliente), RespuestaRest.vacioANull(paciente),
                    inicio, fin, RespuestaRest.vacioANull(tipoDocumento),
                    RespuestaRest.vacioANull(accion), RespuestaRest.vacioANull(responsable),
                    RespuestaRest.enteroOpcional(idSolicitud, "idSolicitud"),
                    RespuestaRest.enteroOpcional(idCirugia, "idCirugia"),
                    pagina == null ? 1 : Integer.parseInt(pagina),
                    tamano == null ? 20 : Integer.parseInt(tamano)));
        } catch (NumberFormatException | BLException ex) {
            if (ex instanceof BLException && ex.getCause() instanceof java.sql.SQLException)
                return RespuestaRest.error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo consultar la bitácora documental");
            return RespuestaRest.badRequest(ex.getMessage());
        }
    }

    @GET
    @Path("/{id}")
    public Response obtener(@PathParam("id") String id) {
        Response acceso = acceso();
        if (acceso != null) return acceso;
        try {
            var evento = auditoriaBL.obtener(Integer.parseInt(id));
            return evento == null ? RespuestaRest.notFound("Evento documental no encontrado")
                    : RespuestaRest.ok(evento);
        } catch (NumberFormatException | BLException ex) {
            if (ex instanceof BLException && ex.getCause() instanceof java.sql.SQLException)
                return RespuestaRest.error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo consultar el evento documental");
            return RespuestaRest.badRequest(ex.getMessage());
        }
    }

    private Response acceso() {
        SesionesRest.Sesion sesion = SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (sesion == null) return Response.status(Response.Status.UNAUTHORIZED).build();
        return "ADMINISTRADOR".equals(sesion.rol()) ? null
                : Response.status(Response.Status.FORBIDDEN).build();
    }
}
