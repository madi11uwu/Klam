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
 * El solicitante sale de la sesión (token de /login), no del cuerpo: un cliente no puede actuar por otro.
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
    public Response registrar(@HeaderParam("Authorization") String authorization, RegistrarSolicitudDTO datos) {
        Response rechazo = verificar(authorization);
        if (rechazo != null) {
            return rechazo;
        }
        if (datos == null) {
            return Errores.error(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es obligatorio");
        }
        SesionesRest.Sesion sesion = SesionesRest.validar(authorization);

        SolicitudCirugia solicitud = new SolicitudCirugia();
        solicitud.setCliente(clienteDe(sesion));
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
    public Response listarPropias(@HeaderParam("Authorization") String authorization) {
        Response rechazo = verificar(authorization);
        if (rechazo != null) {
            return rechazo;
        }
        try {
            Cliente cliente = clienteDe(SesionesRest.validar(authorization));
            return Response.ok(solicitudBL.listarSolicitudesPorCliente(cliente).stream()
                    .map(SolicitudCirugiaDTO::new).toList()).build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    /** 401 sin sesión válida; 403 si quien llama no es un cliente. */
    private static Response verificar(String authorization) {
        SesionesRest.Sesion sesion = SesionesRest.validar(authorization);
        if (sesion == null) {
            return Errores.error(Response.Status.UNAUTHORIZED, "Sesión inválida o expirada");
        }
        boolean esCliente = sesion.idCliente() != null && sesion.idCliente() > 0
                && sesion.rol().equals(sesion.tipoCliente())
                && ("CLINICA_HOSPITAL".equals(sesion.rol())
                    || "PACIENTE_PARTICULAR".equals(sesion.rol()));
        return esCliente ? null
                : Errores.error(Response.Status.FORBIDDEN, "Esta opción es solo para clientes");
    }

    /** La cuenta autenticada conserva una vinculación comprobada con el cliente comercial. */
    private static Cliente clienteDe(SesionesRest.Sesion sesion) {
        Cliente cliente = "CLINICA_HOSPITAL".equals(sesion.rol()) ? new ClinicaHospital() : new PacienteParticular();
        cliente.setIdCliente(sesion.idCliente());
        return cliente;
    }
}
