package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.PacienteParticularBL;
import pe.edu.pucp.klam.dao.PacienteParticularDAO;
import pe.edu.pucp.klam.dao.impl.PacienteParticularDAOImpl;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;

import java.sql.SQLException;

public class PacienteParticularBLImpl extends ClienteBLImpl<PacienteParticular> implements PacienteParticularBL {
    private final PacienteParticularDAO pacienteParticularDAO;

    public PacienteParticularBLImpl() {
        this(new PacienteParticularDAOImpl());
    }

    private PacienteParticularBLImpl(PacienteParticularDAO pacienteParticularDAO) {
        super(pacienteParticularDAO, "el paciente particular", "los pacientes particulares");
        this.pacienteParticularDAO = pacienteParticularDAO;
    }

    @Override
    protected void validarDatos(PacienteParticular pacienteParticular) throws BLException {
        if (pacienteParticular == null) {
            throw new BLException("Debe proporcionar el paciente particular");
        }
        validarDatosCliente(pacienteParticular.getNombre(), pacienteParticular.getDireccion(),
                pacienteParticular.getEmailContacto(), pacienteParticular.getTelefono());
        if (pacienteParticular.getDni() == null
                || pacienteParticular.getDni().length()!=8) {
            throw new BLException("El DNI debe tener exactamente 8 dígitos");
        }
    }

    @Override
    protected void validarDocumentoUnico(PacienteParticular pacienteParticular, boolean modificacion) throws BLException {
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

}
