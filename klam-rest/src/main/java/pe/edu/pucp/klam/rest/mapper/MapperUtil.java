package pe.edu.pucp.klam.rest.mapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Conversiones comunes de valores que llegan desde la BL (mapas JDBC). */
public final class MapperUtil {

    private static final DateTimeFormatter FORMATO_ISO = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");

    private MapperUtil() {
    }

    /** Fecha-hora como texto ISO-8601; null se conserva como null. */
    public static String aIso(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof Timestamp ts) {
            return ts.toLocalDateTime().format(FORMATO_ISO);
        }
        if (valor instanceof LocalDateTime ldt) {
            return ldt.format(FORMATO_ISO);
        }
        return valor.toString();
    }

    /** Texto; null se conserva como null. */
    public static String aTexto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    /** Entero obligatorio: el dato debe existir en la fila. */
    public static int aEntero(Object valor) {
        if (valor == null) {
            throw new IllegalStateException("Campo entero obligatorio ausente en la fila");
        }
        if (valor instanceof Number numero) {
            return numero.intValue();
        }
        return Integer.parseInt(valor.toString());
    }
}
