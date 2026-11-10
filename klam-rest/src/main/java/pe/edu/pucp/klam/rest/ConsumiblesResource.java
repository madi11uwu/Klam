package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
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
    private final ConsumibleBL consumibleBL;

    public ConsumiblesResource() {
        consumibleBL = new ConsumibleBLImpl();
    }

    @GET
    public Response listar() {
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
