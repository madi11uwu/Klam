package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.UsuarioPlataformaBL;
import pe.edu.pucp.klam.bl.impl.UsuarioPlataformaBLImpl;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;

import java.util.Map;

@Path("/login")
@Produces("application/json")
@Consumes("application/json")
public class AuthResource {
    private final UsuarioPlataformaBL usuarioBL =
            new UsuarioPlataformaBLImpl();

    @POST
    public Response login(Map<String, String> credenciales) {
        if (credenciales == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Credenciales obligatorias"))
                    .build();
        }

        try {
            UsuarioPlataforma usuario = usuarioBL.autenticar(
                    credenciales.get("userName"),
                    credenciales.get("password")
            );

            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Credenciales incorrectas"))
                        .build();
            }

            return Response.ok(Map.of(
                    "id", usuario.getIdUsuario(),
                    "userName", usuario.getUsername(),
                    "rol", usuario.getRol(),
                    "token", SesionesActivas.crear(usuario)
            )).build();

        } catch (BLException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al autenticar usuario"))
                    .build();
        }
    }

    /** Cierra la sesión: invalida el token en el servidor. */
    @DELETE
    public Response logout(@HeaderParam("Authorization") String authorization) {
        SesionesActivas.cerrar(authorization);
        return Response.noContent().build();
    }
}
