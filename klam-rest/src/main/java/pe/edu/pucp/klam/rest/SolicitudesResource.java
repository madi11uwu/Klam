package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.SolicitudCirugiaBL;
import pe.edu.pucp.klam.bl.UsuarioPlataformaBL;
import pe.edu.pucp.klam.bl.impl.SolicitudCirugiaBLImpl;
import pe.edu.pucp.klam.bl.impl.UsuarioPlataformaBLImpl;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;
import pe.edu.pucp.klam.rest.dto.AccionSolicitudDTO;

@Path("/solicitudes")
@Produces("application/json")
@Consumes("application/json")
public class SolicitudesResource {

    private final SolicitudCirugiaBL solicitudBL = new SolicitudCirugiaBLImpl();
    private final UsuarioPlataformaBL usuarioBL = new UsuarioPlataformaBLImpl();

    @POST
    @Path("/{id}/aprobacion")
    public Response aprobar(@PathParam("id") int id, AccionSolicitudDTO datos) {
        if (id <= 0) {
            return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        }
        if (sinCredenciales(datos)) {
            return error(Response.Status.UNAUTHORIZED, "Credenciales obligatorias");
        }
        UsuarioPlataforma usuario;
        try {
            usuario = usuarioBL.autenticar(datos.getUsername(), datos.getPassword());
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
        if (usuario == null) {
            return error(Response.Status.UNAUTHORIZED, "Credenciales incorrectas");
        }
        if (!"ADMINISTRADOR".equals(usuario.getRol())) {
            return error(Response.Status.FORBIDDEN, "No tiene permisos para aprobar solicitudes");
        }
        try {
            if (solicitudBL.findSolicitudById(id) == null) {
                return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            }
            solicitudBL.aprobarSolicitud(id);
            return Response.noContent().build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        }
    }

    @POST
    @Path("/{id}/rechazo")
    public Response rechazar(@PathParam("id") int id, AccionSolicitudDTO datos) {
        if (id <= 0) {
            return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        }
        if (sinCredenciales(datos)) {
            return error(Response.Status.UNAUTHORIZED, "Credenciales obligatorias");
        }
        UsuarioPlataforma usuario;
        try {
            usuario = usuarioBL.autenticar(datos.getUsername(), datos.getPassword());
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
        if (usuario == null) {
            return error(Response.Status.UNAUTHORIZED, "Credenciales incorrectas");
        }
        if (!"ADMINISTRADOR".equals(usuario.getRol())) {
            return error(Response.Status.FORBIDDEN, "No tiene permisos para rechazar solicitudes");
        }
        if (datos.getMotivo() == null || datos.getMotivo().isBlank()
                || datos.getMotivo().length() > 200) {
            return error(Response.Status.BAD_REQUEST, "El motivo de rechazo debe tener entre 1 y 200 caracteres");
        }
        try {
            if (solicitudBL.findSolicitudById(id) == null) {
                return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            }
            solicitudBL.rechazarSolicitud(id, datos.getMotivo());
            return Response.noContent().build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        }
    }

    @POST
    @Path("/{id}/cirugia")
    public Response registrarCirugia(@PathParam("id") int id, AccionSolicitudDTO datos) {
        if (id <= 0) {
            return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        }
        if (sinCredenciales(datos)) {
            return error(Response.Status.UNAUTHORIZED, "Credenciales obligatorias");
        }
        UsuarioPlataforma usuario;
        try {
            usuario = usuarioBL.autenticar(datos.getUsername(), datos.getPassword());
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
        if (usuario == null) {
            return error(Response.Status.UNAUTHORIZED, "Credenciales incorrectas");
        }
        if (!"ADMINISTRADOR".equals(usuario.getRol())) {
            return error(Response.Status.FORBIDDEN, "No tiene permisos para registrar cirugías");
        }
        try {
            if (solicitudBL.findSolicitudById(id) == null) {
                return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            }
            int idCirugia = solicitudBL.registrarCirugiaDesdeSolicitud(id, null, usuario.getUsername());
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("idCirugia", idCirugia))
                    .build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        }
    }

    private boolean sinCredenciales(AccionSolicitudDTO datos) {
        return datos == null || datos.getUsername() == null || datos.getUsername().isBlank()
                || datos.getPassword() == null || datos.getPassword().isBlank();
    }

    private Response fallo(BLException ex, Response.Status estadoNegocio) {
        return error(ex.getCause() == null ? estadoNegocio : Response.Status.INTERNAL_SERVER_ERROR,
                ex.getMessage());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(Map.of("error", mensaje)).build();
    }
}
