package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.SolicitudCirugiaBL;
import pe.edu.pucp.klam.bl.impl.SolicitudCirugiaBLImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.rest.dto.SolicitudCirugiaRequestDTO;
import pe.edu.pucp.klam.rest.mapper.RegistroMapper;

import java.net.URI;
import java.util.Map;

/**
 * RF05: solicitudes de un cliente. Ruta propia (no choca con /solicitudes, que es de revisión).
 *   POST /clientes/{tipo}/{id}/solicitudes  -> registra (201 + solicitud creada)
 *   GET  /clientes/{tipo}/{id}/solicitudes  -> lista las del cliente
 * {tipo} = CLINICA_HOSPITAL | PACIENTE_PARTICULAR (los ids de ambas tablas se solapan).
 */
@Path("/clientes/{tipo}/{id}/solicitudes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClientesSolicitudesResource {

    private final SolicitudCirugiaBL solicitudBL = new SolicitudCirugiaBLImpl();

    /** Un cliente solo puede operar sobre sí mismo; administrador y vendedor, sobre cualquiera. */
    private static Response verificarPropietario(String authorization, String tipo, int id) {
        SesionesActivas.Sesion sesion = SesionesActivas.desdeEncabezado(authorization);
        boolean esCliente = "CLINICA_HOSPITAL".equals(sesion.rol()) || "PACIENTE_PARTICULAR".equals(sesion.rol());
        if (esCliente && (!sesion.rol().equals(tipo) || sesion.id() != id)) {
            return Errores.error(Response.Status.FORBIDDEN, "Solo puede gestionar sus propias solicitudes");
        }
        return null;
    }

    @POST
    @RequiereRol({"ADMINISTRADOR", "VENDEDOR", "CLINICA_HOSPITAL", "PACIENTE_PARTICULAR"})
    public Response registrar(@HeaderParam("Authorization") String authorization,
                              @PathParam("tipo") String tipo, @PathParam("id") int id,
                              SolicitudCirugiaRequestDTO dto) {
        Response prohibido = verificarPropietario(authorization, tipo, id);
        if (prohibido != null) {
            return prohibido;
        }
        if (dto == null) {
            return Errores.error(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es obligatorio");
        }
        try {
            Cirugia datos = RegistroMapper.toDatosSolicitud(tipo, id, dto);
            int idSolicitud = solicitudBL.registrarSolicitud(
                    datos,
                    RegistroMapper.toClinicaDestino(dto.getIdClinicaDestino()),
                    dto.getPacienteNombre(),
                    dto.getObservaciones());
            Map<String, Object> creada = solicitudBL.findSolicitudById(idSolicitud);
            return Response.created(URI.create("/solicitudes/" + idSolicitud)).entity(creada).build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    @GET
    @RequiereRol({"ADMINISTRADOR", "VENDEDOR", "CLINICA_HOSPITAL", "PACIENTE_PARTICULAR"})
    public Response listar(@HeaderParam("Authorization") String authorization,
                           @PathParam("tipo") String tipo, @PathParam("id") int id) {
        Response prohibido = verificarPropietario(authorization, tipo, id);
        if (prohibido != null) {
            return prohibido;
        }
        try {
            Cliente cliente = RegistroMapper.toCliente(tipo, id);
            return Response.ok(solicitudBL.listarSolicitudesPorCliente(cliente)).build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }
}
