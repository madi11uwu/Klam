package pe.edu.pucp.klam.bl.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import pe.edu.pucp.klam.bl.BLException;


final class Contrasenas {

    static final int MIN = 8;
    static final int MAX = 64;
    static final String REGLA = "La contraseña debe tener entre " + MIN + " y " + MAX
            + " caracteres, con al menos una letra y un número, sin espacios";

    static final String ETIQUETA = "pbkdf2-sha256";

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 600_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_CLAVE = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Contrasenas() {
    }

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

    static String hashear(String password) throws BLException {
        if (password == null || password.isEmpty()) {
            throw new BLException("La contraseña es obligatoria");
        }
        byte[] sal = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] hash = derivar(password, sal, ITERACIONES);
        Base64.Encoder base64 = Base64.getEncoder();
        return ETIQUETA + "$" + ITERACIONES
                + "$" + base64.encodeToString(sal)
                + "$" + base64.encodeToString(hash);
    }

    /** true si la contraseña escrita corresponde al valor guardado. */
    static boolean coincide(String password, String guardada) {
        if (password == null || guardada == null || guardada.isEmpty()) {
            return false;
        }
        if (requiereMigracion(guardada)) {
            return password.equals(guardada);
        }
        String[] partes = guardada.split("\\$");
        if (partes.length != 4) {
            return false;
        }
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] obtenido = derivar(password, sal, iteraciones);
            return MessageDigest.isEqual(esperado, obtenido);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    static boolean requiereMigracion(String guardada) {
        return guardada == null || !guardada.startsWith(ETIQUETA + "$");
    }

    private static byte[] derivar(String password, byte[] sal, int iteraciones) throws BLException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), sal, iteraciones, BITS_CLAVE);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new BLException("No se pudo procesar la contraseña", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
