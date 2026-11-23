package pe.edu.pucp.klam.bl.impl;

import java.security.SecureRandom;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import pe.edu.pucp.klam.bl.CorreoBL;


final class CuentaInicialCliente {

    private static final String LETRAS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final String DIGITOS = "23456789";
    private static final int LARGO = 12;

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private static final CorreoBL CORREO = new CorreoBLImpl();

    private CuentaInicialCliente() {
    }

    static String usuarioInicial(String documento) {
        return documento == null ? "" : documento.trim();
    }

    static String passwordTemporal() {
        char[] clave = new char[LARGO];
        clave[0] = LETRAS.charAt(ALEATORIO.nextInt(LETRAS.length()));
        clave[1] = DIGITOS.charAt(ALEATORIO.nextInt(DIGITOS.length()));
        String todos = LETRAS + DIGITOS;
        for (int i = 2; i < LARGO; i++) {
            clave[i] = todos.charAt(ALEATORIO.nextInt(todos.length()));
        }
        for (int i = clave.length - 1; i > 0; i--) {
            int j = ALEATORIO.nextInt(i + 1);
            char temporal = clave[i];
            clave[i] = clave[j];
            clave[j] = temporal;
        }
        return new String(clave);
    }

    static void avisarPorCorreo(String nombre, String email, String usuario, String passwordTemporal) {
        CORREO.enviar(email, "BIOKLAM · Acceso a su cuenta",
                cuerpoCredenciales(nombre, usuario, passwordTemporal));
    }

    /**
     * Dirección de la plataforma para el enlace del correo. Se lee en cada envío, en este orden:
     * 1) primera línea del archivo klam-app-url.txt en la carpeta del usuario que corre el servidor
     * (se puede cambiar sin reiniciar); 2) propiedad de JVM app.url; 3) variable de entorno APP_URL;
     * 4) clave app.url de correo.properties; 5) la dirección de desarrollo.
     */
    private static String urlAcceso() {
        String base = leerArchivoUrl();
        if (base == null) base = vacioANulo(System.getProperty("app.url"));
        if (base == null) base = vacioANulo(System.getenv("APP_URL"));
        if (base == null) {
            try {
                base = vacioANulo(ResourceBundle.getBundle("correo").getString("app.url"));
            } catch (MissingResourceException ex) {
                // se usa la dirección por defecto
            }
        }
        if (base == null) base = "http://localhost:5153";
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base + "/login";
    }

    private static String leerArchivoUrl() {
        try {
            java.nio.file.Path ruta = java.nio.file.Paths.get(System.getProperty("user.home"), "klam-app-url.txt");
            if (!java.nio.file.Files.isRegularFile(ruta)) return null;
            java.util.List<String> lineas = java.nio.file.Files.readAllLines(ruta);
            return lineas.isEmpty() ? null : vacioANulo(lineas.get(0));
        } catch (java.io.IOException | RuntimeException ex) {
            return null;
        }
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.trim().isEmpty() ? null : valor.trim();
    }

    private static String cuerpoCredenciales(String nombre, String usuario, String passwordTemporal) {
        return "Estimado(a) " + (nombre == null ? "" : nombre.trim()) + ":\n\n"
                + "Se creó su cuenta en BIOKLAM. Ingrese desde este enlace:\n\n"
                + "    " + urlAcceso() + "\n\n"
                + "Sus datos de acceso son:\n\n"
                + "    Usuario: " + usuario + "\n"
                + "    Contraseña temporal: " + passwordTemporal + "\n\n"
                + "Por seguridad, cámbiela la primera vez que ingrese, desde la opción\n"
                + "Cambiar contraseña.\n\n"
                + "Este es un mensaje automático; por favor no responda a este correo.";
    }

}
