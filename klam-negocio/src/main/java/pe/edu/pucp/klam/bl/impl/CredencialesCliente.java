package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;

/** RF01: todo cliente nuevo se registra con usuario y contraseña. */
final class CredencialesCliente {

    private static final int MIN_USERNAME = 4;
    private static final int MAX_USERNAME = 50;

    private CredencialesCliente() {
    }

    /**
     * Alta sin usuario ni contraseña (p. ej. un vendedor registra un paciente): se asignan credenciales iniciales,
     * así todo cliente puede ingresar a la plataforma. Usuario = DNI o RUC; contraseña inicial = "Klam" + DNI o RUC.
     * El cliente debe cambiarla desde "Cambiar contraseña".
     */
    static void asignarInicialesSiFaltan(UsuarioPlataforma cliente, String documento) {
        boolean sinUsuario = cliente.getUsername() == null || cliente.getUsername().isBlank();
        boolean sinPassword = cliente.getPasswordHash() == null || cliente.getPasswordHash().isBlank();
        if (sinUsuario && sinPassword && documento != null && !documento.isBlank()) {
            cliente.setUsername(documento.trim());
            cliente.setPasswordHash("Klam" + documento.trim());
        }
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
