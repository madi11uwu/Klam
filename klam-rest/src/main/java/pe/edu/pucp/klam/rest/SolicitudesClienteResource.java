package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.SolicitudCirugiaBL;
import pe.edu.pucp.klam.bl.impl.SolicitudCirugiaBLImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.SolicitudCirugia;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.rest.dto.RegistrarSolicitudDTO;
import pe.edu.pucp.klam.rest.dto.SolicitudCirugiaDTO;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * RF05: el CLIENTE registra y consulta sus propias solicitudes de cirugía.
 * El solicitante es el cliente de la sesión de Blazor: viaja en el cuerpo (tipoCliente, idCliente) al registrar
 * y como ?tipoCliente=&idCliente= al listar, como los ids del cliente en las órdenes del profesor.
 * (Las solicitudes que registra un vendedor, RF08, siguen en /solicitudes.)
 *   POST /cliente/solicitudes   -> registra (201)
 *   GET  /cliente/solicitudes   -> lista las propias
 */
@Path("/cliente/solicitudes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SolicitudesClienteResource {

    private final SolicitudCirugiaBL solicitudBL = new SolicitudCirugiaBLImpl();

    @POST
    public Response registrar(RegistrarSolicitudDTO datos) {
        if (datos == null) {
            return Errores.error(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es obligatorio");
        }
        Cliente cliente = clienteDe(datos.getTipoCliente(), datos.getIdCliente());
        if (cliente == null) {
            return Errores.error(Response.Status.BAD_REQUEST, "Indique el cliente solicitante");
        }

        SolicitudCirugia solicitud = new SolicitudCirugia();
        solicitud.setCliente(cliente);
        if (datos.getIdClinicaDestino() > 0) {
            ClinicaHospital destino = new ClinicaHospital();
            destino.setIdCliente(datos.getIdClinicaDestino());
            solicitud.setClinicaDestino(destino);
        }
        solicitud.setPacienteNombre(datos.getPacienteNombre());
        solicitud.setDiagnostico(datos.getDiagnostico());
        solicitud.setTipoProcedimiento(datos.getTipoProcedimiento());
        solicitud.setDoctorNombre(datos.getDoctorNombre());
        solicitud.setObservaciones(datos.getObservaciones());
        try {
            if (datos.getFechaHoraSolicitada() != null && !datos.getFechaHoraSolicitada().isBlank()) {
                solicitud.setFechaHoraSolicitada(LocalDateTime.parse(datos.getFechaHoraSolicitada().trim()));
            }
            int id = solicitudBL.registrarSolicitud(solicitud);
            SolicitudCirugia creada = solicitudBL.findSolicitudById(id);
            return Response.created(URI.create("/solicitudes/" + id)).entity(new SolicitudCirugiaDTO(creada)).build();
        } catch (DateTimeParseException ex) {
            return Errores.error(Response.Status.BAD_REQUEST, "La fecha y hora solicitadas no son válidas");
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    @GET
    public Response listarPropias(@QueryParam("tipoCliente") String tipoCliente,
                                  @QueryParam("idCliente") Integer idCliente) {
        Cliente cliente = clienteDe(tipoCliente, idCliente == null ? 0 : idCliente);
        if (cliente == null) {
            return Errores.error(Response.Status.BAD_REQUEST, "Indique el cliente");
        }
        try {
            return Response.ok(solicitudBL.listarSolicitudesPorCliente(cliente).stream()
                    .map(SolicitudCirugiaDTO::new).toList()).build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    /** Cliente de la cuenta: CLINICA_HOSPITAL o PACIENTE_PARTICULAR con su id; null si no es válido. */
    private static Cliente clienteDe(String tipoCliente, int idCliente) {
        if (idCliente <= 0) {
            return null;
        }
        Cliente cliente;
        if ("CLINICA_HOSPITAL".equals(tipoCliente)) {
            cliente = new ClinicaHospital();
        } else if ("PACIENTE_PARTICULAR".equals(tipoCliente)) {
            cliente = new PacienteParticular();
        } else {
            return null;
        }
        cliente.setIdCliente(idCliente);
        return cliente;
    }
}
