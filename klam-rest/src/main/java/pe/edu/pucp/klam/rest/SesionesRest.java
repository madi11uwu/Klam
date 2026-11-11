package pe.edu.pucp.klam.rest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;
import pe.edu.pucp.klam.modelo.usuariosPermisos.CuentaUsuario;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;

// Token opaco de 256 bits; el servidor conserva solo su resumen durante ocho horas.
final class SesionesRest {
    private static final Duration DURACION = Duration.ofHours(8);
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final ConcurrentHashMap<String, Sesion> SESIONES = new ConcurrentHashMap<>();

    private SesionesRest() {
    }

    static Credencial emitir(UsuarioPlataforma usuario) {
        return emitir(usuario, DURACION);
    }

    static Credencial emitir(UsuarioPlataforma usuario, Duration duracion) {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant vence = Instant.now().plus(duracion);
        Integer idCliente = null;
        String tipoCliente = null;
        if (usuario instanceof CuentaUsuario cuenta && cuenta.getCliente() != null) {
            idCliente = cuenta.getCliente().getIdCliente();
            tipoCliente = cuenta.getCliente() instanceof ClinicaHospital
                    ? "CLINICA_HOSPITAL" : "PACIENTE_PARTICULAR";
        }
        SESIONES.put(resumen(token), new Sesion(
                usuario.getIdUsuario(), usuario.getUsername(), usuario.getRol(), vence,
                idCliente, tipoCliente));
        return new Credencial(token, vence);
    }

    static Sesion validar(String autorizacion) {
        if (autorizacion == null || !autorizacion.startsWith("Bearer ")) {
            return null;
        }
        String token = autorizacion.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            return null;
        }
        String clave = resumen(token);
        Sesion sesion = SESIONES.get(clave);
        if (sesion == null) {
            return null;
        }
        if (!sesion.vence().isAfter(Instant.now())) {
            SESIONES.remove(clave, sesion);
            return null;
        }
        return sesion;
    }

    static void revocar(String autorizacion) {
        if (autorizacion != null && autorizacion.startsWith("Bearer ")) {
            SESIONES.remove(resumen(autorizacion.substring("Bearer ".length()).trim()));
        }
    }

    private static String resumen(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible", ex);
        }
    }

    record Credencial(String token, Instant vence) {
    }

    record Sesion(int idUsuario, String username, String rol, Instant vence,
                  Integer idCliente, String tipoCliente) {
    }
}
