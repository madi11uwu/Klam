package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.ClinicaHospitalBL;
import pe.edu.pucp.klam.dao.ClinicaHospitalDAO;
import pe.edu.pucp.klam.dao.impl.ClinicaHospitalDAOImpl;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;

import java.sql.SQLException;

public class ClinicaHospitalBLImpl extends ClienteBLImpl<ClinicaHospital> implements ClinicaHospitalBL {
    private final ClinicaHospitalDAO clinicaHospitalDAO;

    public ClinicaHospitalBLImpl() {
        this(new ClinicaHospitalDAOImpl());
    }

    private ClinicaHospitalBLImpl(ClinicaHospitalDAO clinicaHospitalDAO) {
        super(clinicaHospitalDAO, "la clínica u hospital", "las clínicas y hospitales");
        this.clinicaHospitalDAO = clinicaHospitalDAO;
    }

    @Override
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

    @Override
    protected void validarDocumentoUnico(ClinicaHospital clinicaHospital, boolean modificacion) throws BLException {
        try {
            ClinicaHospital existente = clinicaHospitalDAO.findByRUC(clinicaHospital.getRuc());
            if (existente != null && (!modificacion
                    || existente.getId_cliente() != clinicaHospital.getId_cliente())) {
                throw new BLException(
                        "Ya existe un registro con el RUC " + clinicaHospital.getRuc());
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la unicidad del RUC", e);
        }
    }
}
