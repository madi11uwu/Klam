package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.usuariosPermisos.TecnicoInstrumentista;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;

import java.util.List;

public interface UsuarioPlataformaBL {
    UsuarioPlataforma autenticar(
            String username,
            String password
    ) throws BLException;

    /**
     * Cambia la contraseña del usuario (cualquier rol). Exige la contraseña actual, que la nueva cumpla la
     * política (8 a 64 caracteres, al menos una letra y un número, sin espacios) y que sea distinta.
     */
    void cambiarPassword(String username, String passwordActual, String passwordNueva) throws BLException;

    int habilitarCuentaCliente(String tipo, int idCliente, String username, String password) throws BLException;

    /**
     * RF01 + RF03: el administrador habilita la cuenta de un cliente ya registrado.
     * El sistema genera la contrasena temporal (nunca la teclea el administrador),
     * crea la cuenta y le avisa al cliente por correo. Devuelve esa contrasena una
     * sola vez para poder mostrarsela en pantalla, porque el envio de correo es
     * asincrono y silencioso y no hay forma de recuperarla despues.
     *
     * @param username nombre de usuario; si viene vacio se usa el RUC o DNI del cliente.
     */
    CredencialInicial habilitarCuentaClienteConCorreo(String tipo, int idCliente, String username) throws BLException;

    /** Credenciales recien generadas para un cliente. Solo viajan una vez. */
    record CredencialInicial(int idCuenta, String usuario, String passwordTemporal) {
    }

    /**
     * Lista los usuarios del personal (administradores, vendedores, instrumentistas)
     * con filtros opcionales por rol, estado y texto de búsqueda.
     */
    List<UsuarioPlataforma> listar(String rol, String estado, String busqueda) throws BLException;

    /**
     * Busca un usuario por su ID y rol específico.
     */
    UsuarioPlataforma buscarPorIdYRol(int id, String rol) throws BLException;

    /**
     * Busca un usuario por su ID intentando en todos los roles del personal.
     */
    UsuarioPlataforma buscarPorId(int id) throws BLException;

    /**
     * Busca un usuario por su username en todas las cuentas de la plataforma.
     */
    UsuarioPlataforma buscarPorUsername(String username) throws BLException;

    /**
     * Registra un nuevo usuario del personal (Administrador, Vendedor o Técnico Instrumentista).
     * Valida unicidad de username y email, valida y hashea la contraseña.
     */
    UsuarioPlataforma crearUsuario(UsuarioPlataforma usuario, String password) throws BLException;

    /**
     * Actualiza los datos de un usuario existente. Si nuevaPassword no es nula ni vacía,
     * valida y hashea la nueva contraseña.
     */
    UsuarioPlataforma modificarUsuario(UsuarioPlataforma usuario, String nuevaPassword) throws BLException;

    /**
     * Desactivación lógica (baja lógica) de un usuario.
     */
    void eliminarLogico(int id, String rol) throws BLException;

    /**
     * Reactiva una cuenta desactivada.
     */
    void reactivar(int id, String rol) throws BLException;

    /**
     * Retorna únicamente los técnicos instrumentistas activos para su asignación en cirugías.
     */
    List<TecnicoInstrumentista> listarInstrumentistasActivos() throws BLException;
}

