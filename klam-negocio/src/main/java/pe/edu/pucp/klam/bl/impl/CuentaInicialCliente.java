package pe.edu.pucp.klam.bl.impl;

import java.security.SecureRandom;
import pe.edu.pucp.klam.bl.BLException;

final class CuentaInicialCliente {

    private static final String LETRAS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final String DIGITOS = "23456789";
    private static final int LARGO = 12;

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private CuentaInicialCliente() {
    }

    static String usuarioInicial(String documento) {
        return documento == null ? "" : documento.trim();
    }

    static String passwordTemporal() {
        char[] clave = new char[LARGO];
        // Se asegura la composición mínima antes de rellenar el resto.
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

    static void validar(String username, String password) throws BLException {
        new UsuarioPlataformaBLImpl().validarCredencialesNuevas(username, password);
    }

    static void crear(String tipoCliente, int idCliente, String username, String password) throws BLException {
        new UsuarioPlataformaBLImpl().habilitarCuentaCliente(tipoCliente, idCliente, username, password);
    }
}
