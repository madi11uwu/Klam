package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.ClinicaHospitalBL;
import pe.edu.pucp.klam.dao.ClinicaHospitalDAO;
import pe.edu.pucp.klam.dao.impl.ClinicaHospitalDAOImpl;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;

import java.sql.SQLException;
import java.util.List;

public class ClinicaHospitalBLImpl implements ClinicaHospitalBL {
    private final ClinicaHospitalDAO clinicaHospitalDAO;

    public ClinicaHospitalBLImpl() {
        this(new ClinicaHospitalDAOImpl());
    }

    private ClinicaHospitalBLImpl(ClinicaHospitalDAO clinicaHospitalDAO) {
        this.clinicaHospitalDAO = clinicaHospitalDAO;
    }

    protected void validarDatos(ClinicaHospital clinicaHospital) throws BLException {
        if (clinicaHospital == null) {
            throw new BLException("Debe proporcionar la clínica");
        }
        validarDatosCliente(clinicaHospital.getNombre(), clinicaHospital.getDireccion(),
                clinicaHospital.getEmailContacto(), clinicaHospital.getTelefono());
        validarLongitud(clinicaHospital.getPeriodoCredito(), 30, "El periodo de crédito");
        if (clinicaHospital.getRuc() == null
                || clinicaHospital.getRuc().length()!=11) {
            throw new BLException("El RUC debe tener exactamente 11 dígitos");
        }
    }

    protected void validarDocumentoUnico(ClinicaHospital clinicaHospital, boolean modificacion) throws BLException {
        try {
            ClinicaHospital existente = clinicaHospitalDAO.findByRUC(clinicaHospital.getRuc());
            if (existente != null && (!modificacion
                    || existente.getIdCliente() != clinicaHospital.getIdCliente())) {
                throw new BLException(
                        "Ya existe un registro con el RUC " + clinicaHospital.getRuc());
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la unicidad del RUC", e);
        }
    }

    @Override
    public List<ClinicaHospital> findAll() throws BLException {
        try {
            return clinicaHospitalDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar " + "las clínicas y hospitales", e);
        }
    }

    @Override
    public ClinicaHospital findById(Integer id) throws BLException {
        validarId(id);
        try {
            return clinicaHospitalDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar " + "la clínica u hospital", e);
        }
    }

    @Override
    public void insert(ClinicaHospital cliente) throws BLException {
        validarDatos(cliente);
        validarDocumentoUnico(cliente, false);
        try {
            clinicaHospitalDAO.insert(cliente);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar " + "la clínica u hospital", e);
        }
    }

    @Override
    public void update(ClinicaHospital cliente) throws BLException {
        validarDatos(cliente);
        validarExiste(cliente.getIdCliente());
        validarDocumentoUnico(cliente, true);
        try {
            clinicaHospitalDAO.update(cliente);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar " + "la clínica u hospital", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarId(id);
        try {
            clinicaHospitalDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar " + "la clínica u hospital", e);
        }
    }

    protected void validarExiste(Integer id) throws BLException {
        validarId(id);
        try {
            if (clinicaHospitalDAO.findById(id) == null) {
                throw new BLException("No existe " + "la clínica u hospital" + " con id " + id);
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de " + "la clínica u hospital", e);
        }
    }

    public void validarDatosCliente(String nombre, String direccion, String correo, String telefono)
            throws BLException {
        if (nombre == null || nombre.isBlank()) {
            throw new BLException("El nombre es obligatorio");
        }
        validarLongitud(nombre, 150, "El nombre");
        validarLongitud(direccion, 200, "La dirección");
        validarLongitud(correo, 120, "El correo de contacto");
        validarLongitud(telefono, 20, "El teléfono");
        if (correo != null && !correo.isBlank()
                && (!correo.contains("@") || !correo.contains("."))) {
            throw new BLException("El correo de contacto tiene un formato inválido");
        }
    }

    public void validarLongitud(String valor, int maximo, String campo) {
        if (valor != null && valor.length() > maximo) {
            throw new BLException(campo + " no puede exceder " + maximo + " caracteres");
        }
    }

    public void validarId(Integer id) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("El id debe ser un número positivo");
        }
    }
}
