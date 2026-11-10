package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;

/**
 * Política de formato de las contraseñas de la plataforma. Se guardan y comparan en texto plano
 * (columna password_hash, igual que las cuentas de administrador, vendedor y técnico).
 */
final class Contrasenas {

    static final int MIN = 8;
    static final int MAX = 64;
    static final String REGLA = "La contraseña debe tener entre " + MIN + " y " + MAX
            + " caracteres, con al menos una letra y un número, sin espacios";

    private Contrasenas() {
    }

    /** Política de formato (la misma que muestra la pantalla). */
    static void validarFormato(String password) throws BLException {
        if (password == null || password.isEmpty()) {
            throw new BLException("La contraseña es obligatoria");
        }
        boolean largoOk = password.length() >= MIN && password.length() <= MAX;
        boolean sinEspacios = password.chars().noneMatch(Character::isWhitespace);
        boolean tieneLetra = password.chars().anyMatch(Character::isLetter);
        boolean tieneNumero = password.chars().anyMatch(Character::isDigit);
        if (!largoOk || !sinEspacios || !tieneLetra || !tieneNumero) {
            throw new BLException(REGLA);
        }
    }

    /** true si la contraseña escrita es igual a la guardada. */
    static boolean coincide(String password, String guardada) {
        return password != null && password.equals(guardada);
    }
}
