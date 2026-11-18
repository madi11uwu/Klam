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
        String nuevoHash = Contrasenas.hashear(passwordNueva);
        if (usuario instanceof Administrador a) {
            a.setPasswordHash(nuevoHash);
            administradorBL.update(a);
        } else if (usuario instanceof Vendedor v) {
            v.setPasswordHash(nuevoHash);
            vendedorBL.update(v);
        } else if (usuario instanceof TecnicoInstrumentista t) {
            t.setPasswordHash(nuevoHash);
            tecnicoBL.update(t);
        } else if (usuario instanceof CuentaUsuario cuenta) {
            try {
                cuentaDAO.cambiarPassword(cuenta.getIdUsuario(), nuevoHash);
            } catch (SQLException ex) {
                throw new BLException("No se pudo actualizar la cuenta", ex);
            }
        } else {
            throw new BLException("Tipo de usuario no soportado");
        }
    }

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
    public CredencialInicial habilitarCuentaClienteConCorreo(String tipo, int idCliente, String username)
            throws BLException {
        Cliente cliente = buscarCliente(tipo, idCliente);
        String nombreUsuario = (username == null || username.isBlank())
                ? documentoDe(cliente)
                : username.trim();
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new BLException("El cliente no tiene documento: indique un nombre de usuario");
        }
        String temporal = CuentaInicialCliente.passwordTemporal();
        int idCuenta = habilitarCuentaCliente(tipo, idCliente, nombreUsuario, temporal);
        // El correo es el canal oficial; si no sale, la contrasena igual vuelve en la respuesta.
        CuentaInicialCliente.avisarPorCorreo(cliente.getNombre(), cliente.getEmailContacto(),
                nombreUsuario, temporal);
        return new CredencialInicial(idCuenta, nombreUsuario, temporal);
    }

    private Cliente buscarCliente(String tipo, int idCliente) throws BLException {
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
        return cliente;
    }

    private static String documentoDe(Cliente cliente) {
        if (cliente instanceof PacienteParticular paciente) {
            return CuentaInicialCliente.usuarioInicial(paciente.getDni());
        }
        if (cliente instanceof ClinicaHospital clinica) {
            return CuentaInicialCliente.usuarioInicial(clinica.getRuc());
        }
        return null;
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
            cuenta.setPasswordHash(Contrasenas.hashear(password));
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

    @Override
    public java.util.List<UsuarioPlataforma> listar(String rol, String estado, String busqueda) throws BLException {
        java.util.List<UsuarioPlataforma> todos = new java.util.ArrayList<>();
        todos.addAll(administradorBL.findAll());
        todos.addAll(vendedorBL.findAll());
        todos.addAll(tecnicoBL.findAll());

        String rolNorm = rol != null ? rol.trim().toUpperCase() : "";
        if ("INSTRUMENTISTA".equals(rolNorm)) {
            rolNorm = "TECNICO_INSTRUMENTISTA";
        }

        String estadoNorm = estado != null ? estado.trim().toLowerCase() : "";
        String busqNorm = busqueda != null ? busqueda.trim().toLowerCase() : "";

        java.util.List<UsuarioPlataforma> filtrados = new java.util.ArrayList<>();
        for (UsuarioPlataforma u : todos) {
            if (!rolNorm.isEmpty() && !rolNorm.equalsIgnoreCase("TODOS") && !rolNorm.equalsIgnoreCase("TODOS LOS ROLES")) {
                String uRol = u.getRol() != null ? u.getRol().toUpperCase() : "";
                if ("INSTRUMENTISTA".equals(uRol)) uRol = "TECNICO_INSTRUMENTISTA";
                if (!rolNorm.equals(uRol)) {
                    continue;
                }
            }

            if ("activo".equals(estadoNorm)) {
                if (!u.isActivo()) continue;
            } else if ("inactivo".equals(estadoNorm)) {
                if (u.isActivo()) continue;
            }

            if (!busqNorm.isEmpty()) {
                String nombreCompleto = (u.getNombreCompleto() != null ? u.getNombreCompleto() : "").toLowerCase();
                String username = (u.getUsername() != null ? u.getUsername() : "").toLowerCase();
                String email = (u.getEmail() != null ? u.getEmail() : "").toLowerCase();
                if (!nombreCompleto.contains(busqNorm) && !username.contains(busqNorm) && !email.contains(busqNorm)) {
                    continue;
                }
            }

            filtrados.add(u);
        }

        return filtrados;
    }

    @Override
    public UsuarioPlataforma buscarPorIdYRol(int id, String rol) throws BLException {
        if (id <= 0) {
            throw new BLException("ID de usuario inválido");
        }
        String rolNorm = rol != null ? rol.trim().toUpperCase() : "";
        if ("ADMINISTRADOR".equals(rolNorm)) {
            return administradorBL.findById(id);
        } else if ("VENDEDOR".equals(rolNorm)) {
            return vendedorBL.findById(id);
        } else if ("TECNICO_INSTRUMENTISTA".equals(rolNorm) || "INSTRUMENTISTA".equals(rolNorm)) {
            return tecnicoBL.findById(id);
        } else {
            return buscarPorId(id);
        }
    }

    @Override
    public UsuarioPlataforma buscarPorId(int id) throws BLException {
        if (id <= 0) {
            throw new BLException("ID de usuario inválido");
        }
        UsuarioPlataforma u = administradorBL.findById(id);
        if (u != null) return u;
        u = vendedorBL.findById(id);
        if (u != null) return u;
        u = tecnicoBL.findById(id);
        if (u != null) return u;
        return null;
    }

    @Override
    public UsuarioPlataforma buscarPorUsername(String username) throws BLException {
        if (username == null || username.isBlank()) return null;
        String un = username.trim();
        UsuarioPlataforma u = administradorBL.findByUsername(un);
        if (u != null) return u;
        u = vendedorBL.findByUsername(un);
        if (u != null) return u;
        u = tecnicoBL.findByUsername(un);
        if (u != null) return u;
        try {
            return cuentaDAO.findByUsername(un);
        } catch (SQLException ex) {
            throw new BLException("Error al consultar cuenta por username", ex);
        }
    }

    @Override
    public UsuarioPlataforma crearUsuario(UsuarioPlataforma usuario, String password) throws BLException {
        if (usuario == null) {
            throw new BLException("Los datos del usuario son obligatorios");
        }
        if (usuario.getNombres() == null || usuario.getNombres().isBlank()) {
            throw new BLException("Los nombres son obligatorios");
        }
        if (usuario.getApellidos() == null || usuario.getApellidos().isBlank()) {
            throw new BLException("Los apellidos son obligatorios");
        }
        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            throw new BLException("El nombre de usuario es obligatorio");
        }
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new BLException("El correo electrónico es obligatorio");
        }
        if (!usuario.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BLException("El formato del correo electrónico es inválido");
        }

        String username = usuario.getUsername().trim();
        if (buscarPorUsername(username) != null) {
            throw new BLException("El nombre de usuario '" + username + "' ya está registrado");
        }

        verificarEmailUnico(usuario.getEmail().trim(), 0, null);

        Contrasenas.validarFormato(password);
        usuario.setPasswordHash(Contrasenas.hashear(password));

        String rol = normalizarRol(usuario.getRol());
        usuario.setRol(rol);

        if ("ADMINISTRADOR".equals(rol)) {
            Administrador admin = new Administrador();
            copiarDatosComunes(usuario, admin);
            administradorBL.insert(admin);
            return administradorBL.findByUsername(username);
        } else if ("VENDEDOR".equals(rol)) {
            Vendedor vendedor = new Vendedor();
            copiarDatosComunes(usuario, vendedor);
            if (usuario instanceof Vendedor vOrig) {
                vendedor.setComisionAcumulada(vOrig.getComisionAcumulada());
            }
            vendedorBL.insert(vendedor);
            return vendedorBL.findByUsername(username);
        } else if ("TECNICO_INSTRUMENTISTA".equals(rol)) {
            TecnicoInstrumentista tecnico = new TecnicoInstrumentista();
            copiarDatosComunes(usuario, tecnico);
            if (usuario instanceof TecnicoInstrumentista tOrig) {
                tecnico.setEspecialidad(tOrig.getEspecialidad());
            }
            tecnicoBL.insert(tecnico);
            return tecnicoBL.findByUsername(username);
        } else {
            throw new BLException("Rol de usuario inválido: " + usuario.getRol());
        }
    }

    @Override
    public UsuarioPlataforma modificarUsuario(UsuarioPlataforma usuario, String nuevaPassword) throws BLException {
        if (usuario == null || usuario.getIdUsuario() <= 0) {
            throw new BLException("El usuario a modificar no es válido");
        }
        if (usuario.getNombres() == null || usuario.getNombres().isBlank()) {
            throw new BLException("Los nombres son obligatorios");
        }
        if (usuario.getApellidos() == null || usuario.getApellidos().isBlank()) {
            throw new BLException("Los apellidos son obligatorios");
        }
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new BLException("El correo electrónico es obligatorio");
        }
        if (!usuario.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BLException("El formato del correo electrónico es inválido");
        }

        String rol = normalizarRol(usuario.getRol());
        usuario.setRol(rol);

        verificarEmailUnico(usuario.getEmail().trim(), usuario.getIdUsuario(), rol);

        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            Contrasenas.validarFormato(nuevaPassword);
            usuario.setPasswordHash(Contrasenas.hashear(nuevaPassword));
        }

        if ("ADMINISTRADOR".equals(rol)) {
            Administrador a = administradorBL.findById(usuario.getIdUsuario());
            if (a == null) throw new BLException("No se encontró el administrador con ID: " + usuario.getIdUsuario());
            actualizarDatosComunes(usuario, a, nuevaPassword);
            administradorBL.update(a);
            return a;
        } else if ("VENDEDOR".equals(rol)) {
            Vendedor v = vendedorBL.findById(usuario.getIdUsuario());
            if (v == null) throw new BLException("No se encontró el vendedor con ID: " + usuario.getIdUsuario());
            actualizarDatosComunes(usuario, v, nuevaPassword);
            if (usuario instanceof Vendedor vOrig) {
                v.setComisionAcumulada(vOrig.getComisionAcumulada());
            }
            vendedorBL.update(v);
            return v;
        } else if ("TECNICO_INSTRUMENTISTA".equals(rol)) {
            TecnicoInstrumentista t = tecnicoBL.findById(usuario.getIdUsuario());
            if (t == null) throw new BLException("No se encontró el instrumentista con ID: " + usuario.getIdUsuario());
            actualizarDatosComunes(usuario, t, nuevaPassword);
            if (usuario instanceof TecnicoInstrumentista tOrig) {
                t.setEspecialidad(tOrig.getEspecialidad());
            }
            tecnicoBL.update(t);
            return t;
        } else {
            throw new BLException("Rol de usuario inválido: " + rol);
        }
    }

    @Override
    public void eliminarLogico(int id, String rol) throws BLException {
        if (id <= 0) throw new BLException("ID de usuario inválido");
        String rolNorm = normalizarRol(rol);
        if ("ADMINISTRADOR".equals(rolNorm)) {
            administradorBL.delete(id);
        } else if ("VENDEDOR".equals(rolNorm)) {
            vendedorBL.delete(id);
        } else if ("TECNICO_INSTRUMENTISTA".equals(rolNorm)) {
            tecnicoBL.delete(id);
        } else {
            UsuarioPlataforma u = buscarPorId(id);
            if (u == null) throw new BLException("Usuario no encontrado con ID: " + id);
            eliminarLogico(id, u.getRol());
        }
    }

    @Override
    public void reactivar(int id, String rol) throws BLException {
        if (id <= 0) throw new BLException("ID de usuario inválido");
        String rolNorm = normalizarRol(rol);
        if ("ADMINISTRADOR".equals(rolNorm)) {
            Administrador a = administradorBL.findById(id);
            if (a == null) throw new BLException("Administrador no encontrado");
            a.setActivo(true);
            administradorBL.update(a);
        } else if ("VENDEDOR".equals(rolNorm)) {
            Vendedor v = vendedorBL.findById(id);
            if (v == null) throw new BLException("Vendedor no encontrado");
            v.setActivo(true);
            vendedorBL.update(v);
        } else if ("TECNICO_INSTRUMENTISTA".equals(rolNorm)) {
            TecnicoInstrumentista t = tecnicoBL.findById(id);
            if (t == null) throw new BLException("Técnico no encontrado");
            t.setActivo(true);
            tecnicoBL.update(t);
        } else {
            UsuarioPlataforma u = buscarPorId(id);
            if (u == null) throw new BLException("Usuario no encontrado con ID: " + id);
            reactivar(id, u.getRol());
        }
    }

    @Override
    public java.util.List<TecnicoInstrumentista> listarInstrumentistasActivos() throws BLException {
        return tecnicoBL.findAll().stream()
                .filter(TecnicoInstrumentista::isActivo)
                .toList();
    }

    private String normalizarRol(String rol) {
        if (rol == null) return "VENDEDOR";
        String r = rol.trim().toUpperCase();
        if ("INSTRUMENTISTA".equals(r) || "TECNICO".equals(r) || "TECNICO_INSTRUMENTISTA".equals(r)) {
            return "TECNICO_INSTRUMENTISTA";
        }
        if ("ADMINISTRADOR".equals(r) || "ADMIN".equals(r)) {
            return "ADMINISTRADOR";
        }
        if ("VENDEDOR".equals(r)) {
            return "VENDEDOR";
        }
        return r;
    }

    private void copiarDatosComunes(UsuarioPlataforma origen, UsuarioPlataforma destino) {
        destino.setUsername(origen.getUsername().trim());
        destino.setPasswordHash(origen.getPasswordHash());
        destino.setEmail(origen.getEmail().trim());
        destino.setNombres(origen.getNombres().trim());
        destino.setApellidos(origen.getApellidos().trim());
        destino.setActivo(origen.isActivo());
    }

    private void actualizarDatosComunes(UsuarioPlataforma origen, UsuarioPlataforma destino, String nuevaPassword) {
        destino.setNombres(origen.getNombres().trim());
        destino.setApellidos(origen.getApellidos().trim());
        destino.setEmail(origen.getEmail().trim());
        destino.setActivo(origen.isActivo());
        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            destino.setPasswordHash(origen.getPasswordHash());
        }
    }

    private void verificarEmailUnico(String email, int excluirId, String excluirRol) throws BLException {
        if (email == null || email.isBlank()) return;
        java.util.List<UsuarioPlataforma> todos = new java.util.ArrayList<>();
        todos.addAll(administradorBL.findAll());
        todos.addAll(vendedorBL.findAll());
        todos.addAll(tecnicoBL.findAll());

        for (UsuarioPlataforma u : todos) {
            if (email.equalsIgnoreCase(u.getEmail())) {
                boolean esMismo = (u.getIdUsuario() == excluirId) &&
                        (excluirRol == null || normalizarRol(u.getRol()).equals(normalizarRol(excluirRol)));
                if (!esMismo) {
                    throw new BLException("El correo electrónico '" + email + "' ya está registrado por otro usuario");
                }
            }
        }
    }
}
