package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.UsuarioPlataformaBL;
import pe.edu.pucp.klam.bl.impl.UsuarioPlataformaBLImpl;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.usuariosPermisos.CuentaUsuario;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;

@Path("/login")
@Produces("application/json")
@Consumes("application/json")
public class AuthResource {
    private final UsuarioPlataformaBL usuarioBL;

    public AuthResource() {
        usuarioBL = new UsuarioPlataformaBLImpl();
    }

    @POST
    public Response login(Map<String, String> credenciales) {
        try {
            if (credenciales == null) {
                throw new BLException("Credenciales obligatorias");
            }
            UsuarioPlataforma usuario = usuarioBL.autenticar(credenciales.get("userName"), credenciales.get("password"));
            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Usuario o contraseña incorrectos"))
                        .build();
            }
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id", usuario.getIdUsuario());
            respuesta.put("userName", usuario.getUsername());
            respuesta.put("rol", usuario.getRol());
            // La cuenta de un cliente (clínica o paciente) lleva al cliente al que pertenece
            if (usuario instanceof CuentaUsuario && ((CuentaUsuario) usuario).getCliente() != null) {
                CuentaUsuario cuenta = (CuentaUsuario) usuario;
                respuesta.put("idCliente", cuenta.getCliente().getIdCliente());
                respuesta.put("tipoCliente", cuenta.getCliente() instanceof ClinicaHospital
                        ? "CLINICA_HOSPITAL" : "PACIENTE_PARTICULAR");
            }
            return Response.ok(respuesta).build();
        }
        catch (BLException ex) {
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
}
