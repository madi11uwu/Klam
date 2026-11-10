package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.BandejaInstrumentalBL;
import pe.edu.pucp.klam.bl.impl.BandejaInstrumentalBLImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.rest.dto.BandejaFormDTO;
import pe.edu.pucp.klam.rest.dto.ConsumibleBandejaDTO;
import pe.edu.pucp.klam.rest.mapper.BandejaMapper;

import java.net.URI;
import java.util.Map;

// RF13: plantillas de bandejas y sus consumibles por procedimiento. Las reglas están en BandejaInstrumentalBL.
@Path("/bandejas")
@Produces("application/json")
@Consumes("application/json")
public class BandejasResource {
    private final BandejaInstrumentalBL bandejaBL = new BandejaInstrumentalBLImpl();

    @Context
    private HttpHeaders headers;

    @Context
    private UriInfo uriInfo;

    // GET /bandejas o /bandejas?procedimiento=Cirugía de columna
    @GET
    public Response listar(@QueryParam("procedimiento") String procedimiento) {
        try {
            return Response.ok(BandejaMapper.toDTOs(procedimiento == null || procedimiento.isBlank()
                    ? bandejaBL.findAll()
                    : bandejaBL.listarPorProcedimiento(procedimiento))).build();
        } catch (BLException ex) {
            return fallo(ex);
        }
    }

    // Esterilizadas, activas y sin cirugía asignada
    @GET
    @Path("/disponibles")
    public Response disponibles(@QueryParam("procedimiento") String procedimiento) {
        try {
            return Response.ok(BandejaMapper.toDTOs(bandejaBL.listarDisponibles(procedimiento))).build();
        } catch (BLException ex) {
            return fallo(ex);
        }
    }

    @GET
    @Path("/{id}")
    public Response consultar(@PathParam("id") int id) {
        try {
            if (bandejaBL.findById(id) == null) {
                return error(Response.Status.NOT_FOUND, "No existe la bandeja " + id);
            }
            return Response.ok(BandejaMapper.toDTO(bandejaBL.consultarDetalle(id))).build();
        } catch (BLException ex) {
            return fallo(ex);
        }
    }

    // Crea una plantilla nueva (sin consumibles; luego se asocian)
    @POST
    public Response registrar(BandejaFormDTO datos) {
        if (!esAdministrador()) {
            return sinPermiso();
        }
        if (datos == null) {
            return error(Response.Status.BAD_REQUEST, "Debe enviar los datos de la bandeja");
        }
        try {
            BandejaInstrumental bandeja = BandejaMapper.toModelo(datos);
            bandejaBL.insert(bandeja);

            URI uri = uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(bandeja.getIdBandeja()))
                    .build();
            return Response.created(uri)
                    .entity(BandejaMapper.toDTO(bandejaBL.consultarDetalle(bandeja.getIdBandeja())))
                    .build();
        } catch (BLException ex) {
            return fallo(ex);
        }
    }

    // Body: { "idConsumible": 2, "cantidad": 10 }
    @POST
    @Path("/{id}/consumibles")
    public Response asociarConsumible(@PathParam("id") int id, ConsumibleBandejaDTO datos) {
        if (!esAdministrador()) {
            return sinPermiso();
        }
        if (datos == null) {
            return error(Response.Status.BAD_REQUEST, "Debe indicar el consumible y la cantidad");
        }
        try {
            bandejaBL.asociarConsumible(id, datos.getIdConsumible(), datos.getCantidad());
            return Response.ok(BandejaMapper.toDTO(bandejaBL.consultarDetalle(id))).build();
        } catch (BLException ex) {
            return fallo(ex);
        }
    }

    // Body: { "cantidad": 12 }
    @PUT
    @Path("/{id}/consumibles/{idConsumible}")
    public Response actualizarCantidad(@PathParam("id") int id, @PathParam("idConsumible") int idConsumible,
                                       ConsumibleBandejaDTO datos) {
        if (!esAdministrador()) {
            return sinPermiso();
        }
        if (datos == null) {
            return error(Response.Status.BAD_REQUEST, "Debe indicar la cantidad");
        }
        try {
            bandejaBL.actualizarCantidadDespachada(id, idConsumible, datos.getCantidad());
            return Response.ok(BandejaMapper.toDTO(bandejaBL.consultarDetalle(id))).build();
        } catch (BLException ex) {
            return fallo(ex);
        }
    }

    @DELETE
    @Path("/{id}/consumibles/{idConsumible}")
    public Response quitarConsumible(@PathParam("id") int id, @PathParam("idConsumible") int idConsumible) {
        if (!esAdministrador()) {
            return sinPermiso();
        }
        try {
            bandejaBL.quitarConsumible(id, idConsumible);
            return Response.ok(BandejaMapper.toDTO(bandejaBL.consultarDetalle(id))).build();
        } catch (BLException ex) {
            return fallo(ex);
        }
    }

    // ---- sesión ----

    // TODO token: cuando SesionesRest llegue a fix-backend, validar
    // SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION)) y exigir rol ADMINISTRADOR.
    private boolean esAdministrador() {
        return true;
    }

    // ---- respuestas ----

    private Response sinPermiso() {
        return error(Response.Status.FORBIDDEN, "Solo el administrador puede gestionar plantillas de bandejas");
    }

    private Response fallo(BLException ex) {
        return error(ex.getCause() == null ? Response.Status.BAD_REQUEST
                : Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(Map.of("error", mensaje == null ? "Error" : mensaje)).build();
    }
}
