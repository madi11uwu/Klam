package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.AdministradorBL;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.dao.AdministradorDAO;
import pe.edu.pucp.klam.dao.impl.AdministradorDAOImpl;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Administrador;

import java.sql.SQLException;
import java.util.List;

public class AdministradorBLImpl implements AdministradorBL {

    private final AdministradorDAO administradorDAO = new AdministradorDAOImpl();

    @Override
    public List findAll() throws BLException {
        try {
            return administradorDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los administradores", e);
        }
    }

    @Override
    public Administrador findById(Integer id) throws BLException {
        if (id == null) {
            throw new BLException("El ID no puede ser nulo");
        }
        try {
            return administradorDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el administrador con ID: " + id, e);
        }
    }

    @Override
    public void insert(Administrador administrador) throws BLException {
        validarDatos(administrador);

        try {
            administradorDAO.insert(administrador);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar el administrador", e);
        }
    }

    @Override
    public void update(Administrador administrador) throws BLException {
        validarDatos(administrador);
        validarExiste(administrador.getIdUsuario());

        try {
            administradorDAO.update(administrador);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar el administrador", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarExiste(id);

        try {
            administradorDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar el administrador", e);
        }
    }

    private void validarDatos(Administrador administrador) throws BLException {
        if (administrador == null) {
            throw new BLException("El administrador no puede ser nulo");
        }
        if (administrador.getUsername() == null || administrador.getUsername().isBlank()) {
            throw new BLException("El nombre de usuario (username) es obligatorio");
        }
        if (administrador.getEmail() == null || administrador.getEmail().isBlank()) {
            throw new BLException("El correo electrónico es obligatorio");
        }
        if (!administrador.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BLException("El formato del correo electrónico es inválido");
        }
        if (administrador.getNombres() == null || administrador.getNombres().isBlank()) {
            throw new BLException("Los nombres del administrador son obligatorios");
        }
        if (administrador.getApellidos() == null || administrador.getApellidos().isBlank()) {
            throw new BLException("Los apellidos del administrador son obligatorios");
        }
    }

    private void validarExiste(Integer id) throws BLException {
        if (id == null) {
            throw new BLException("El ID no puede ser nulo");
        }
        try {
            if (administradorDAO.findById(id) == null) {
                throw new BLException("No existe un administrador con el ID " + id);
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia del administrador", e);
        }
    }
}