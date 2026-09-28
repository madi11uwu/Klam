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
        validarDniUnico(pacienteParticular, false);
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
        validarDniUnico(pacienteParticular, true);
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
        validarDatosCliente(pacienteParticular.getNombre(), pacienteParticular.getDireccion(),
                pacienteParticular.getEmailContacto(), pacienteParticular.getTelefono());
        if (pacienteParticular.getDni() == null
                || !pacienteParticular.getDni().matches("\\d{8}")) {
            throw new BLException("El DNI debe tener exactamente 8 dígitos");
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

    private void validarDniUnico(PacienteParticular pacienteParticular, boolean modificacion) throws BLException {
        try {
            PacienteParticular existente = pacienteParticularDAO.findByDNI(pacienteParticular.getDni());
            if (existente != null && (!modificacion
                    || existente.getId_cliente() != pacienteParticular.getId_cliente())) {
                throw new BLException(
                        "Ya existe un registro con el DNI " + pacienteParticular.getDni());
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la unicidad del DNI", e);
        }
    }

    @Override
    public void validarDatosCliente(String nombre, String direccion, String correo, String telefono)
            throws BLException {
        if (nombre == null || nombre.isBlank()) {
            throw new BLException("El nombre es obligatorio");
        }
        validarLongitud(nombre, 150, "El nombre");
        validarLongitud(direccion, 200, "La dirección");
        validarLongitud(correo, 120, "El correo de contacto");
        validarLongitud(telefono, 20, "El teléfono");
        if (correo != null && !correo.isEmpty()
                && !correo.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new BLException("El correo de contacto tiene un formato inválido");
        }
    }

    @Override
    public void validarLongitud(String valor, int maximo, String campo) {
        if (valor != null && valor.codePointCount(0, valor.length()) > maximo) {
            throw new BLException(campo + " no puede exceder " + maximo + " caracteres");
        }
    }

    @Override
    public void validarId(Integer id) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("El id debe ser un número positivo");
        }
    }
}
