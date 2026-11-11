package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.core.Response;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.rest.dto.ErrorDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/** Respuestas HTTP estandarizadas y parseo de parametros opcionales de los recursos REST. */
public final class RespuestaRest {

    private static final String MENSAJE_GENERICO = "Error interno del servidor";

    private RespuestaRest() {
    }

    public static Response ok(Object cuerpo) {
        return Response.ok(cuerpo).build();
    }

    public static Response badRequest(String mensaje) {
        return error(Response.Status.BAD_REQUEST, mensaje);
    }

    public static Response notFound(String mensaje) {
        return error(Response.Status.NOT_FOUND, mensaje);
    }

    public static Response error(Response.Status estado, String mensaje) {
        String texto = (mensaje == null || mensaje.isBlank()) ? MENSAJE_GENERICO : mensaje;
        return Response.status(estado).entity(new ErrorDTO(texto)).build();
    }

    /** Texto vacio o nulo equivale a null (sin filtrar). */
    public static String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    /** Entero opcional. Texto no numerico lanza BLException (se responde 400). */
    public static Integer enteroOpcional(String texto, String campo) throws BLException {
        String limpio = vacioANull(texto);
        if (limpio == null) {
            return null;
        }
        try {
            return Integer.valueOf(limpio);
        } catch (NumberFormatException e) {
            throw new BLException(campo + " debe ser un número entero", e);
        }
    }

    /** Inicio de rango. Acepta AAAA-MM-DD (00:00:00) o AAAA-MM-DDTHH:mm:ss. */
    public static LocalDateTime fechaDesde(String texto, String campo) throws BLException {
        return parsearFecha(texto, campo, false);
    }

    /** Fin de rango. Con solo fecha, incluye todo el dia (23:59:59). */
    public static LocalDateTime fechaHasta(String texto, String campo) throws BLException {
        return parsearFecha(texto, campo, true);
    }

    private static LocalDateTime parsearFecha(String texto, String campo, boolean finDeDia) throws BLException {
        String limpio = vacioANull(texto);
        if (limpio == null) {
            return null;
        }
        try {
            if (limpio.length() == 10) {
                LocalDate dia = LocalDate.parse(limpio);
                return finDeDia ? dia.atTime(23, 59, 59) : dia.atStartOfDay();
            }
            return LocalDateTime.parse(limpio);
        } catch (DateTimeParseException e) {
            throw new BLException("Formato de " + campo
                    + " no válido (use AAAA-MM-DD o AAAA-MM-DDTHH:mm:ss)", e);
        }
    }
}
