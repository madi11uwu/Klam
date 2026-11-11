package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.NotificacionBL;
import pe.edu.pucp.klam.bl.impl.NotificacionBLImpl;
import pe.edu.pucp.klam.modelo.comunicaciones.Notificacion;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Administrador;
import pe.edu.pucp.klam.modelo.usuariosPermisos.TecnicoInstrumentista;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;
import pe.edu.pucp.klam.rest.dto.NotificacionDTO;

/**
 * RF12: bandeja de avisos internos.
 *
 * GET  /notificaciones?rol=administrador|instrumentista&estado=no-leidas|leidas
 * GET  /notificaciones/resumen?rol=...            {total, noLeidas}
 * GET  /notificaciones/{id}                      detalle de un aviso
 * POST /notificaciones/{id}/lectura              marca un aviso como leído
 * PUT  /notificaciones/{id}/leida                marca un aviso como leído
 * POST /notificaciones/lectura?rol=...           marca avisos como leídos
 *
 **/


@Path("/notificaciones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificacionesResource {

    @Context private HttpHeaders headers;
    private SesionesRest.Sesion sesion() { return SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION)); }
    private Response acceso(SesionesRest.Sesion s) {
        if (s == null) return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Sesión inválida o expirada")).build();
        if (!"INSTRUMENTISTA".equals(s.rol())) return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", "Acceso denegado")).build();
        return null;
    }
    private boolean propia(Notificacion n, SesionesRest.Sesion s) {
        return n.getDestinatario() instanceof TecnicoInstrumentista
                && n.getDestinatario().getIdUsuario() == s.idUsuario();
    }
    private Response ajena() { return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", "Notificación ajena")).build(); }

    private final NotificacionBL notificacionBL = new NotificacionBLImpl();

    @GET
    public Response listar(@QueryParam("rol") String rol, @QueryParam("estado") String estado) {
        SesionesRest.Sesion sesion = sesion();
        Response acceso = acceso(sesion);
        if (acceso != null) return acceso;
        boolean soloNoLeidas = "no-leidas".equalsIgnoreCase(estado);
        boolean soloLeidas = "leidas".equalsIgnoreCase(estado);
        if (estado != null && !estado.isBlank() && !soloNoLeidas && !soloLeidas && !"todas".equalsIgnoreCase(estado)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El estado debe ser 'leidas', 'no-leidas' o 'todas'"))
                    .build();
        }
        try {
            List<Notificacion> todas = notificacionBL.findAll();
            List<NotificacionDTO> avisos = todas.stream().filter(n -> propia(n, sesion))
                    .filter(n -> !soloNoLeidas || !n.isEstadoLeida())
                    .filter(n -> !soloLeidas || n.isEstadoLeida())
                    .sorted(Comparator.comparing(Notificacion::getFechaHora, Comparator.nullsLast(Comparator.reverseOrder())))
                    .map(this::dto)
                    .toList();
            return Response.ok(avisos).header("Cache-Control", "no-store").build();
        } catch (BLException ex) {
            return responderError(ex);
        }
    }

    @GET
    @Path("/resumen")
    public Response resumen(@QueryParam("rol") String rol) {
        SesionesRest.Sesion sesion = sesion();
        Response acceso = acceso(sesion);
        if (acceso != null) return acceso;
        try {
            List<Notificacion> filtradas = notificacionBL.findAll().stream().filter(n -> propia(n, sesion)).toList();
            long noLeidas = filtradas.stream().filter(n -> !n.isEstadoLeida()).count();
            return Response.ok(Map.of("total", filtradas.size(), "noLeidas", noLeidas))
                    .header("Cache-Control", "no-store").build();
        } catch (BLException ex) {
            return responderError(ex);
        }
    }

    @GET
    @Path("/{id}")
    public Response consultar(@PathParam("id") int id) {
        SesionesRest.Sesion sesion = sesion();
        Response acceso = acceso(sesion);
        if (acceso != null) return acceso;
        if (id <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El ID de la notificación no es válido"))
                    .build();
        }
        try {
            Notificacion aviso = notificacionBL.findById(id);
            if (aviso == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "No existe la notificación " + id))
                        .build();
            }
            if (!propia(aviso, sesion)) return ajena();
            return Response.ok(dto(aviso)).header("Cache-Control", "no-store").build();
        } catch (BLException ex) {
            return responderError(ex);
        }
    }

    @POST
    @Path("/{id}/lectura")
    @Consumes(MediaType.WILDCARD)
    public Response marcarLecturaPost(@PathParam("id") int id) {
        return marcarLeidaInterno(id);
    }

    @PUT
    @Path("/{id}/leida")
    @Consumes(MediaType.WILDCARD)
    public Response marcarLeidaPut(@PathParam("id") int id) {
        return marcarLeidaInterno(id);
    }

    private Response marcarLeidaInterno(int id) {
        SesionesRest.Sesion sesion = sesion();
        Response acceso = acceso(sesion);
        if (acceso != null) return acceso;
        if (id <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El ID de la notificación no es válido"))
                    .build();
        }
        try {
            Notificacion aviso = notificacionBL.findById(id);
            if (aviso == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "No existe la notificación " + id))
                        .build();
            }
            if (!propia(aviso, sesion)) return ajena();
            notificacionBL.marcarLeida(id, true);
            return Response.noContent().build();
        } catch (BLException ex) {
            return responderError(ex);
        }
    }

    @POST
    @Path("/lectura")
    @Consumes(MediaType.WILDCARD)
    public Response marcarTodasLeidas(@QueryParam("rol") String rol) {
        SesionesRest.Sesion sesion = sesion();
        Response acceso = acceso(sesion);
        if (acceso != null) return acceso;
        try {
            List<Notificacion> filtradas = filtrarPorRol(notificacionBL.findAll(), rol);
            int marcadas = 0;
            for (Notificacion aviso : filtradas) {
                if (!aviso.isEstadoLeida()) {
                    notificacionBL.marcarLeida(aviso.getIdNotificacion(), true);
                    marcadas++;
                }
            }
            return Response.ok(Map.of("marcadas", marcadas)).build();
        } catch (BLException ex) {
            return responderError(ex);
        }
    }

    // ---- apoyo ----

    private List<Notificacion> filtrarPorRol(List<Notificacion> todas, String rol) {
        if (rol == null || rol.isBlank() || "todas".equalsIgnoreCase(rol)) {
            return todas;
        }
        String r = rol.trim().toLowerCase(Locale.ROOT);
        return todas.stream().filter(n -> {
            UsuarioPlataforma d = n.getDestinatario();
            if (d == null) return false;
            if (r.contains("admin")) return d instanceof Administrador;
            if (r.contains("tec") || r.contains("inst")) return d instanceof TecnicoInstrumentista;
            if (r.contains("vend")) return d instanceof Vendedor;
            return false;
        }).toList();
    }

    private NotificacionDTO dto(Notificacion n) {
        String perfil = "usuario";
        String destNombre = "Usuario";
        if (n.getDestinatario() instanceof Administrador a) {
            perfil = "administrador";
            destNombre = a.getUsername() != null ? a.getUsername() : "Administrador";
        } else if (n.getDestinatario() instanceof TecnicoInstrumentista t) {
            perfil = "instrumentista";
            destNombre = t.getUsername() != null ? t.getUsername() : "Instrumentista";
        } else if (n.getDestinatario() instanceof Vendedor v) {
            perfil = "vendedor";
            destNombre = v.getUsername() != null ? v.getUsername() : "Vendedor";
        }
        return new NotificacionDTO(n, destNombre, perfil);
    }

    private Response responderError(BLException ex) {
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
