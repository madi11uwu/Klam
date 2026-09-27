package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.PacienteParticularBL;
import pe.edu.pucp.klam.dao.PacienteParticularDAO;
import pe.edu.pucp.klam.dao.impl.PacienteParticularDAOImpl;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;

import java.sql.SQLException;
import java.util.List;

public class PacienteParticularBLImpl implements PacienteParticularBL {
    private final PacienteParticularDAO pacienteParticularDAO = new PacienteParticularDAOImpl();

    @Override
    public List<PacienteParticular> findAll() throws BLException {
        try {
            return pacienteParticularDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los pacientes particulares", e);
        }
    }

    @Override
    public PacienteParticular findById(Integer id) throws BLException {
        validarId(id);
        try {
            return pacienteParticularDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el paciente particular", e);
        }
    }

    @Override
    public void insert(PacienteParticular pacienteParticular) throws BLException {
        validarDatos(pacienteParticular);
        validarDniUnico(pacienteParticular);
        try {
            pacienteParticularDAO.insert(pacienteParticular);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar el paciente particular", e);
        }
    }

    @Override
    public void update(PacienteParticular pacienteParticular) throws BLException {
        validarDatos(pacienteParticular);
        validarExiste(pacienteParticular.getId_cliente());
        validarDniUnico(pacienteParticular);
        try {
            pacienteParticularDAO.update(pacienteParticular);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar el paciente particular", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarId(id);
        try {
            pacienteParticularDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar el paciente particular", e);
        }
    }

    private void validarDatos(PacienteParticular pacienteParticular) throws BLException {
        if (pacienteParticular == null) {
            throw new BLException("Debe proporcionar el paciente particular");
        }
        if (pacienteParticular.getDni() == null
                || !pacienteParticular.getDni().matches("\\d{8}")) {
            throw new BLException("El DNI debe tener exactamente 8 dígitos");
        }
    }

    private void validarId(Integer id) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("El id debe ser un número positivo");
        }
    }

    private void validarExiste(Integer id) throws BLException {
        validarId(id);
        try {
            if (pacienteParticularDAO.findById(id) == null) {
                throw new BLException("No existe el paciente particular con id " + id);
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de el paciente particular", e);
        }
    }

    private void validarDniUnico(PacienteParticular pacienteParticular) throws BLException {
        try {
            PacienteParticular existente = pacienteParticularDAO.findByDNI(pacienteParticular.getDni());
            if (existente != null && existente.getId_cliente() != pacienteParticular.getId_cliente()) {
                throw new BLException(
                        "Ya existe un registro con el DNI " + pacienteParticular.getDni());
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la unicidad del DNI", e);
        }
    }
}
