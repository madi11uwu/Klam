package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.*;
import java.sql.SQLException;
import pe.edu.pucp.klam.dao.CuentaUsuarioDAO;
import pe.edu.pucp.klam.dao.impl.CuentaUsuarioDAOImpl;
import pe.edu.pucp.klam.modelo.usuariosPermisos.CuentaUsuario;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Administrador;
import pe.edu.pucp.klam.modelo.usuariosPermisos.TecnicoInstrumentista;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;

public class UsuarioPlataformaBLImpl implements UsuarioPlataformaBL {
    private final AdministradorBL administradorBL =
            new AdministradorBLImpl();

    private final VendedorBL vendedorBL =
            new VendedorBLImpl();

    private final TecnicoInstrumentistaBL tecnicoBL =
            new TecnicoInstrumentistaBLImpl();
    private final CuentaUsuarioDAO cuentaDAO = new CuentaUsuarioDAOImpl();
    @Override
    public UsuarioPlataforma autenticar(
            String username,
            String password) throws BLException {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return null;
        }

        username = username.trim();

        UsuarioPlataforma usuario =
                administradorBL.findByUsername(username);

        if (usuario == null) {
            usuario = vendedorBL.findByUsername(username);
        }

        if (usuario == null) {
            usuario = tecnicoBL.findByUsername(username);
        }

        if (usuario == null) {
            try {
                usuario = cuentaDAO.findByUsername(username);
            } catch (SQLException ex) {
                throw new BLException("No se pudo consultar la cuenta", ex);
            }
        }

        if (usuario instanceof CuentaUsuario cuenta) {
            Cliente vinculado = cuenta.getCliente();
            Cliente cliente = vinculado instanceof PacienteParticular
                    ? new PacienteParticularBLImpl().findById(vinculado.getIdCliente())
                    : vinculado instanceof ClinicaHospital
                      ? new ClinicaHospitalBLImpl().findById(vinculado.getIdCliente()) : null;
            if (cliente == null || !cliente.isActivo()) return null;
            cuenta.setCliente(cliente);
        }
        if (usuario == null || !usuario.isActivo()) {
            return null;
        }

        return Contrasenas.coincide(password, usuario.getPasswordHash())
                ? usuario
                : null;
    }

    @Override
    public void cambiarPassword(String username, String passwordActual, String passwordNueva) throws BLException {
        if (passwordActual == null || passwordActual.isEmpty()) {
            throw new BLException("La contraseña actual es obligatoria");
        }
        Contrasenas.validarFormato(passwordNueva);
        if (passwordNueva.equals(passwordActual)) {
            throw new BLException("La nueva contraseña debe ser distinta de la actual");
        }
        UsuarioPlataforma usuario = autenticar(username, passwordActual);
        if (usuario == null) {
            throw new BLException("La contraseña actual es incorrecta");
        }
        if (usuario instanceof Administrador a) {
            a.setPasswordHash(passwordNueva);
            administradorBL.update(a);
        } else if (usuario instanceof Vendedor v) {
            v.setPasswordHash(passwordNueva);
            vendedorBL.update(v);
        } else if (usuario instanceof TecnicoInstrumentista t) {
            t.setPasswordHash(passwordNueva);
            tecnicoBL.update(t);
        } else if (usuario instanceof CuentaUsuario cuenta) {
            try {
                cuentaDAO.cambiarPassword(cuenta.getIdUsuario(), passwordNueva);
            } catch (SQLException ex) {
                throw new BLException("No se pudo actualizar la cuenta", ex);
            }
        } else {
            throw new BLException("Tipo de usuario no soportado");
        }
    }
    /**
     * Comprueba, sin guardar nada, que un usuario y una contraseña sirven para una cuenta nueva de cliente:
     * usuario de 4 a 50 caracteres (letras, números, punto, guion y guion bajo), contraseña con el formato de la
     * plataforma y nombre de usuario que no esté en uso (personal ni otras cuentas).
     */
    public void validarCredencialesNuevas(String username, String password) throws BLException {
        if (username == null || username.isBlank()) {
            throw new BLException("El usuario es obligatorio");
        }
        String nombre = username.trim();
        if (nombre.length() < 4 || nombre.length() > 50 || !nombre.matches("[A-Za-z0-9._-]+")) {
            throw new BLException("El usuario debe tener entre 4 y 50 caracteres (letras, números, punto, guion y guion bajo)");
        }
        Contrasenas.validarFormato(password);
        if (administradorBL.findByUsername(nombre) != null
                || vendedorBL.findByUsername(nombre) != null
                || tecnicoBL.findByUsername(nombre) != null) {
            throw new BLException("El nombre de usuario ya está registrado");
        }
        try {
            if (cuentaDAO.findByUsername(nombre) != null) {
                throw new BLException("El nombre de usuario ya está registrado");
            }
        } catch (SQLException ex) {
            throw new BLException("No se pudo verificar el nombre de usuario", ex);
        }
    }

    @Override
    public int habilitarCuentaCliente(String tipo, int idCliente, String username, String password)
            throws BLException {
        if (idCliente <= 0 || username == null || username.isBlank() || username.trim().length() > 50) {
            throw new BLException("Cliente y nombre de usuario obligatorios");
        }
        Contrasenas.validarFormato(password);
        String nombreUsuario = username.trim();
        if (administradorBL.findByUsername(nombreUsuario) != null
                || vendedorBL.findByUsername(nombreUsuario) != null
                || tecnicoBL.findByUsername(nombreUsuario) != null) {
            throw new BLException("El nombre de usuario ya está registrado");
        }
        try {
            if (cuentaDAO.findByUsername(nombreUsuario) != null) {
                throw new BLException("El nombre de usuario ya está registrado");
            }
            Cliente cliente;
            if ("PACIENTE_PARTICULAR".equals(tipo)) {
                cliente = new PacienteParticularBLImpl().findById(idCliente);
            } else if ("CLINICA_HOSPITAL".equals(tipo)) {
                cliente = new ClinicaHospitalBLImpl().findById(idCliente);
            } else {
                throw new BLException("Tipo de cliente inválido");
            }
            if (cliente == null || !cliente.isActivo()) {
                throw new BLException("El cliente no existe o está inactivo");
            }
            CuentaUsuario cuenta = new CuentaUsuario();
            cuenta.setCliente(cliente);
            cuenta.setUsername(nombreUsuario);
            cuenta.setPasswordHash(password);
            cuenta.setEmail(cliente.getEmailContacto());
            cuenta.setNombres(cliente.getNombre());
            cuenta.setApellidos("");
            cuenta.setActivo(true);
            cuentaDAO.insert(cuenta);
            return cuenta.getIdUsuario();
        } catch (SQLException ex) {
            throw new BLException("No se pudo habilitar la cuenta del cliente", ex);
        }
    }
}
