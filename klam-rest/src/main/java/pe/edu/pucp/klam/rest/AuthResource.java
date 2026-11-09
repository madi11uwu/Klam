package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.UsuarioPlataformaBL;
import pe.edu.pucp.klam.bl.impl.UsuarioPlataformaBLImpl;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    private final UsuarioPlataformaBL usuarioBL = new UsuarioPlataformaBLImpl();

    @Context
    private HttpHeaders headers;

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

            SesionesRest.Credencial credencial = SesionesRest.emitir(usuario);
            return Response.ok(Map.of(
                    "id", usuario.getIdUsuario(),
                    "userName", usuario.getUsername(),
                    "rol", usuario.getRol(),
                    "token", credencial.token(),
                    "expiresAt", credencial.vence().toString()
            )).header("Cache-Control", "no-store").build();

        } catch (BLException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al autenticar usuario"))
                    .build();
        }
    }

    @POST
    @Path("/logout")
    @Consumes(MediaType.WILDCARD)
    public Response logout() {
        String autorizacion = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (SesionesRest.validar(autorizacion) == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Sesión inválida o expirada"))
                    .build();
        }
        SesionesRest.revocar(autorizacion);
        return Response.noContent().header("Cache-Control", "no-store").build();
    }
}
