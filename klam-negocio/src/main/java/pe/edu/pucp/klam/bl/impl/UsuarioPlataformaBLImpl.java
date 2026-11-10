package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.*;
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
    private final ClinicaHospitalBL clinicaBL =
            new ClinicaHospitalBLImpl();
    private final PacienteParticularBL pacienteBL =
            new PacienteParticularBLImpl();
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

        // Los clientes también son usuarios de la plataforma (RF01)
        if (usuario == null) {
            usuario = clinicaBL.findByUsername(username);
        }

        if (usuario == null) {
            usuario = pacienteBL.findByUsername(username);
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
        } else if (usuario instanceof ClinicaHospital c) {
            clinicaBL.guardarPasswordHash(c.getIdCliente(), c.getUsername(), passwordNueva);
        } else if (usuario instanceof PacienteParticular p) {
            pacienteBL.guardarPasswordHash(p.getIdCliente(), p.getUsername(), passwordNueva);
        } else {
            throw new BLException("Tipo de usuario no soportado");
        }
    }
}
