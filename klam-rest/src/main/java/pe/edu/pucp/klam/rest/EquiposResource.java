package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.EquipoBL;
import pe.edu.pucp.klam.bl.impl.EquipoBLImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.CategoriaEquipo;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
import pe.edu.pucp.klam.rest.mapper.EquipoMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Catálogo de equipos para el filtro de maquinaria (RF17) y para la logística (RF14).
 *
 * GET /api/v1/equipos                         -> todos los equipos activos
 * GET /api/v1/equipos?soloDisponibles=true    -> solo los disponibles para despacho
 * GET /api/v1/equipos?categoria=CRANEOTOMO    -> por categoría (CRANEOTOMO, NAVEGADOR, MICROSCOPIO, OTRO)
 *
 * Cada equipo incluye sus especificaciones (clave/valor), entre ellas la "marca".
 */
@Path("/equipos")
@Produces("application/json")
public class EquiposResource {
    private final EquipoBL equipoBL;

    public EquiposResource() {
        this(new EquipoBLImpl());
    }

    // Visibilidad de paquete: el contenedor REST solo usa el constructor público sin argumentos.
    EquiposResource(EquipoBL equipoBL) {
        this.equipoBL = equipoBL;
    }

    @GET
    public Response listar(@QueryParam("categoria") String categoria,
                           @QueryParam("soloDisponibles") Boolean soloDisponibles) {
        try {
            CategoriaEquipo filtro = null;
            if (categoria != null && !categoria.isBlank()) {
                try {
                    filtro = CategoriaEquipo.valueOf(categoria.trim().toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ex) {
                    return error(Response.Status.BAD_REQUEST, "Categoría de equipo no válida: " + categoria);
                }
            }

            List<Equipo> equipos = Boolean.TRUE.equals(soloDisponibles)
                    ? equipoBL.listarDisponibles() : equipoBL.findAll();
            if (filtro != null) {
                List<Equipo> filtrados = new ArrayList<>();
                for (Equipo equipo : equipos) {
                    if (equipo.getCategoria() == filtro) {
                        filtrados.add(equipo);
                    }
                }
                equipos = filtrados;
            }
            return Response.ok(EquipoMapper.toDTOs(equipos)).build();
        } catch (BLException ex) {
            return error(ex.getCause() == null ? Response.Status.BAD_REQUEST
                    : Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private static Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(Map.of("error", mensaje)).build();
    }
}
