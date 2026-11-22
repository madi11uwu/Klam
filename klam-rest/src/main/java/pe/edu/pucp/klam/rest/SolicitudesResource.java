package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import pe.edu.pucp.klam.bl.CotizacionBL;
import pe.edu.pucp.klam.bl.impl.CotizacionBLImpl;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Cotizacion;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.BandejaInstrumentalBL;
import pe.edu.pucp.klam.bl.impl.BandejaInstrumentalBLImpl;
import pe.edu.pucp.klam.bl.SolicitudCirugiaBL;
import pe.edu.pucp.klam.bl.impl.SolicitudCirugiaBLImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.SolicitudCirugia;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;
import pe.edu.pucp.klam.rest.dto.AccionSolicitudDTO;
import pe.edu.pucp.klam.rest.dto.ClinicaNuevaDTO;
import pe.edu.pucp.klam.rest.dto.RegistrarSolicitudDTO;
import pe.edu.pucp.klam.rest.dto.SolicitudCirugiaDTO;

@Path("/solicitudes")
@Produces("application/json")
@Consumes("application/json")
public class SolicitudesResource {

    private final SolicitudCirugiaBL solicitudBL = new SolicitudCirugiaBLImpl();
    private final CotizacionBL cotizacionBL = new CotizacionBLImpl();
    private final BandejaInstrumentalBL bandejaBL = new BandejaInstrumentalBLImpl();

    @Context
    private HttpHeaders headers;

