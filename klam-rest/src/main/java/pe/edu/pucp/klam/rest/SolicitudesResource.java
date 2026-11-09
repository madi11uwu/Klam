package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.SolicitudCirugiaBL;
import pe.edu.pucp.klam.bl.impl.SolicitudCirugiaBLImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.SolicitudCirugia;
import pe.edu.pucp.klam.rest.dto.AccionSolicitudDTO;
import pe.edu.pucp.klam.rest.dto.SolicitudCirugiaDTO;

@Path("/solicitudes")
@Produces("application/json")
@Consumes("application/json")
public class SolicitudesResource {

    private final SolicitudCirugiaBL solicitudBL = new SolicitudCirugiaBLImpl();

    @Context
    private HttpHeaders headers;

    @GET
    public Response listar(@QueryParam("estado") String estado) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
        try {
            return Response.ok(solicitudBL.listarSolicitudes(estado).stream()
                    .map(SolicitudCirugiaDTO::new).toList()).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Path("/{id}")
    public Response consultar(@PathParam("id") int id) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
        if (id <= 0) return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        try {
            SolicitudCirugia solicitud = solicitudBL.findSolicitudById(id);
            if (solicitud == null) return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            return Response.ok(new SolicitudCirugiaDTO(solicitud)).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/{id}/aprobacion")
    public Response aprobar(@PathParam("id") int id) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
        if (id <= 0) return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        try {
            if (solicitudBL.findSolicitudById(id) == null) {
                return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            }
            solicitudBL.aprobarSolicitud(id);
            return Response.noContent().build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        }
    }

    @POST
    @Path("/{id}/rechazo")
    public Response rechazar(@PathParam("id") int id, AccionSolicitudDTO datos) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
        if (id <= 0) return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        if (datos == null || datos.getMotivo() == null || datos.getMotivo().isBlank()
                || datos.getMotivo().length() > 200) {
            return error(Response.Status.BAD_REQUEST, "El motivo de rechazo debe tener entre 1 y 200 caracteres");
        }
        try {
            if (solicitudBL.findSolicitudById(id) == null) {
                return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            }
            solicitudBL.rechazarSolicitud(id, datos.getMotivo());
            return Response.noContent().build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        }
    }

    @POST
    @Path("/{id}/cirugia")
    public Response registrarCirugia(@PathParam("id") int id) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
        if (id <= 0) return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        try {
            if (solicitudBL.findSolicitudById(id) == null) {
                return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            }
            int idCirugia = solicitudBL.registrarCirugiaDesdeSolicitud(id, null, sesion.username());
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("idCirugia", idCirugia))
                    .build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        }
    }

    private SesionesRest.Sesion sesion() {
        return SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
    }

    private boolean esAdministrador(SesionesRest.Sesion sesion) {
        return "ADMINISTRADOR".equals(sesion.rol());
    }

    private Response noAutenticado() {
        return error(Response.Status.UNAUTHORIZED, "Sesión inválida o expirada");
    }

    private Response sinPermiso() {
        return error(Response.Status.FORBIDDEN, "No tiene permisos de administrador");
    }

    private Response fallo(BLException ex, Response.Status estadoNegocio) {
        return error(ex.getCause() == null ? estadoNegocio : Response.Status.INTERNAL_SERVER_ERROR,
                ex.getMessage());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(Map.of("error", mensaje)).build();
    }
}
