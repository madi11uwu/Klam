package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.impl.CirugiaBLImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Historial de cirugias de un cliente (RF10): culminadas y canceladas. */
@Path("/cirugias")
@Produces(MediaType.APPLICATION_JSON)
public class CirugiasResource {

    private static final Set<String> ESTADOS_HISTORIAL = Set.of("FINALIZADA", "CANCELADA");

    private final CirugiaBL cirugiaBL = new CirugiaBLImpl();

    @GET
    @Path("/historial")
    public Response historialPorCliente(@QueryParam("tipoCliente") String tipoCliente,
                                        @QueryParam("idCliente") String idClienteTexto) {
        try {
            Integer idCliente = RespuestaRest.enteroOpcional(idClienteTexto, "idCliente");
            if (idCliente == null) {
                return RespuestaRest.badRequest("Indique el identificador del cliente");
            }
            String tipo = RespuestaRest.vacioANull(tipoCliente);
            if (tipo == null) {
                return RespuestaRest.badRequest("Indique el tipo de cliente");
            }

            // La BL valida el tipo y el id; la respuesta solo incluye estados de historial.
            List<Map<String, Object>> filas = cirugiaBL.buscarDetalle(null, null, tipo, idCliente, null, null);
            List<Map<String, Object>> historial = new ArrayList<>();
            for (Map<String, Object> fila : filas) {
                Object estado = fila.get("estado");
                if (estado instanceof String texto && ESTADOS_HISTORIAL.contains(texto)) {
                    historial.add(fila);
                }
            }
            return RespuestaRest.ok(RespuestaRest.normalizarTodas(historial));
        } catch (BLException ex) {
            return RespuestaRest.badRequest(ex.getMessage());
        }
    }
}