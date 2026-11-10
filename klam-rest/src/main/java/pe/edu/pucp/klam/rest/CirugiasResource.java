package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.impl.CirugiaBLImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.rest.dto.CirugiaRequestDTO;
import pe.edu.pucp.klam.rest.mapper.RegistroMapper;

import java.util.Map;

/**
 * RF09: el administrador registra una cirugía (POST /cirugias).
 * Integrante 5 (RF02) agrega aquí GET/PUT/DELETE: es la misma clase, no crear otra con @Path("/cirugias").
 * RF01/RF02: solo ADMINISTRADOR (verificado en el servidor con el token de /login); el responsable sale del token.
 */
@Path("/cirugias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CirugiasResource {

    private final CirugiaBL cirugiaBL = new CirugiaBLImpl();

    @POST
    @RequiereRol("ADMINISTRADOR")
    public Response registrar(@HeaderParam("Authorization") String authorization, CirugiaRequestDTO dto) {
        if (dto == null) {
            return Errores.error(Response.Status.BAD_REQUEST, "El cuerpo de la cirugía es obligatorio");
        }
        // El responsable de la auditoría es quien inició sesión (ya verificado por @RequiereRol).
        String responsable = SesionesActivas.desdeEncabezado(authorization).username();
        try {
            Cirugia cirugia = RegistroMapper.toCirugia(dto);
            cirugiaBL.registrar(cirugia, responsable);
            Map<String, Object> detalle = cirugiaBL.obtenerDetalle(cirugia.getIdCirugia());
            return Response.created(java.net.URI.create("/cirugias/" + cirugia.getIdCirugia()))
                    .entity(detalle).build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }
}
