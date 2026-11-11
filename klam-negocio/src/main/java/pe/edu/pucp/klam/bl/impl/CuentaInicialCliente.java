package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;

/**
 * RF01: todo cliente tiene credenciales y no puede existir sin ellas.
 * Al registrar un cliente se le crea su cuenta de acceso en el mismo paso:
 *  - alta del administrador: con el usuario y la contraseña que él indica (obligatorios);
 *  - alta del vendedor u otra vía sin credenciales: cuenta inicial con usuario = DNI o RUC y
 *    contraseña = "Klam" + ese número (el cliente la cambia desde "Cambiar contraseña").
 */
final class CuentaInicialCliente {

    private static final String PREFIJO_PASSWORD = "Klam";

    private CuentaInicialCliente() {
    }

    static String usuarioInicial(String documento) {
        return documento == null ? "" : documento.trim();
    }

    static String passwordInicial(String documento) {
        return PREFIJO_PASSWORD + usuarioInicial(documento);
    }

    /** Verifica, sin guardar nada, que el usuario y la contraseña sirven (formato y usuario libre). */
    static void validar(String username, String password) throws BLException {
        new UsuarioPlataformaBLImpl().validarCredencialesNuevas(username, password);
    }

    static void crear(String tipoCliente, int idCliente, String username, String password) throws BLException {
        new UsuarioPlataformaBLImpl().habilitarCuentaCliente(tipoCliente, idCliente, username, password);
    }
}
