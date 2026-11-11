package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Context;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.ConsumibleBL;
import pe.edu.pucp.klam.bl.impl.ConsumibleBLImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;
import pe.edu.pucp.klam.rest.dto.ConsumibleDTO;
import pe.edu.pucp.klam.rest.mapper.BandejaMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Catálogo de consumibles activos (selector de la pantalla de plantillas, RF13)
@Path("/consumibles")
@Produces("application/json")
public class ConsumiblesResource {
    @Context private HttpHeaders headers;
    private Response acceso(String rol) {
        SesionesRest.Sesion sesion = SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (sesion == null) return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Sesión inválida o expirada")).build();
        if (rol != null && !rol.equals(sesion.rol())) return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", "Acceso denegado")).build();
        return null;
    }

    private final ConsumibleBL consumibleBL;

    public ConsumiblesResource() {
        consumibleBL = new ConsumibleBLImpl();
    }

    @GET
    public Response listar() {
        Response acceso = acceso(null);
        if (acceso != null) return acceso;
        try {
            List<ConsumibleDTO> dtos = new ArrayList<>();
            for (Consumible consumible : consumibleBL.findAll()) {
                if (consumible.isActivo()) {
                    dtos.add(BandejaMapper.toDTO(consumible));
                }
            }
            return Response.ok(dtos)
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
