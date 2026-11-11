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
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Context;
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
// BLException sin causa = regla de negocio (400); con causa = error de base de datos (500).
@Path("/bandejas")
@Produces("application/json")
@Consumes("application/json")
public class BandejasResource {
    @Context private HttpHeaders headers;
    private Response acceso(String rol) {
        SesionesRest.Sesion sesion = SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (sesion == null) return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Sesión inválida o expirada")).build();
        if (rol != null && !rol.equals(sesion.rol())) return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", "Acceso denegado")).build();
        return null;
    }

    private final BandejaInstrumentalBL bandejaBL;

    @Context
    private UriInfo uriInfo;

    public BandejasResource() {
        bandejaBL = new BandejaInstrumentalBLImpl();
    }

    // GET /bandejas o /bandejas?procedimiento=Cirugía de columna
    @GET
    public Response listar(@QueryParam("procedimiento") String procedimiento) {
        Response acceso = acceso(null);
        if (acceso != null) return acceso;
        try {
            if (procedimiento != null && !procedimiento.isBlank()) {
                return Response.ok(BandejaMapper.toDTOs(bandejaBL.listarPorProcedimiento(procedimiento)))
                        .build();
            }
            return Response.ok(BandejaMapper.toDTOs(bandejaBL.findAll()))
                    .build();
        }
        catch (BLException ex) {
            if (ex.getCause() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", ex.getMessage()))
                        .build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        }
    }

    // Esterilizadas, activas y sin cirugía asignada
    @GET
    @Path("/disponibles")
    public Response disponibles(@QueryParam("procedimiento") String procedimiento) {
        Response acceso = acceso(null);
        if (acceso != null) return acceso;
        try {
            return Response.ok(BandejaMapper.toDTOs(bandejaBL.listarDisponibles(procedimiento)))
                    .build();
        }
        catch (BLException ex) {
            if (ex.getCause() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", ex.getMessage()))
                        .build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response consultar(@PathParam("id") int id) {
        Response acceso = acceso(null);
        if (acceso != null) return acceso;
        try {
            if (bandejaBL.findById(id) == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "No existe la bandeja " + id))
                        .build();
            }
            return Response.ok(BandejaMapper.toDTO(bandejaBL.consultarDetalle(id)))
                    .build();
        }
        catch (BLException ex) {
            if (ex.getCause() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", ex.getMessage()))
                        .build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        }
    }

    // Crea una plantilla nueva (sin consumibles; luego se asocian)
    @POST
    public Response registrar(BandejaFormDTO datos) {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        try {
            if (datos == null) {
                throw new BLException("Debe enviar los datos de la bandeja");
            }
            BandejaInstrumental bandeja = BandejaMapper.toModelo(datos);
            bandejaBL.insert(bandeja);

            URI uri = uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(bandeja.getIdBandeja()))
                    .build();

            return Response.created(uri)
                    .entity(BandejaMapper.toDTO(bandejaBL.consultarDetalle(bandeja.getIdBandeja())))
                    .build();
        }
        catch (BLException ex) {
            if (ex.getCause() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", ex.getMessage()))
                        .build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        }
    }

    // Body: { "idConsumible": 2, "cantidad": 10 }
    @POST
    @Path("/{id}/consumibles")
    public Response asociarConsumible(@PathParam("id") int id, ConsumibleBandejaDTO datos) {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        try {
            if (datos == null) {
                throw new BLException("Debe indicar el consumible y la cantidad");
            }
            bandejaBL.asociarConsumible(id, datos.getIdConsumible(), datos.getCantidad());
            return Response.ok(BandejaMapper.toDTO(bandejaBL.consultarDetalle(id)))
                    .build();
        }
        catch (BLException ex) {
            if (ex.getCause() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", ex.getMessage()))
                        .build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        }
    }

    // Body: { "cantidad": 12 }
    @PUT
    @Path("/{id}/consumibles/{idConsumible}")
    public Response actualizarCantidad(@PathParam("id") int id, @PathParam("idConsumible") int idConsumible,
                                       ConsumibleBandejaDTO datos) {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        try {
            if (datos == null) {
                throw new BLException("Debe indicar la cantidad");
            }
            bandejaBL.actualizarCantidadDespachada(id, idConsumible, datos.getCantidad());
            return Response.ok(BandejaMapper.toDTO(bandejaBL.consultarDetalle(id)))
                    .build();
        }
        catch (BLException ex) {
            if (ex.getCause() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", ex.getMessage()))
                        .build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}/consumibles/{idConsumible}")
    public Response quitarConsumible(@PathParam("id") int id, @PathParam("idConsumible") int idConsumible) {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        try {
            bandejaBL.quitarConsumible(id, idConsumible);
            return Response.ok(BandejaMapper.toDTO(bandejaBL.consultarDetalle(id)))
                    .build();
        }
        catch (BLException ex) {
            if (ex.getCause() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", ex.getMessage()))
                        .build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        }
    }
}
