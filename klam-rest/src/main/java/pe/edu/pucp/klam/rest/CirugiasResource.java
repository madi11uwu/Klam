package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.BandejaInstrumentalBL;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.ClinicaHospitalBL;
import pe.edu.pucp.klam.bl.EquipoBL;
import pe.edu.pucp.klam.bl.PacienteParticularBL;
import pe.edu.pucp.klam.bl.impl.BandejaInstrumentalBLImpl;
import pe.edu.pucp.klam.bl.impl.CirugiaBLImpl;
import pe.edu.pucp.klam.bl.impl.ClinicaHospitalBLImpl;
import pe.edu.pucp.klam.bl.impl.EquipoBLImpl;
import pe.edu.pucp.klam.bl.impl.PacienteParticularBLImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.rest.dto.AccionCirugiaDTO;
import pe.edu.pucp.klam.rest.dto.CirugiaDTO;
import pe.edu.pucp.klam.rest.dto.CirugiaFormDTO;
import pe.edu.pucp.klam.rest.dto.OpcionDTO;
import pe.edu.pucp.klam.rest.dto.OpcionesCirugiaDTO;
import pe.edu.pucp.klam.rest.mapper.BandejaMapper;
import pe.edu.pucp.klam.rest.mapper.CirugiaMapper;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

// RF02: gestión de cirugías. Las reglas (cruces de horario, esterilización, estados) están en CirugiaBL.
@Path("/cirugias")
@Produces("application/json")
@Consumes("application/json")
public class CirugiasResource {
    private final CirugiaBL cirugiaBL = new CirugiaBLImpl();
    private final ClinicaHospitalBL clinicaBL = new ClinicaHospitalBLImpl();
    private final PacienteParticularBL pacienteBL = new PacienteParticularBLImpl();
    private final EquipoBL equipoBL = new EquipoBLImpl();
    private final BandejaInstrumentalBL bandejaBL = new BandejaInstrumentalBLImpl();

    @Context
    private HttpHeaders headers;

    @Context
    private UriInfo uriInfo;

    // GET /cirugias?desde=2026-11-01&hasta=2026-11-30&tipoCliente=CLINICA_HOSPITAL&idCliente=2&idEquipo=1&estado=PROGRAMADA
    @GET
    public Response listar(@QueryParam("desde") String desde, @QueryParam("hasta") String hasta,
                           @QueryParam("tipoCliente") String tipoCliente, @QueryParam("idCliente") Integer idCliente,
                           @QueryParam("idEquipo") Integer idEquipo, @QueryParam("estado") String estado) {
        try {
            return Response.ok(CirugiaMapper.toDTOs(cirugiaBL.buscarDetalle(
                    fecha(desde, false), fecha(hasta, true), vacioANulo(tipoCliente),
                    idCliente, idEquipo, estado(estado)))).build();
        } catch (IllegalArgumentException ex) {
            return error(Response.Status.BAD_REQUEST, ex.getMessage());
        } catch (BLException ex) {
            return fallo(ex);
        }
    }

    @GET
    @Path("/{id}")
    public Response consultar(@PathParam("id") int id) {
        try {
            Cirugia cirugia = cirugiaBL.findById(id);
            if (cirugia == null) {
                return error(Response.Status.NOT_FOUND, "No existe la cirugía " + id);
            }
            CirugiaDTO dto = CirugiaMapper.toDTO(cirugiaBL.obtenerDetalle(id));
            dto.setActivo(cirugia.isActivo());
            return Response.ok(dto).build();
        } catch (BLException ex) {
            return fallo(ex);
        }
    }

    // Listas del formulario: clientes, equipos disponibles y bandejas disponibles del procedimiento
    @GET
    @Path("/opciones")
    public Response opciones(@QueryParam("procedimiento") String procedimiento) {
        if (!esAdministrador()) {
            return sinPermiso();
        }
        try {
            OpcionesCirugiaDTO dto = new OpcionesCirugiaDTO();
            for (ClinicaHospital clinica : clinicaBL.findAll()) {
                if (clinica.isActivo()) {
                    dto.getClientes().add(new OpcionDTO(clinica.getIdCliente(), clinica.getNombre(),
                            CirugiaBL.TIPO_CLIENTE_CLINICA));
                }
            }
            for (PacienteParticular paciente : pacienteBL.findAll()) {
                if (paciente.isActivo()) {
                    dto.getClientes().add(new OpcionDTO(paciente.getIdCliente(), paciente.getNombre(),
                            CirugiaBL.TIPO_CLIENTE_PARTICULAR));
                }
            }
            for (Equipo equipo : equipoBL.listarDisponibles()) {
                dto.getEquipos().add(new OpcionDTO(equipo.getIdEquipo(), equipo.getNombre(),
                        equipo.getCategoria() != null ? equipo.getCategoria().name() : null));
            }
            dto.setBandejas(BandejaMapper.toDTOs(bandejaBL.listarDisponibles(vacioANulo(procedimiento))));
            return Response.ok(dto).build();
        } catch (BLException ex) {
            return fallo(ex);
        }
    }

