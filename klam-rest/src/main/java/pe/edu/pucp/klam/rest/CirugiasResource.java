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
// BLException sin causa = regla de negocio (400); con causa = error de base de datos (500).
@Path("/cirugias")
@Produces("application/json")
@Consumes("application/json")
public class CirugiasResource {
    private final CirugiaBL cirugiaBL;
    private final ClinicaHospitalBL clinicaBL;
    private final PacienteParticularBL pacienteBL;
    private final EquipoBL equipoBL;
    private final BandejaInstrumentalBL bandejaBL;

    @Context
    private UriInfo uriInfo;

    public CirugiasResource() {
        cirugiaBL = new CirugiaBLImpl();
        clinicaBL = new ClinicaHospitalBLImpl();
        pacienteBL = new PacienteParticularBLImpl();
        equipoBL = new EquipoBLImpl();
        bandejaBL = new BandejaInstrumentalBLImpl();
    }

    // GET /cirugias?desde=2026-11-01&hasta=2026-11-30&tipoCliente=CLINICA_HOSPITAL&idCliente=2&idEquipo=1&estado=PROGRAMADA
    @GET
    public Response listar(@QueryParam("desde") String desde, @QueryParam("hasta") String hasta,
                           @QueryParam("tipoCliente") String tipoCliente, @QueryParam("idCliente") Integer idCliente,
                           @QueryParam("idEquipo") Integer idEquipo, @QueryParam("estado") String estado) {
        try {
            return Response.ok(CirugiaMapper.toDTOs(cirugiaBL.buscarDetalle(
                    fecha(desde, false), fecha(hasta, true), vacioANulo(tipoCliente),
                    idCliente, idEquipo, estado(estado))))
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

    @GET
    @Path("/{id}")
    public Response consultar(@PathParam("id") int id) {
        try {
            Cirugia cirugia = cirugiaBL.findById(id);
            if (cirugia == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "No existe la cirugía " + id))
                        .build();
            }
            CirugiaDTO dto = CirugiaMapper.toDTO(cirugiaBL.obtenerDetalle(id));
            dto.setActivo(cirugia.isActivo());
            return Response.ok(dto)
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

    // Listas del formulario: clientes, equipos disponibles y bandejas disponibles del procedimiento
    @GET
    @Path("/opciones")
    public Response opciones(@QueryParam("procedimiento") String procedimiento) {
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
            return Response.ok(dto)
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

    @POST
    public Response registrar(CirugiaFormDTO datos) {
        try {
            if (datos == null) {
                throw new BLException("Debe enviar los datos de la cirugía");
            }
            Cirugia cirugia = CirugiaMapper.toModelo(datos);
            cirugiaBL.registrar(cirugia, CirugiaBL.RESPONSABLE_SISTEMA);

            URI uri = uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(cirugia.getIdCirugia()))
                    .build();

            return Response.created(uri)
                    .entity(CirugiaMapper.toDTO(cirugiaBL.obtenerDetalle(cirugia.getIdCirugia())))
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

    // Modifica los datos; el estado no se cambia aquí (para eso están /estado y /cancelacion)
    @PUT
    @Path("/{id}")
    public Response modificar(@PathParam("id") int id, CirugiaFormDTO datos) {
        try {
            if (datos == null) {
                throw new BLException("Debe enviar los datos de la cirugía");
            }
            Cirugia existente = cirugiaBL.findById(id);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "No existe la cirugía " + id))
                        .build();
            }
            Cirugia cirugia = CirugiaMapper.toModelo(datos);
            cirugia.setIdCirugia(id);
            cirugia.setEstado(existente.getEstado());
            cirugia.setMotivoCancelacion(existente.getMotivoCancelacion());
            cirugiaBL.modificar(cirugia, CirugiaBL.RESPONSABLE_SISTEMA);

            return Response.ok(CirugiaMapper.toDTO(cirugiaBL.obtenerDetalle(id)))
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

    // PROGRAMADA -> EN_PROCESO -> FINALIZADA
    @POST
    @Path("/{id}/estado")
    public Response cambiarEstado(@PathParam("id") int id, AccionCirugiaDTO datos) {
        try {
            EstadoCirugia nuevo = estado(datos == null ? null : datos.getEstado());
            cirugiaBL.cambiarEstado(id, nuevo, CirugiaBL.RESPONSABLE_SISTEMA);
            return Response.ok(CirugiaMapper.toDTO(cirugiaBL.obtenerDetalle(id)))
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

    @POST
    @Path("/{id}/cancelacion")
    public Response cancelar(@PathParam("id") int id, AccionCirugiaDTO datos) {
        try {
            cirugiaBL.cancelar(id, datos == null ? null : datos.getMotivo(), CirugiaBL.RESPONSABLE_SISTEMA);
            return Response.ok(CirugiaMapper.toDTO(cirugiaBL.obtenerDetalle(id)))
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

    // Eliminación lógica (activo = 0)
    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id) {
        try {
            cirugiaBL.eliminar(id, CirugiaBL.RESPONSABLE_SISTEMA);
            return Response.noContent().build();
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

    // Acepta yyyy-MM-dd (día completo) o yyyy-MM-ddTHH:mm:ss
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
        }
        catch (DateTimeParseException ex) {
            throw new BLException("Fecha con formato inválido: " + valor);
        }
    }

    private EstadoCirugia estado(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return EstadoCirugia.valueOf(valor.trim().toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            throw new BLException("Estado de cirugía no válido: " + valor);
        }
    }

    private String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
