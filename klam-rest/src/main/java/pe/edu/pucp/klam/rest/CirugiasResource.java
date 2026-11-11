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
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Context;
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
import pe.edu.pucp.klam.rest.dto.CirugiaFormDTO;
import pe.edu.pucp.klam.rest.dto.OpcionDTO;
import pe.edu.pucp.klam.rest.dto.OpcionesCirugiaDTO;
import pe.edu.pucp.klam.rest.mapper.BandejaMapper;
import pe.edu.pucp.klam.rest.mapper.CirugiaConsultaMapper;
import pe.edu.pucp.klam.rest.mapper.CirugiaMapper;

import java.net.URI;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Cirugías.
 * Consultas (rol 1): RF06 calendario y detalle, RF17 filtros, RF10 historial por cliente.
 * Gestión (rol 5):   RF02 registrar, modificar, cambiar estado, cancelar y eliminar lógicamente.
 *
 * GET    /api/v1/cirugias                 -> lista filtrada (CirugiaResumenDTO)
 * GET    /api/v1/cirugias/calendario      -> cirugías de un mes (CirugiaResumenDTO)
 * GET    /api/v1/cirugias/{id}            -> detalle (CirugiaDetalleDTO)
 * GET    /api/v1/cirugias/opciones        -> listas del formulario (clientes, equipos y bandejas disponibles)
 * POST   /api/v1/cirugias                 -> registra (CirugiaFormDTO, responde CirugiaDTO)
 * PUT    /api/v1/cirugias/{id}            -> modifica datos (no el estado)
 * POST   /api/v1/cirugias/{id}/estado     -> PROGRAMADA -> EN_PROCESO -> FINALIZADA
 * POST   /api/v1/cirugias/{id}/cancelacion-> cancela con motivo
 * DELETE /api/v1/cirugias/{id}            -> eliminación lógica
 *
 * Las reglas (cruces de horario, esterilización, estados) están en CirugiaBL.
 * BLException sin causa = regla de negocio (400); con causa = error de base de datos (500).
 */
@Path("/cirugias")
@Produces("application/json")
@Consumes("application/json")
public class CirugiasResource {
    private static final Set<String> ESTADOS_HISTORIAL = Set.of("FINALIZADA", "CANCELADA");
    @Context private HttpHeaders headers;
    private Response acceso(String rol) {
        SesionesRest.Sesion sesion = SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (sesion == null) return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Sesión inválida o expirada")).build();
        if (rol != null && !rol.equals(sesion.rol())) return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", "Acceso denegado")).build();
        return null;
    }

    // ---------------------------------------------------------------- alcance por perfil (RF06, RF10)
    // ADMINISTRADOR y TECNICO_INSTRUMENTISTA ven toda la agenda; el VENDEDOR, las cirugías que nacieron de
    // solicitudes que él registró; el cliente, solo las suyas. La gestión (RF02) sigue siendo del administrador.
    private static boolean esCliente(SesionesRest.Sesion s) {
        return ("CLINICA_HOSPITAL".equals(s.rol()) || "PACIENTE_PARTICULAR".equals(s.rol()))
                && s.idCliente() != null && s.tipoCliente() != null;
    }

    private static boolean puedeConsultarAgenda(SesionesRest.Sesion s) {
        return "ADMINISTRADOR".equals(s.rol()) || "TECNICO_INSTRUMENTISTA".equals(s.rol())
                || "VENDEDOR".equals(s.rol()) || esCliente(s);
    }

    private List<Map<String, Object>> visibles(List<Map<String, Object>> filas, SesionesRest.Sesion s)
            throws BLException {
        if ("ADMINISTRADOR".equals(s.rol()) || "TECNICO_INSTRUMENTISTA".equals(s.rol())) return filas;
        List<Map<String, Object>> resultado = new ArrayList<>();
        if (esCliente(s)) {
            for (Map<String, Object> f : filas) {
                if (s.tipoCliente().equals(String.valueOf(f.get("tipoCliente")))
                        && f.get("idCliente") instanceof Number n && n.intValue() == s.idCliente()) {
                    resultado.add(f);
                }
            }
            return resultado;
        }
        if ("VENDEDOR".equals(s.rol())) {
            pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor vendedor = new pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor();
            vendedor.setIdUsuario(s.idUsuario());
            Set<Integer> solicitudes = new java.util.HashSet<>();
            for (var sol : solicitudBL.listarSolicitudesPorVendedor(vendedor)) solicitudes.add(sol.getIdSolicitud());
            for (Map<String, Object> f : filas) {
                if (f.get("idSolicitudOrigen") instanceof Number n && solicitudes.contains(n.intValue())) {
                    resultado.add(f);
                }
            }
        }
        return resultado;
    }

