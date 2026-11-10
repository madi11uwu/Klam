package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.UsuarioPlataformaBL;
import pe.edu.pucp.klam.bl.impl.UsuarioPlataformaBLImpl;
import pe.edu.pucp.klam.rest.dto.CambioPasswordDTO;

/** Cuenta del usuario que inició sesión (cualquier rol). La sesión es la de SesionesRest (token de /login). */
@Path("/cuenta")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CuentaResource {

    private final UsuarioPlataformaBL usuarioBL = new UsuarioPlataformaBLImpl();

    /** Cambia la contraseña de quien tiene la sesión; la BL exige la actual y valida el formato de la nueva. */
    @PUT
    @Path("/password")
    public Response cambiarPassword(@HeaderParam("Authorization") String authorization, CambioPasswordDTO dto) {
        SesionesRest.Sesion sesion = SesionesRest.validar(authorization);
        if (sesion == null) {
            return Errores.error(Response.Status.UNAUTHORIZED, "Sesión inválida o expirada");
        }
        if (dto == null) {
            return Errores.error(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es obligatorio");
        }
        try {
            usuarioBL.cambiarPassword(sesion.username(), dto.getPasswordActual(), dto.getPasswordNueva());
            return Response.noContent().build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }
}
