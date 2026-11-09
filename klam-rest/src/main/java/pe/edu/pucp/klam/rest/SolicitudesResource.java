
package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.SolicitudCirugiaBL;
import pe.edu.pucp.klam.bl.impl.SolicitudCirugiaBLImpl;
import pe.edu.pucp.klam.bl.UsuarioPlataformaBL;
import pe.edu.pucp.klam.bl.impl.UsuarioPlataformaBLImpl;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;
import pe.edu.pucp.klam.rest.dto.AccionSolicitudDTO;
import java.util.Map;

@Path("/solicitudes")
@Produces("application/json")
@Consumes("application/json")
public class SolicitudesResource {
    private final SolicitudCirugiaBL solicitudBL;

    private final UsuarioPlataformaBL usuarioBL;
    public SolicitudesResource() {
        solicitudBL = new SolicitudCirugiaBLImpl();
        usuarioBL = new UsuarioPlataformaBLImpl();
    }


    @POST
    @Path("/{id}/aprobacion")
    public Response aprobar(
            @PathParam("id") int id,
            AccionSolicitudDTO datos) {

        if (datos == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Datos obligatorios"))
                    .build();
        }

        try {
            UsuarioPlataforma usuario = usuarioBL.autenticar(
                    datos.getUsername(),
                    datos.getPassword()
            );

            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Credenciales incorrectas"))
                        .build();
            }

            if (!"ADMINISTRADOR".equals(usuario.getRol())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "No tiene permisos para aprobar solicitudes"))
                        .build();
            }

            solicitudBL.aprobarSolicitud(
                    id
            );

            return Response.noContent().build();

        } catch (BLException ex) {
              return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{id}/rechazo")
    public Response rechazar(
            @PathParam("id") int id,
            AccionSolicitudDTO datos) {

        if (datos == null ||
                datos.getMotivo() == null ||
                datos.getMotivo().isBlank()) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El motivo de rechazo es obligatorio"))
                    .build();
        }

        try {
            UsuarioPlataforma usuario = usuarioBL.autenticar(
                    datos.getUsername(),
                    datos.getPassword()
            );

            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Credenciales incorrectas"))
                        .build();
            }

            if (!"ADMINISTRADOR".equals(usuario.getRol())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "No tiene permisos para rechazar solicitudes"))
                        .build();
            }

            solicitudBL.rechazarSolicitud(
                    id,
                    datos.getMotivo()
            );

            return Response.noContent().build();

        } catch (BLException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        }
    }

}