    private final pe.edu.pucp.klam.bl.SolicitudCirugiaBL solicitudBL = new pe.edu.pucp.klam.bl.impl.SolicitudCirugiaBLImpl();

    private String responsableAutenticado() {
        return SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION)).username();
    }

    @GET
    @Path("/historial")
    public Response historialPorCliente(@QueryParam("tipoCliente") String tipoCliente,
                                        @QueryParam("idCliente") String idClienteTexto) {
        SesionesRest.Sesion sesion = SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (sesion == null) return error(Response.Status.UNAUTHORIZED, "Sesión inválida o expirada");
        String tipo;
        int id;
        if ("ADMINISTRADOR".equals(sesion.rol())) {
            try { id = Integer.parseInt(idClienteTexto); }
            catch (NumberFormatException ex) { return error(Response.Status.BAD_REQUEST, "Indique el cliente"); }
            tipo = tipoCliente;
        } else if ("PACIENTE_PARTICULAR".equals(sesion.rol()) || "CLINICA_HOSPITAL".equals(sesion.rol())) {
            if (sesion.idCliente() == null) return error(Response.Status.FORBIDDEN, "Cuenta sin cliente asociado");
            id = sesion.idCliente();
            tipo = sesion.tipoCliente();
        } else return error(Response.Status.FORBIDDEN, "Acceso denegado");
        try {
            List<Map<String, Object>> filas = cirugiaBL.buscarDetalle(null, null, tipo, id, null, null);
            List<Map<String, Object>> historial = new ArrayList<>();
            for (Map<String, Object> fila : filas)
                if (ESTADOS_HISTORIAL.contains(String.valueOf(fila.get("estado")))) historial.add(fila);
            return Response.ok(CirugiaConsultaMapper.toResumenDTOs(historial)).build();
        } catch (BLException ex) { return errorBL(ex); }
    }

    private final CirugiaBL cirugiaBL;
    private final ClinicaHospitalBL clinicaBL;
    private final PacienteParticularBL pacienteBL;
    private final EquipoBL equipoBL;
    private final BandejaInstrumentalBL bandejaBL;

    @Context
    private UriInfo uriInfo;

    public CirugiasResource() {
        this(new CirugiaBLImpl());
    }

    // Visibilidad de paquete a propósito: el contenedor REST solo usa el constructor público sin argumentos.
    CirugiasResource(CirugiaBL cirugiaBL) {
        this.cirugiaBL = cirugiaBL;
        this.clinicaBL = new ClinicaHospitalBLImpl();
        this.pacienteBL = new PacienteParticularBLImpl();
        this.equipoBL = new EquipoBLImpl();
        this.bandejaBL = new BandejaInstrumentalBLImpl();
    }

    /**
     * Lista de cirugías con filtros combinables (todos opcionales):
     * desde / hasta (yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss), tipoCliente (CLINICA_HOSPITAL | PACIENTE_PARTICULAR),
     * idCliente (requiere tipoCliente), idEquipo y estado (PROGRAMADA, EN_PROCESO, FINALIZADA, CANCELADA
     * o su etiqueta: Programada, En curso, Culminada, Cancelada).
     * Sin resultados responde 200 con una lista vacía.
     */
    @GET
    public Response buscar(@QueryParam("desde") String desde,
                           @QueryParam("hasta") String hasta,
                           @QueryParam("tipoCliente") String tipoCliente,
                           @QueryParam("idCliente") Integer idCliente,
                           @QueryParam("idEquipo") Integer idEquipo,
                           @QueryParam("estado") String estado) {
        SesionesRest.Sesion sesion = SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (sesion == null) return error(Response.Status.UNAUTHORIZED, "Sesión inválida o expirada");
        if (!puedeConsultarAgenda(sesion)) return error(Response.Status.FORBIDDEN, "Acceso denegado");
        try {
            // Un cliente solo consulta las suyas: su tipo e id vienen de la sesión, no de los parámetros.
            if (esCliente(sesion)) {
                tipoCliente = sesion.tipoCliente();
                idCliente = sesion.idCliente();
            }
            LocalDateTime inicio = parsearFecha(desde, false);
            LocalDateTime fin = parsearFecha(hasta, true);
            EstadoCirugia estadoCirugia = CirugiaConsultaMapper.parsearEstado(estado);
            String tipo = vacioANulo(tipoCliente);
            if (tipo != null) {
                tipo = tipo.toUpperCase(Locale.ROOT);
            }

            List<Map<String, Object>> filas =
                    cirugiaBL.buscarDetalle(inicio, fin, tipo, idCliente, idEquipo, estadoCirugia);
            return Response.ok(CirugiaConsultaMapper.toResumenDTOs(visibles(filas, sesion))).build();
        } catch (IllegalArgumentException ex) {
            return error(Response.Status.BAD_REQUEST, ex.getMessage());
        } catch (BLException ex) {
            return errorBL(ex);
        }
    }

    /** Cirugías de un mes. Si no se indican ano y mes se usa el mes actual. */
    @GET
    @Path("/calendario")
    public Response calendario(@QueryParam("anio") Integer anio,
                               @QueryParam("mes") Integer mes) {
        SesionesRest.Sesion sesion = SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (sesion == null) return error(Response.Status.UNAUTHORIZED, "Sesión inválida o expirada");
        if (!puedeConsultarAgenda(sesion)) return error(Response.Status.FORBIDDEN, "Acceso denegado");
        LocalDate hoy = LocalDate.now();
        int a = anio != null ? anio : hoy.getYear();
        int m = mes != null ? mes : hoy.getMonthValue();
        try {
            List<Map<String, Object>> filas = cirugiaBL.buscarCalendarioMensual(a, m);
            return Response.ok(CirugiaConsultaMapper.toResumenDTOs(visibles(filas, sesion))).build();
        } catch (DateTimeException ex) {
            return error(Response.Status.BAD_REQUEST, "Año o mes no válido: " + a + "/" + m);
        } catch (BLException ex) {
            return errorBL(ex);
        }
    }

    /** Detalle de una cirugía con su equipo (y marca), bandeja y consumibles. */
    @GET
    @Path("/{id: \\d+}")
    public Response detalle(@PathParam("id") int id) {
        SesionesRest.Sesion sesion = SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (sesion == null) return error(Response.Status.UNAUTHORIZED, "Sesión inválida o expirada");
        if (!puedeConsultarAgenda(sesion)) return error(Response.Status.FORBIDDEN, "Acceso denegado");
        try {
            // findById devuelve la cirugía con equipo, bandeja y cliente ya cargados; no filtra por
            // 'activo', así que una cirugía eliminada lógicamente se trata como inexistente.
            Cirugia cirugia = cirugiaBL.findById(id);
            if (cirugia == null || !cirugia.isActivo()) {
                return error(Response.Status.NOT_FOUND, "No existe una cirugía con id " + id);
            }
            Map<String, Object> detalle = cirugiaBL.obtenerDetalle(id);
            if (visibles(List.of(detalle), sesion).isEmpty()) {
                return error(Response.Status.FORBIDDEN, "La cirugía no pertenece a su cuenta");
            }
            return Response.ok(CirugiaConsultaMapper.toDetalleDTO(
                    detalle, cirugia.getEquipo(), cirugia.getBandejaInstrumental())).build();
        } catch (BLException ex) {
            return errorBL(ex);
        }
    }

    // ---------------------------------------------------------------- RF02 (rol 5)

    /** Listas del formulario: clientes, equipos disponibles y bandejas disponibles del procedimiento. */
    @GET
    @Path("/opciones")
    public Response opciones(@QueryParam("procedimiento") String procedimiento) {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
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
            return errorBL(ex);
        }
    }

    @POST
    public Response registrar(CirugiaFormDTO datos) {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        try {
            if (datos == null) {
                throw new BLException("Debe enviar los datos de la cirugía");
            }
            Cirugia cirugia = CirugiaMapper.toModelo(datos);
            cirugiaBL.registrar(cirugia, responsableAutenticado());
            URI uri = uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(cirugia.getIdCirugia()))
                    .build();
            return Response.created(uri)
                    .entity(CirugiaMapper.toDTO(cirugiaBL.obtenerDetalle(cirugia.getIdCirugia())))
                    .build();
        } catch (BLException ex) {
            return errorBL(ex);
        }
    }

    /** Modifica los datos; el estado no se cambia aquí (para eso están /estado y /cancelacion). */
    @PUT
    @Path("/{id: \\d+}")
    public Response modificar(@PathParam("id") int id, CirugiaFormDTO datos) {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        try {
            if (datos == null) {
                throw new BLException("Debe enviar los datos de la cirugía");
            }
            Cirugia existente = cirugiaBL.findById(id);
            if (existente == null || !existente.isActivo()) {
                return error(Response.Status.NOT_FOUND, "No existe una cirugía con id " + id);
            }
            Cirugia cirugia = CirugiaMapper.toModelo(datos);
            cirugia.setIdCirugia(id);
            cirugia.setEstado(existente.getEstado());
            cirugia.setMotivoCancelacion(existente.getMotivoCancelacion());
            cirugiaBL.modificar(cirugia, responsableAutenticado());
            return Response.ok(CirugiaMapper.toDTO(cirugiaBL.obtenerDetalle(id))).build();
        } catch (BLException ex) {
            return errorBL(ex);
        }
    }

    /** PROGRAMADA -> EN_PROCESO -> FINALIZADA. */
    @POST
    @Path("/{id: \\d+}/estado")
    public Response cambiarEstado(@PathParam("id") int id, AccionCirugiaDTO datos) {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        try {
            EstadoCirugia nuevo = CirugiaConsultaMapper.parsearEstado(datos == null ? null : datos.getEstado());
            if (nuevo == null) {
                throw new BLException("Debe indicar el nuevo estado");
            }
            cirugiaBL.cambiarEstado(id, nuevo, responsableAutenticado());
            return Response.ok(CirugiaMapper.toDTO(cirugiaBL.obtenerDetalle(id))).build();
        } catch (IllegalArgumentException ex) {
            return error(Response.Status.BAD_REQUEST, ex.getMessage());
        } catch (BLException ex) {
            return errorBL(ex);
        }
    }

    @POST
    @Path("/{id: \\d+}/cancelacion")
    public Response cancelar(@PathParam("id") int id, AccionCirugiaDTO datos) {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        try {
            cirugiaBL.cancelar(id, datos == null ? null : datos.getMotivo(), responsableAutenticado());
            return Response.ok(CirugiaMapper.toDTO(cirugiaBL.obtenerDetalle(id))).build();
        } catch (BLException ex) {
            return errorBL(ex);
        }
    }

    /** Eliminación lógica (activo = 0). */
    @DELETE
    @Path("/{id: \\d+}")
    public Response eliminar(@PathParam("id") int id) {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        try {
            cirugiaBL.eliminar(id, responsableAutenticado());
            return Response.noContent().build();
        } catch (BLException ex) {
            return errorBL(ex);
        }
    }

    // ---------------------------------------------------------------- apoyo
    private static LocalDateTime parsearFecha(String texto, boolean finDeDia) {
        String t = vacioANulo(texto);
        if (t == null) {
            return null;
        }
        try {
            if (t.length() <= 10) {
                LocalDate fecha = LocalDate.parse(t);
                return finDeDia ? fecha.atTime(23, 59, 59) : fecha.atStartOfDay();
            }
            return LocalDateTime.parse(t);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Fecha no válida: " + texto + " (use yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss)");
        }
    }

    private static String vacioANulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }


    private static Response errorBL(BLException ex) {
        Response.Status estado = ex.getCause() == null
                ? Response.Status.BAD_REQUEST
                : Response.Status.INTERNAL_SERVER_ERROR;
        return error(estado, ex.getMessage());
    }

    private static Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(Map.of("error", mensaje)).build();
    }
}