    @POST
    public Response registrar(CirugiaFormDTO datos) {
        if (!esAdministrador()) {
            return sinPermiso();
        }
        if (datos == null) {
            return error(Response.Status.BAD_REQUEST, "Debe enviar los datos de la cirugía");
        }
        try {
            Cirugia cirugia = CirugiaMapper.toModelo(datos);
            cirugiaBL.registrar(cirugia, responsable());

            URI uri = uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(cirugia.getIdCirugia()))
                    .build();
            return Response.created(uri)
                    .entity(CirugiaMapper.toDTO(cirugiaBL.obtenerDetalle(cirugia.getIdCirugia())))
                    .build();
        } catch (IllegalArgumentException ex) {
            return error(Response.Status.BAD_REQUEST, ex.getMessage());
        } catch (BLException ex) {
            return fallo(ex);
        }
    }

    // Modifica los datos; el estado no se cambia aquí (para eso están /estado y /cancelacion)
    @PUT
    @Path("/{id}")
    public Response modificar(@PathParam("id") int id, CirugiaFormDTO datos) {
        if (!esAdministrador()) {
            return sinPermiso();
        }
        if (datos == null) {
            return error(Response.Status.BAD_REQUEST, "Debe enviar los datos de la cirugía");
        }
        try {
            Cirugia existente = cirugiaBL.findById(id);
            if (existente == null) {
                return error(Response.Status.NOT_FOUND, "No existe la cirugía " + id);
            }
            Cirugia cirugia = CirugiaMapper.toModelo(datos);
            cirugia.setIdCirugia(id);
            cirugia.setEstado(existente.getEstado());
            cirugia.setMotivoCancelacion(existente.getMotivoCancelacion());
            cirugiaBL.modificar(cirugia, responsable());
            return Response.ok(CirugiaMapper.toDTO(cirugiaBL.obtenerDetalle(id))).build();
        } catch (IllegalArgumentException ex) {
            return error(Response.Status.BAD_REQUEST, ex.getMessage());
        } catch (BLException ex) {
            return fallo(ex);
        }
    }

    // PROGRAMADA -> EN_PROCESO -> FINALIZADA
    @POST
    @Path("/{id}/estado")
    public Response cambiarEstado(@PathParam("id") int id, AccionCirugiaDTO datos) {
        if (!esAdministrador()) {
            return sinPermiso();
        }
        try {
            EstadoCirugia nuevo = estado(datos == null ? null : datos.getEstado());
            cirugiaBL.cambiarEstado(id, nuevo, responsable());
            return Response.ok(CirugiaMapper.toDTO(cirugiaBL.obtenerDetalle(id))).build();
        } catch (IllegalArgumentException ex) {
            return error(Response.Status.BAD_REQUEST, ex.getMessage());
        } catch (BLException ex) {
            return fallo(ex);
        }
    }

    @POST
    @Path("/{id}/cancelacion")
    public Response cancelar(@PathParam("id") int id, AccionCirugiaDTO datos) {
        if (!esAdministrador()) {
            return sinPermiso();
        }
        try {
            cirugiaBL.cancelar(id, datos == null ? null : datos.getMotivo(), responsable());
            return Response.ok(CirugiaMapper.toDTO(cirugiaBL.obtenerDetalle(id))).build();
        } catch (BLException ex) {
            return fallo(ex);
        }
    }

    // Eliminación lógica (activo = 0)
    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id) {
        if (!esAdministrador()) {
            return sinPermiso();
        }
        try {
            cirugiaBL.eliminar(id, responsable());
            return Response.noContent().build();
        } catch (BLException ex) {
            return fallo(ex);
        }
    }

    // ---- sesión ----

    // TODO token: cuando SesionesRest llegue a fix-backend, validar
    // SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION)) y exigir rol ADMINISTRADOR.
    private boolean esAdministrador() {
        return true;
    }

    // TODO token: usar el username de la sesión para la bitácora (RF16)
    private String responsable() {
        return CirugiaBL.RESPONSABLE_SISTEMA;
    }

    // ---- conversiones de parámetros ----

    private LocalDateTime fecha(String valor, boolean finDelDia) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            if (valor.trim().length() == 10) {
                LocalDate dia = LocalDate.parse(valor.trim());
                return finDelDia ? dia.atTime(23, 59, 59) : dia.atStartOfDay();
            }
            return LocalDateTime.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha con formato inválido: " + valor);
        }
    }

    private EstadoCirugia estado(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return EstadoCirugia.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de cirugía no válido: " + valor);
        }
    }

    private String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    // ---- respuestas ----

    private Response sinPermiso() {
        return error(Response.Status.FORBIDDEN, "Solo el administrador puede gestionar cirugías");
    }

    // Sin causa = regla de negocio (400); con causa = error de base de datos (500)
    private Response fallo(BLException ex) {
        return error(ex.getCause() == null ? Response.Status.BAD_REQUEST
                : Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(Map.of("error", mensaje == null ? "Error" : mensaje)).build();
    }
}
