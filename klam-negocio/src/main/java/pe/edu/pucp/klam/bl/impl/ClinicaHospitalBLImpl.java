package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.ClinicaHospitalBL;
import pe.edu.pucp.klam.dao.ClinicaHospitalDAO;
import pe.edu.pucp.klam.dao.impl.ClinicaHospitalDAOImpl;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;

import java.sql.SQLException;
import java.util.List;

public class ClinicaHospitalBLImpl implements ClinicaHospitalBL {
    private final ClinicaHospitalDAO clinicaHospitalDAO = new ClinicaHospitalDAOImpl();

    @Override
    public List<ClinicaHospital> findAll() throws BLException {
        try {
            return clinicaHospitalDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las clínicas y hospitales", e);
        }
    }

    @Override
    public ClinicaHospital findById(Integer id) throws BLException {
        validarId(id);
        try {
            return clinicaHospitalDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la clínica u hospital", e);
        }
    }

    @Override
    public void insert(ClinicaHospital clinicaHospital) throws BLException {
        validarDatos(clinicaHospital);
        validarRucUnico(clinicaHospital);
        try {
            clinicaHospitalDAO.insert(clinicaHospital);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la clínica u hospital", e);
        }
    }

    @Override
    public void update(ClinicaHospital clinicaHospital) throws BLException {
        validarDatos(clinicaHospital);
        validarExiste(clinicaHospital.getId_cliente());
        validarRucUnico(clinicaHospital);
        try {
            clinicaHospitalDAO.update(clinicaHospital);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la clínica u hospital", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarId(id);
        try {
            clinicaHospitalDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar la clínica u hospital", e);
        }
    }

    private void validarDatos(ClinicaHospital clinicaHospital) throws BLException {
        if (clinicaHospital == null) {
            throw new BLException("Debe proporcionar la clínica u hospital");
        }
        if (clinicaHospital.getRuc() == null
                || !clinicaHospital.getRuc().matches("\\d{11}")) {
            throw new BLException("El RUC debe tener exactamente 11 dígitos");
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
            if (clinicaHospitalDAO.findById(id) == null) {
                throw new BLException("No existe la clínica u hospital con id " + id);
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de la clínica u hospital", e);
        }
    }

    private void validarRucUnico(ClinicaHospital clinicaHospital) throws BLException {
        try {
            ClinicaHospital existente = clinicaHospitalDAO.findByRUC(clinicaHospital.getRuc());
            if (existente != null && existente.getId_cliente() != clinicaHospital.getId_cliente()) {
                throw new BLException(
                        "Ya existe un registro con el RUC " + clinicaHospital.getRuc());
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la unicidad del RUC", e);
        }
    }
}
