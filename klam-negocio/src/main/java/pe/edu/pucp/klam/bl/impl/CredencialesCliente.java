package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;

/** RF01: todo cliente nuevo se registra con usuario y contraseña. */
final class CredencialesCliente {

    private static final int MIN_USERNAME = 4;
    private static final int MAX_USERNAME = 50;

    private CredencialesCliente() {
    }

    static void validarFormato(String username, String password) throws BLException {
        if (username == null || username.isBlank()) {
            throw new BLException("El usuario es obligatorio");
        }
        String u = username.trim();
        if (u.length() < MIN_USERNAME || u.length() > MAX_USERNAME) {
            throw new BLException("El usuario debe tener entre " + MIN_USERNAME + " y " + MAX_USERNAME
                    + " caracteres (letras, números, punto, guion y guion bajo)");
        }
        if (!u.matches("[A-Za-z0-9._-]+")) {
            throw new BLException("El usuario solo puede tener letras, números, punto, guion y guion bajo");
        }
        Contrasenas.validarFormato(password);
    }
}
