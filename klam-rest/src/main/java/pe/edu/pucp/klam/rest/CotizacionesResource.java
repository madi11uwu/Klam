package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.NoSuchFileException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CotizacionBL;
import pe.edu.pucp.klam.bl.SolicitudCirugiaBL;
import pe.edu.pucp.klam.bl.impl.CotizacionBLImpl;
import pe.edu.pucp.klam.bl.impl.SolicitudCirugiaBLImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.SolicitudCirugia;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Cotizacion;

@Path("/cotizaciones")
@Produces(MediaType.APPLICATION_JSON)
public class CotizacionesResource {
    private final CotizacionBL cotizaciones = new CotizacionBLImpl();
    private final SolicitudCirugiaBL solicitudes = new SolicitudCirugiaBLImpl();
    @Context private HttpHeaders headers;

    @GET
    @Path("/solicitud/{idSolicitud}")
    public Response historial(@PathParam("idSolicitud") int idSolicitud) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return error(Response.Status.UNAUTHORIZED, "Sesión inválida o expirada");
        try {
            SolicitudCirugia solicitud = solicitudes.findSolicitudById(idSolicitud);
            if (solicitud == null) return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            if (!puedeConsultar(sesion, solicitud)) return error(Response.Status.FORBIDDEN, "No puede consultar esta cotización");
            List<Map<String, Object>> resultado = cotizaciones.listarPorSolicitud(idSolicitud).stream()
                    .map(CotizacionesResource::resumen).toList();
            return Response.ok(resultado).build();
        } catch (BLException ex) { return Errores.desde(ex); }
    }

    @GET
    @Path("/solicitud/{idSolicitud}/vigente")
    public Response vigente(@PathParam("idSolicitud") int idSolicitud) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return error(Response.Status.UNAUTHORIZED, "Sesión inválida o expirada");
        try {
            SolicitudCirugia solicitud = solicitudes.findSolicitudById(idSolicitud);
            if (solicitud == null) return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            if (!puedeConsultar(sesion, solicitud)) return error(Response.Status.FORBIDDEN, "No puede consultar esta cotización");
            Cotizacion cotizacion = cotizaciones.buscarVigentePorSolicitud(idSolicitud);
            return cotizacion == null ? error(Response.Status.NOT_FOUND, "No existe cotización vigente")
                    : Response.ok(resumen(cotizacion)).build();
        } catch (BLException ex) { return Errores.desde(ex); }
    }

    @GET
    @Path("/{idCotizacion}/archivo")
    public Response archivo(@PathParam("idCotizacion") int idCotizacion) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return error(Response.Status.UNAUTHORIZED, "Sesión inválida o expirada");
        try {
            Cotizacion cotizacion = cotizaciones.findById(idCotizacion);
            if (cotizacion == null || cotizacion.getSolicitud() == null)
                return error(Response.Status.NOT_FOUND, "No existe el archivo de cotización");
            SolicitudCirugia solicitud = solicitudes.findSolicitudById(cotizacion.getSolicitud().getIdSolicitud());
            if (solicitud == null || !puedeConsultar(sesion, solicitud))
                return error(Response.Status.FORBIDDEN, "No puede descargar este archivo");
            byte[] contenido = ArchivoDocumentoStorage.leer(cotizacion.getArchivoPath());
            return Response.ok(contenido, ArchivoDocumentoStorage.tipoContenido(cotizacion.getArchivoPath()))
                    .header("Content-Disposition", "attachment; filename=\"cotizacion-" + idCotizacion
                            + ArchivoDocumentoStorage.extension(cotizacion.getArchivoPath()) + "\"")
                    .header("X-Content-Type-Options", "nosniff").build();
        } catch (NoSuchFileException ex) { return error(Response.Status.NOT_FOUND, "El archivo físico no está disponible");
        } catch (BLException ex) { return Errores.desde(ex);
        } catch (IOException ex) { return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo leer el archivo"); }
    }

    @POST
    @Path("/{idCotizacion}/aceptacion")
    public Response aceptar(@PathParam("idCotizacion") int idCotizacion) { return decidir(idCotizacion, true, null); }

    @POST
    @Path("/{idCotizacion}/rechazo")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response rechazar(@PathParam("idCotizacion") int idCotizacion, Map<String, String> datos) {
        return decidir(idCotizacion, false, datos == null ? null : datos.get("motivo"));
    }

    private Response decidir(int idCotizacion, boolean aceptar, String motivo) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return error(Response.Status.UNAUTHORIZED, "Sesión inválida o expirada");
        if (!"CLINICA_HOSPITAL".equals(sesion.rol()) || sesion.idCliente() == null)
            return error(Response.Status.FORBIDDEN, "Solo la clínica puede decidir su cotización");
        try {
            Cotizacion cotizacion = cotizaciones.findById(idCotizacion);
            if (cotizacion == null || cotizacion.getSolicitud() == null)
                return error(Response.Status.NOT_FOUND, "No existe la cotización de solicitud");
            SolicitudCirugia solicitud = solicitudes.findSolicitudById(cotizacion.getSolicitud().getIdSolicitud());
            if (solicitud == null || !puedeConsultar(sesion, solicitud))
                return error(Response.Status.FORBIDDEN, "La cotización no pertenece a su clínica");
            cotizaciones.decidirVersion(idCotizacion, sesion.idUsuario(), aceptar, motivo, sesion.username());
            return Response.ok(Map.of("idCotizacion", idCotizacion,
                    "estado", aceptar ? "ACEPTADA" : "RECHAZADA", "version", cotizacion.getVersion())).build();
        } catch (SecurityException ex) { return error(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (BLException ex) { return Errores.desde(ex); }
    }

    static Map<String, Object> resumen(Cotizacion cotizacion) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("idCotizacion", cotizacion.getIdCotizacion());
        datos.put("idSolicitud", cotizacion.getSolicitud().getIdSolicitud());
        datos.put("version", cotizacion.getVersion());
        datos.put("vigente", cotizacion.isVigente());
        datos.put("estado", cotizacion.getEstado().name());
        datos.put("precioPactado", cotizacion.getPrecioPactado());
        datos.put("fechaEmision", cotizacion.getFechaEmision());
        datos.put("fechaDecision", cotizacion.getFechaDecision());
        datos.put("idCuentaDecision", cotizacion.getIdCuentaDecision());
        datos.put("motivoRechazo", cotizacion.getMotivoRechazo());
        datos.put("archivoDisponible", cotizacion.getArchivoPath() != null);
        return datos;
    }

    private boolean puedeConsultar(SesionesRest.Sesion sesion, SolicitudCirugia solicitud) {
        if ("ADMINISTRADOR".equals(sesion.rol())) return true;
        return "CLINICA_HOSPITAL".equals(sesion.rol())
                && solicitud.getCliente() instanceof ClinicaHospital
                && sesion.idCliente() != null
                && sesion.idCliente() == solicitud.getCliente().getIdCliente();
    }
    private SesionesRest.Sesion sesion() { return SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION)); }
    private Response error(Response.Status status, String mensaje) { return Errores.error(status, mensaje); }
}