package pe.edu.pucp.klam.rest;

import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sesiones abiertas por POST /login (token opaco -> quién es). Viven en memoria del servidor:
 * al reiniciarlo hay que volver a iniciar sesión. Sirve a RequiereRol para verificar identidad y rol.
 */
final class SesionesActivas {

    record Sesion(int id, String username, String rol) {
    }

    private static final ConcurrentHashMap<String, Sesion> SESIONES = new ConcurrentHashMap<>();
    private static final String PREFIJO = "Bearer ";

    private SesionesActivas() {
    }

    static String crear(UsuarioPlataforma usuario) {
        String token = UUID.randomUUID().toString();
        SESIONES.put(token, new Sesion(usuario.getIdUsuario(), usuario.getUsername(), usuario.getRol()));
        return token;
    }

    /** Valor del encabezado Authorization ("Bearer <token>") -> sesión, o null si falta o no es válida. */
    static Sesion desdeEncabezado(String authorization) {
        String token = extraer(authorization);
        return token == null ? null : SESIONES.get(token);
    }

    static void cerrar(String authorization) {
        String token = extraer(authorization);
        if (token != null) {
            SESIONES.remove(token);
        }
    }

    private static String extraer(String authorization) {
        if (authorization == null || !authorization.startsWith(PREFIJO)) {
            return null;
        }
        String token = authorization.substring(PREFIJO.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
