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

    /** Dirección de la plataforma (clave app.url de correo.properties); por defecto la de desarrollo. */
    private static String urlAcceso() {
        String base = "http://localhost:5153";
        try {
            String configurada = ResourceBundle.getBundle("correo").getString("app.url").trim();
            if (!configurada.isEmpty()) base = configurada;
        } catch (MissingResourceException ex) {
            // se usa la dirección por defecto
        }
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base + "/login";
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
