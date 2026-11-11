package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.klam.bl.AuditoriaCirugiaBL;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.impl.AuditoriaCirugiaBLImpl;
import pe.edu.pucp.klam.rest.dto.AuditoriaCirugiaDTO;
import pe.edu.pucp.klam.rest.dto.AuditoriaCirugiaDetalleDTO;
import pe.edu.pucp.klam.rest.mapper.AuditoriaCirugiaMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Bitacora de cirugias (RF16). Solo lectura: la escritura ocurre dentro de CirugiaBL. */
@Path("/auditoria/cirugias")
@Produces(MediaType.APPLICATION_JSON)
public class AuditoriaResource {
    private final AuditoriaCirugiaBL auditoriaBL = new AuditoriaCirugiaBLImpl();

    @GET
    public Response listar(@QueryParam("idCirugia") String idCirugiaTexto,
                           @QueryParam("desde") String desde,
                           @QueryParam("hasta") String hasta,
                           @QueryParam("accion") String accion,
                           @QueryParam("responsable") String responsable) {
        try {
            Integer idCirugia = RespuestaRest.enteroOpcional(idCirugiaTexto, "idCirugia");
            LocalDateTime inicio = RespuestaRest.fechaDesde(desde, "desde");
            LocalDateTime fin = RespuestaRest.fechaHasta(hasta, "hasta");
            List<Map<String, Object>> filas = auditoriaBL.buscarBitacora(
                    idCirugia,
                    inicio,
                    fin,
                    RespuestaRest.vacioANull(accion),
                    RespuestaRest.vacioANull(responsable));
            List<AuditoriaCirugiaDTO> respuesta = AuditoriaCirugiaMapper.aDTOs(filas);
            return RespuestaRest.ok(respuesta);
        } catch (BLException ex) {
            return RespuestaRest.badRequest(ex.getMessage());
        }
    }

    @GET
    @Path("/{id}")
    public Response obtener(@PathParam("id") String idTexto) {
        try {
            Integer id = RespuestaRest.enteroOpcional(idTexto, "id");
            if (id == null) {
                return RespuestaRest.badRequest("Indique el identificador de la entrada");
            }
            Map<String, Object> entrada = auditoriaBL.obtenerEntradaBitacora(id);
            if (entrada == null) {
                return RespuestaRest.notFound("Entrada de bitácora no encontrada");
            }
            List<Map<String, Object>> cambios = auditoriaBL.obtenerDetalleBitacora(id);
            AuditoriaCirugiaDetalleDTO respuesta = AuditoriaCirugiaMapper.aDetalle(entrada, cambios);
            return RespuestaRest.ok(respuesta);
        } catch (BLException ex) {
            return RespuestaRest.badRequest(ex.getMessage());
        }
    }
}