    @POST
    public Response registrar(RegistrarSolicitudDTO datos) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esVendedor(sesion)) return sinPermisoVendedor();
        if (datos == null) {
            return error(Response.Status.BAD_REQUEST, "El cliente solicitante es obligatorio");
        }

        boolean esClinica = "CLINICA".equals(datos.getTipoCliente()) || "CLINICA_HOSPITAL".equals(datos.getTipoCliente());
        ClinicaHospital clinicaNueva = null;
        Cliente cliente = null;
        if (esClinica) {
            // Un vendedor solo solicita por una clinica que aun no esta en BIOKLAM;
            // si ya esta registrada, la solicitud la hace la propia clinica.
            if (datos.getIdCliente() > 0) {
                return error(Response.Status.CONFLICT, SolicitudCirugiaBLImpl.MSG_CLINICA_YA_REGISTRADA);
            }
            ClinicaNuevaDTO c = datos.getClinicaNueva();
            if (c == null) {
                return error(Response.Status.BAD_REQUEST, "Ingrese los datos de la clínica");
            }
            clinicaNueva = new ClinicaHospital();
            clinicaNueva.setNombre(c.getNombre());
            clinicaNueva.setRuc(c.getRuc());
            clinicaNueva.setDireccion(c.getDireccion());
            clinicaNueva.setEmailContacto(c.getEmailContacto());
            clinicaNueva.setTelefono(c.getTelefono());
            clinicaNueva.setPeriodoCredito(c.getPeriodoCredito());
            clinicaNueva.setTieneConsignacion(c.isTieneConsignacion());
        } else {
            if (datos.getIdCliente() <= 0) {
                return error(Response.Status.BAD_REQUEST, "El cliente solicitante es obligatorio");
            }
            if ("PARTICULAR".equals(datos.getTipoCliente()) || "PACIENTE_PARTICULAR".equals(datos.getTipoCliente())) {
                PacienteParticular particular = new PacienteParticular();
                particular.setIdCliente(datos.getIdCliente());
                cliente = particular;
            } else {
                return error(Response.Status.BAD_REQUEST, "Tipo de cliente no soportado");
            }
        }

        SolicitudCirugia solicitud = new SolicitudCirugia();
        solicitud.setCliente(cliente);
        if (!esClinica && datos.getIdClinicaDestino() > 0) {
            ClinicaHospital destino = new ClinicaHospital();
            destino.setIdCliente(datos.getIdClinicaDestino());
            solicitud.setClinicaDestino(destino);
        }
        Vendedor vendedor = new Vendedor();
        vendedor.setIdUsuario(sesion.idUsuario());
        solicitud.setVendedorRegistrador(vendedor);
        solicitud.setPacienteNombre(datos.getPacienteNombre());
        solicitud.setDiagnostico(datos.getDiagnostico());
        solicitud.setTipoProcedimiento(datos.getTipoProcedimiento());
        solicitud.setDoctorNombre(datos.getDoctorNombre());
        solicitud.setObservaciones(datos.getObservaciones());
        try {
            if (datos.getFechaHoraSolicitada() != null) {
                solicitud.setFechaHoraSolicitada(LocalDateTime.parse(datos.getFechaHoraSolicitada()));
            }
            if (esClinica) {
                solicitudBL.registrarSolicitudClinicaNueva(solicitud, clinicaNueva,
                        sesion.username(), sesion.idUsuario(), sesion.rol());
            } else {
                solicitudBL.registrarSolicitud(solicitud, sesion.username(), sesion.idUsuario(), sesion.rol());
            }
            return Response.status(Response.Status.CREATED)
                    .entity(new SolicitudCirugiaDTO(solicitud)).build();
        } catch (DateTimeParseException ex) {
            return error(Response.Status.BAD_REQUEST, "La fecha y hora solicitadas no son válidas");
        } catch (BLException ex) {
            return fallo(ex, Response.Status.BAD_REQUEST);
        }
    }

    /** Procedimientos del catálogo (los que tienen bandeja): la solicitud debe usar uno de ellos. */
    @GET
    @Path("/procedimientos")
    public Response listarProcedimientos() {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        try {
            return Response.ok(bandejaBL.listarProcedimientos()).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Path("/mias")
    public Response listarPropias() {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esVendedor(sesion)) return sinPermisoVendedor();
        Vendedor vendedor = new Vendedor();
        vendedor.setIdUsuario(sesion.idUsuario());
        try {
            return Response.ok(solicitudBL.listarSolicitudesPorVendedor(vendedor).stream()
                    .map(SolicitudCirugiaDTO::new).toList()).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.BAD_REQUEST);
        }
    }

    @GET
    public Response listar(@QueryParam("estado") String estado) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
        try {
            return Response.ok(solicitudBL.listarSolicitudes(estado).stream()
                    .map(SolicitudCirugiaDTO::new).toList()).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Path("/{id}")
    public Response consultar(@PathParam("id") int id) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
        if (id <= 0) return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        try {
            SolicitudCirugia solicitud = solicitudBL.findSolicitudById(id);
            if (solicitud == null) return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            return Response.ok(new SolicitudCirugiaDTO(solicitud)).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/{id}/aprobacion")
    public Response aprobar(@PathParam("id") int id) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
        if (id <= 0) return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        try {
            if (solicitudBL.findSolicitudById(id) == null) {
                return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            }
            solicitudBL.aprobarSolicitud(id, sesion.username(), sesion.idUsuario());
            return Response.noContent().build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        }
    }

    @POST
    @Path("/{id}/rechazo")
    public Response rechazar(@PathParam("id") int id, AccionSolicitudDTO datos) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
        if (id <= 0) return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        if (datos == null || datos.getMotivo() == null || datos.getMotivo().isBlank()
                || datos.getMotivo().length() > 200) {
            return error(Response.Status.BAD_REQUEST, "El motivo de rechazo debe tener entre 1 y 200 caracteres");
        }
        try {
            if (solicitudBL.findSolicitudById(id) == null) {
                return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            }
            solicitudBL.rechazarSolicitud(id, datos.getMotivo(), sesion.username(), sesion.idUsuario());
            return Response.noContent().build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        }
    }

    @POST
    @Path("/{id}/cirugia")
    public Response registrarCirugia(@PathParam("id") int id) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
        if (id <= 0) return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        try {
            if (solicitudBL.findSolicitudById(id) == null) {
                return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            }
            int idCirugia = solicitudBL.registrarCirugiaDesdeSolicitud(id, null, sesion.username());
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("idCirugia", idCirugia))
                    .build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        }
    }

    @POST
    @Path("/{id}/cotizaciones")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response adjuntarCotizacion(@PathParam("id") int id,
            @FormParam("archivo") EntityPart archivo, @FormParam("precioPactado") String precioTexto) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
        if (id <= 0 || archivo == null || precioTexto == null)
            return error(Response.Status.BAD_REQUEST, "Solicitud, archivo y precio del ERP son obligatorios");
        double precio;
        try {
            precio = Double.parseDouble(precioTexto);
            if (!Double.isFinite(precio) || precio < 0)
                return error(Response.Status.BAD_REQUEST, "El precio del ERP debe ser válido");
        } catch (NumberFormatException ex) {
            return error(Response.Status.BAD_REQUEST, "El precio del ERP debe ser numérico");
        }
        try {
            if (solicitudBL.findSolicitudById(id) == null)
                return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            String clave = ArchivoDocumentoStorage.guardar(archivo);
            Cotizacion cotizacion = new Cotizacion();
            SolicitudCirugia solicitud = new SolicitudCirugia();
            solicitud.setIdSolicitud(id);
            cotizacion.setSolicitud(solicitud);
            cotizacion.setArchivoPath(clave);
            cotizacion.setPrecioPactado(precio);
            cotizacion.setFechaEmision(LocalDateTime.now());
            try {
                cotizacionBL.registrarVersion(cotizacion, sesion.idUsuario(), sesion.username());
            } catch (RuntimeException ex) {
                try { ArchivoDocumentoStorage.eliminar(clave); }
                catch (IOException limpieza) { ex.addSuppressed(limpieza); }
                throw ex;
            }
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("idCotizacion", cotizacion.getIdCotizacion(),
                            "version", cotizacion.getVersion(), "estado", cotizacion.getEstado().name())).build();
        } catch (IllegalArgumentException ex) {
            return error(Response.Status.BAD_REQUEST, ex.getMessage());
        } catch (SecurityException ex) {
            return error(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        } catch (IOException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo almacenar el archivo");
        }
    }

    @GET
    @Path("/{id}/elegibilidad-cirugia")
    public Response elegibilidad(@PathParam("id") int id) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
        try {
            if (solicitudBL.findSolicitudById(id) == null)
                return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            var resultado = solicitudBL.evaluarElegibilidad(id);
            java.util.Map<String, Object> datos = new java.util.LinkedHashMap<>();
            datos.put("puedeRegistrarCirugia", resultado.puedeRegistrar());
            datos.put("requiereCotizacion", resultado.requiereCotizacion());
            datos.put("estadoDocumental", resultado.estadoDocumental());
            datos.put("estadoCotizacion", resultado.estadoCotizacion());
            datos.put("motivoBloqueo", resultado.motivo());
            return Response.ok(datos).build();
        } catch (BLException ex) { return fallo(ex, Response.Status.CONFLICT); }
    }
    private SesionesRest.Sesion sesion() {
        return SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
    }

    private boolean esAdministrador(SesionesRest.Sesion sesion) {
        return "ADMINISTRADOR".equals(sesion.rol());
    }

    private boolean esVendedor(SesionesRest.Sesion sesion) {
        return "VENDEDOR".equals(sesion.rol());
    }

    private Response noAutenticado() {
        return error(Response.Status.UNAUTHORIZED, "Sesión inválida o expirada");
    }

    private Response sinPermiso() {
        return error(Response.Status.FORBIDDEN, "No tiene permisos de administrador");
    }

    private Response sinPermisoVendedor() {
        return error(Response.Status.FORBIDDEN, "No tiene permisos de vendedor");
    }

    private Response fallo(BLException ex, Response.Status estadoNegocio) {
        return error(ex.getCause() == null ? estadoNegocio : Response.Status.INTERNAL_SERVER_ERROR,
                ex.getMessage());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(Map.of("error", mensaje)).build();
    }
}
