package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.ConsumoBL;
import pe.edu.pucp.klam.bl.NoEncontradoException;
import pe.edu.pucp.klam.bl.ValidacionException;
import pe.edu.pucp.klam.bl.impl.ConsumoBLImpl;
import pe.edu.pucp.klam.rest.dto.RegistroConsumoDTO;
import pe.edu.pucp.klam.rest.mapper.ConsumoMapper;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** RF15: consulta y cierre del consumo real por cirugía, solo para administradores. */
@Path("/consumos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConsumosResource {
    @Context private HttpHeaders headers;

    private Response acceso(String rol) {
        SesionesRest.Sesion sesion = SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (sesion == null) return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Sesión inválida o expirada")).build();
        if (rol != null && !rol.equals(sesion.rol())) return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", "Acceso denegado")).build();
        return null;
    }

    private static final Logger LOG = Logger.getLogger(ConsumosResource.class.getName());

    private final ConsumoBL consumoBL;

    public ConsumosResource() {
        consumoBL = new ConsumoBLImpl();
    }

    @GET
    public Response listar(@QueryParam("q") String q,
                           @QueryParam("desde") String desde,
                           @QueryParam("hasta") String hasta) {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        try {
            LocalDateTime inicio = RespuestaRest.fechaDesde(desde, "desde");
            LocalDateTime fin = RespuestaRest.fechaHasta(hasta, "hasta");
            return Response.ok(ConsumoMapper.toDTOs(consumoBL.listar(RespuestaRest.vacioANull(q), inicio, fin)))
                    .header("Cache-Control", "no-store")
                    .build();
        }
        catch (ValidacionException ex) {
            return RespuestaRest.badRequest(ex.getMessage());
        }
        catch (BLException ex) {
            return responder(ex);
        }
    }

    @GET
    @Path("/{idCirugia}")
    public Response obtener(@PathParam("idCirugia") int idCirugia) {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        try {
            return Response.ok(ConsumoMapper.toDetalleDTO(consumoBL.obtener(idCirugia)))
                    .header("Cache-Control", "no-store")
                    .build();
        }
        catch (NoEncontradoException ex) {
            return RespuestaRest.notFound(ex.getMessage());
        }
        catch (ValidacionException ex) {
            return RespuestaRest.badRequest(ex.getMessage());
        }
        catch (BLException ex) {
            return responder(ex);
        }
    }

    @PUT
    @Path("/{idCirugia}")
    public Response guardar(@PathParam("idCirugia") int idCirugia, RegistroConsumoDTO registro) {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        try {
            if (registro == null) {
                throw new ValidacionException("Debe enviar las cantidades consumidas");
            }
            if (idCirugia != registro.getIdCirugia()) {
                throw new ValidacionException("El id y el payload no coinciden");
            }
            consumoBL.guardar(idCirugia, ConsumoMapper.toConsumos(registro), registro.isConfirmar());
            return Response.ok(ConsumoMapper.toDetalleDTO(consumoBL.obtener(idCirugia))).build();
        }
        catch (NoEncontradoException ex) {
            return RespuestaRest.notFound(ex.getMessage());
        }
        catch (ValidacionException ex) {
            return RespuestaRest.badRequest(ex.getMessage());
        }
        catch (BLException ex) {
            return responder(ex);
        }
    }

    // Errores técnicos (base de datos): al usuario un mensaje genérico, pero la causa real queda en el log del servidor
    private Response responder(BLException ex) {
        if (ex.getCause() != null) {
            LOG.log(Level.SEVERE, "Error en /consumos: " + ex.getMessage(), ex);
        }
        return Errores.desde(ex);
    }
}
