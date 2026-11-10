package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.core.Response;
import pe.edu.pucp.klam.bl.BLException;

import java.sql.SQLException;
import java.util.Map;

/** Respuestas de error comunes: {"error": "mensaje"}. */
final class Errores {

    private Errores() {
    }

    static Response desde(BLException ex) {
        Throwable causa = ex;
        while (causa != null) {
            if (causa instanceof SQLException) {
                return error(Response.Status.INTERNAL_SERVER_ERROR, "Error al acceder a la base de datos");
            }
            causa = causa.getCause();
        }
        // Validación y reglas de negocio
        return error(Response.Status.BAD_REQUEST, ex.getMessage());
    }

    static Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(Map.of("error", mensaje)).build();
    }
}
