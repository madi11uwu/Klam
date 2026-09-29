package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.TecnicoInstrumentistaBL;
import pe.edu.pucp.klam.dao.TecnicoInstrumentistaDAO;
import pe.edu.pucp.klam.dao.impl.TecnicoInstrumentistaDAOImpl;
import pe.edu.pucp.klam.modelo.usuariosPermisos.TecnicoInstrumentista;

import java.sql.SQLException;
import java.util.List;

public class TecnicoInstrumentistaBLImpl implements TecnicoInstrumentistaBL {

    private final TecnicoInstrumentistaDAO tecnicoDAO = new TecnicoInstrumentistaDAOImpl();

    @Override
    public List<TecnicoInstrumentista> findAll() throws BLException {
        try {
            return tecnicoDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los técnicos instrumentistas", e);
        }
    }

    @Override
    public TecnicoInstrumentista findById(Integer id) throws BLException {
        if (id == null) {
            throw new BLException("El ID no puede ser nulo");
        }
        try {
            return tecnicoDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el técnico instrumentista con ID: " + id, e);
        }
    }

    @Override
    public void insert(TecnicoInstrumentista tecnico) throws BLException {
        validarDatos(tecnico);

        try {
            tecnicoDAO.insert(tecnico);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar el técnico instrumentista", e);
        }
    }

    @Override
    public void update(TecnicoInstrumentista tecnico) throws BLException {
        validarDatos(tecnico);
        validarExiste(tecnico.getIdUsuario());

        try {
            tecnicoDAO.update(tecnico);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar el técnico instrumentista", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarExiste(id);

        try {
            tecnicoDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar el técnico instrumentista", e);
        }
    }

    private void validarDatos(TecnicoInstrumentista tecnico) throws BLException {
        if (tecnico == null) {
            throw new BLException("El técnico instrumentista no puede ser nulo");
        }
        if (tecnico.getUsername() == null || tecnico.getUsername().isBlank()) {
            throw new BLException("El nombre de usuario (username) es obligatorio");
        }
        if (tecnico.getPasswordHash() == null || tecnico.getPasswordHash().isBlank()) {
            throw new BLException("La contraseña es obligatoria");
        }
        if (tecnico.getEmail() == null || tecnico.getEmail().isBlank()) {
            throw new BLException("El correo electrónico es obligatorio");
        }
        if (!tecnico.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BLException("El formato del correo electrónico es inválido");
        }
        if (tecnico.getNombres() == null || tecnico.getNombres().isBlank()) {
            throw new BLException("Los nombres del técnico instrumentista son obligatorios");
        }
        if (tecnico.getApellidos() == null || tecnico.getApellidos().isBlank()) {
            throw new BLException("Los apellidos del técnico instrumentista son obligatorios");
        }
        if (tecnico.getEspecialidad() == null || tecnico.getEspecialidad().isBlank()) {
            throw new BLException("La especialidad del técnico es obligatoria");
        }
    }

    private void validarExiste(Integer id) throws BLException {
        if (id == null) {
            throw new BLException("El ID no puede ser nulo");
        }
        try {
            if (tecnicoDAO.findById(id) == null) {
                throw new BLException("No existe un técnico instrumentista con el ID " + id);
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia del técnico instrumentista", e);
        }
    }
}