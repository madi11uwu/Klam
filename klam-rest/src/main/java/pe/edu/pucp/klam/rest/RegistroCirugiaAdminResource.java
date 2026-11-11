package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.impl.CirugiaBLImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.rest.dto.RegistroCirugiaAdminDTO;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * RF09: el ADMINISTRADOR registra una cirugía (POST /admin/cirugias).
 * El rol lo controla Blazor; el responsable de la auditoría llega como ?responsable=usuario. Ruta propia: el CRUD completo de cirugías (RF02) vive en /cirugias.
 */
@Path("/admin/cirugias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistroCirugiaAdminResource {

    private final CirugiaBL cirugiaBL = new CirugiaBLImpl();

    @POST
    public Response registrar(@QueryParam("responsable") String responsable, RegistroCirugiaAdminDTO datos) {
        if (datos == null) {
            return Errores.error(Response.Status.BAD_REQUEST, "El cuerpo de la cirugía es obligatorio");
        }
        try {
            Cirugia cirugia = new Cirugia();   // nace PROGRAMADA y activa
            cirugia.setCliente(clienteDe(datos));
            cirugia.setDiagnostico(datos.getDiagnostico());
            cirugia.setTipoProcedimiento(datos.getTipoProcedimiento());
            cirugia.setDoctorNombre(datos.getDoctorNombre());
            cirugia.setFechaHoraInicio(fecha(datos.getFechaHoraInicio(), "La fecha y hora de inicio"));
            if (datos.getFechaHoraFin() != null && !datos.getFechaHoraFin().isBlank()) {
                cirugia.setFechaHoraFin(fecha(datos.getFechaHoraFin(), "La fecha y hora de fin"));
            }
            if (datos.getIdEquipo() != null) {
                Equipo equipo = new Equipo();
                equipo.setIdEquipo(datos.getIdEquipo());
                cirugia.setEquipo(equipo);
            }
            cirugiaBL.registrar(cirugia, responsable);
            return Response.created(URI.create("/cirugias/" + cirugia.getIdCirugia()))
                    .entity(Map.of("idCirugia", cirugia.getIdCirugia(), "estado", cirugia.getEstado().name()))
                    .build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    private static Cliente clienteDe(RegistroCirugiaAdminDTO datos) throws BLException {
        Cliente cliente;
        if (CirugiaBL.TIPO_CLIENTE_CLINICA.equals(datos.getTipoCliente())) {
            cliente = new ClinicaHospital();
        } else if (CirugiaBL.TIPO_CLIENTE_PARTICULAR.equals(datos.getTipoCliente())) {
            cliente = new PacienteParticular();
        } else {
            throw new BLException("Tipo de cliente inválido: use " + CirugiaBL.TIPO_CLIENTE_CLINICA
                    + " o " + CirugiaBL.TIPO_CLIENTE_PARTICULAR);
        }
        cliente.setIdCliente(datos.getIdCliente());   // la BL verifica que exista y esté activo
        return cliente;
    }

    private static LocalDateTime fecha(String texto, String campo) throws BLException {
        if (texto == null || texto.isBlank()) {
            return null;   // la BL informa que es obligatoria
        }
        try {
            return LocalDateTime.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new BLException(campo + " no tiene formato válido (use yyyy-MM-ddTHH:mm)");
        }
    }
}
