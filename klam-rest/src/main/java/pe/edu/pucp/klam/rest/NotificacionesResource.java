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
 * Cada usuario del personal ve solo su bandeja: idUsuario y rol (ADMINISTRADOR, VENDEDOR,
 * TECNICO_INSTRUMENTISTA) llegan desde la sesión de Blazor.
 *
 * GET  /notificaciones?idUsuario=&rol=&estado=no-leidas|leidas
 * GET  /notificaciones/resumen?idUsuario=&rol=     {total, noLeidas}
 * GET  /notificaciones/{id}?idUsuario=&rol=        detalle de un aviso propio
 * POST /notificaciones/{id}/lectura?idUsuario=&rol= marca un aviso propio como leído
 * PUT  /notificaciones/{id}/leida?idUsuario=&rol=   marca un aviso propio como leído
 * POST /notificaciones/lectura?idUsuario=&rol=      marca como leídos todos los avisos propios
 *
 **/


@Path("/notificaciones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificacionesResource {

    // El aviso es del usuario si coincide el id y el tipo de destinatario (cada perfil vive en su tabla)
    private boolean propia(Notificacion n, int idUsuario, String rol) {
        UsuarioPlataforma d = n.getDestinatario();
        if (d == null || rol == null || d.getIdUsuario() != idUsuario) {
            return false;
        }
        String r = rol.trim().toLowerCase(Locale.ROOT);
        if (r.contains("admin")) return d instanceof Administrador;
        if (r.contains("tec") || r.contains("inst")) return d instanceof TecnicoInstrumentista;
        if (r.contains("vend")) return d instanceof Vendedor;
        return false;
    }
    private Response ajena() { return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", "Notificación ajena")).build(); }

    private final NotificacionBL notificacionBL = new NotificacionBLImpl();

    @GET
    public Response listar(@QueryParam("idUsuario") int idUsuario, @QueryParam("rol") String rol,
                           @QueryParam("estado") String estado) {
        boolean soloNoLeidas = "no-leidas".equalsIgnoreCase(estado);
        boolean soloLeidas = "leidas".equalsIgnoreCase(estado);
        if (estado != null && !estado.isBlank() && !soloNoLeidas && !soloLeidas && !"todas".equalsIgnoreCase(estado)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El estado debe ser 'leidas', 'no-leidas' o 'todas'"))
                    .build();
        }
        try {
            List<Notificacion> todas = notificacionBL.findAll();
            List<NotificacionDTO> avisos = todas.stream().filter(n -> propia(n, idUsuario, rol))
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
    public Response resumen(@QueryParam("idUsuario") int idUsuario, @QueryParam("rol") String rol) {
        try {
            List<Notificacion> filtradas = notificacionBL.findAll().stream().filter(n -> propia(n, idUsuario, rol)).toList();
            long noLeidas = filtradas.stream().filter(n -> !n.isEstadoLeida()).count();
            return Response.ok(Map.of("total", filtradas.size(), "noLeidas", noLeidas))
                    .header("Cache-Control", "no-store").build();
        } catch (BLException ex) {
            return responderError(ex);
        }
    }

    @GET
    @Path("/{id}")
    public Response consultar(@PathParam("id") int id, @QueryParam("idUsuario") int idUsuario,
                              @QueryParam("rol") String rol) {
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
            if (!propia(aviso, idUsuario, rol)) return ajena();
            return Response.ok(dto(aviso)).header("Cache-Control", "no-store").build();
        } catch (BLException ex) {
            return responderError(ex);
        }
    }

    @POST
    @Path("/{id}/lectura")
    @Consumes(MediaType.WILDCARD)
    public Response marcarLecturaPost(@PathParam("id") int id, @QueryParam("idUsuario") int idUsuario,
                                      @QueryParam("rol") String rol) {
        return marcarLeidaInterno(id, idUsuario, rol);
    }

    @PUT
    @Path("/{id}/leida")
    @Consumes(MediaType.WILDCARD)
    public Response marcarLeidaPut(@PathParam("id") int id, @QueryParam("idUsuario") int idUsuario,
                                   @QueryParam("rol") String rol) {
        return marcarLeidaInterno(id, idUsuario, rol);
    }

    private Response marcarLeidaInterno(int id, int idUsuario, String rol) {
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
            if (!propia(aviso, idUsuario, rol)) return ajena();
            notificacionBL.marcarLeida(id, true);
            return Response.noContent().build();
        } catch (BLException ex) {
            return responderError(ex);
        }
    }

    @POST
    @Path("/lectura")
    @Consumes(MediaType.WILDCARD)
    public Response marcarTodasLeidas(@QueryParam("idUsuario") int idUsuario, @QueryParam("rol") String rol) {
        try {
            // Solo los avisos propios (antes marcaba los de todos los usuarios del mismo perfil)
            List<Notificacion> filtradas = notificacionBL.findAll().stream().filter(n -> propia(n, idUsuario, rol)).toList();
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
