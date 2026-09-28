package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.dao.BandejaInstrumentalDAO;
import pe.edu.pucp.klam.dao.CirugiaDAO;
import pe.edu.pucp.klam.dao.ClinicaHospitalDAO;
import pe.edu.pucp.klam.dao.EquipoDAO;
import pe.edu.pucp.klam.dao.PacienteParticularDAO;
import pe.edu.pucp.klam.dao.impl.CirugiaDAOImpl;
import pe.edu.pucp.klam.dao.impl.ClinicaHospitalDAOImpl;
import pe.edu.pucp.klam.dao.impl.PacienteParticularDAOImpl;
import pe.edu.pucp.klam.dao.impl.inventario.BandejaInstrumentalDAOImpl;
import pe.edu.pucp.klam.dao.impl.inventario.EquipoDAOImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class CirugiaBLImpl implements CirugiaBL {
    private final CirugiaDAO cirugiaDAO = new CirugiaDAOImpl();
    private final EquipoDAO equipoDAO = new EquipoDAOImpl();
    private final BandejaInstrumentalDAO bandejaDAO = new BandejaInstrumentalDAOImpl();
    private final ClinicaHospitalDAO clinicaHospitalDAO = new ClinicaHospitalDAOImpl();
    private final PacienteParticularDAO pacienteParticularDAO = new PacienteParticularDAOImpl();

    @Override
    public List<Cirugia> findAll() throws BLException {
        try {
            return cirugiaDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las cirugías", e);
        }
    }

    @Override
    public Cirugia findById(Integer id) throws BLException {
        try {
            return cirugiaDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la cirugía", e);
        }
    }

    @Override
    public List<Cirugia> findByEstado(EstadoCirugia estado) throws BLException {
        if (estado == null) {
            throw new BLException("Debe indicar el estado de las cirugías a buscar");
        }

        try {
            return cirugiaDAO.findByEstado(estado);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las cirugías por estado", e);
        }
    }

    @Override
    public List<Cirugia> findByRangoFechas(LocalDateTime desde, LocalDateTime hasta) throws BLException {
        if (desde == null || hasta == null) {
            throw new BLException("Debe indicar el inicio y el fin del rango de fechas");
        }
        if (desde.isAfter(hasta)) {
            throw new BLException("El inicio del rango no puede ser posterior al fin");
        }

        try {
            return cirugiaDAO.findByRangoFechas(desde, hasta);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las cirugías por rango de fechas", e);
        }
    }

    @Override
    public void insert(Cirugia cirugia) throws BLException {
        validarDatos(cirugia);
        try {
            cirugiaDAO.insert(cirugia);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la cirugía", e);
        }
    }

    @Override
    public void update(Cirugia cirugia) throws BLException {
        validarDatos(cirugia);
        buscarCirugia(cirugia.getId_cirugia());
        try {
            cirugiaDAO.update(cirugia);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la cirugía", e);
        }
    }

    @Override
    public void cancelar(Integer id, String motivo) throws BLException {
        if (motivo == null || motivo.isBlank()) {
            throw new BLException("Debe indicar el motivo de la cancelación");
        }

        Cirugia cirugia = buscarCirugia(id);
        if (cirugia.getEstado() == EstadoCirugia.CANCELADA
                || cirugia.getEstado() == EstadoCirugia.FINALIZADA) {
            throw new BLException(
                    "No se puede cancelar una cirugía en estado " + cirugia.getEstado());
        }

        try {
            cirugiaDAO.cancelar(id, motivo);
        } catch (SQLException e) {
            throw new BLException("No se pudo cancelar la cirugía", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        buscarCirugia(id);
        try {
            cirugiaDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar la cirugía", e);
        }
    }

    private void validarDatos(Cirugia cirugia) throws BLException {
        if (cirugia == null) {
            throw new BLException("La cirugía no puede ser nula");
        }

        if (cirugia.getTipoProcedimiento() == null || cirugia.getTipoProcedimiento().isBlank()) {
            throw new BLException("El tipo de procedimiento es obligatorio");
        }

        LocalDateTime inicio = cirugia.getFechaHoraInicio();
        LocalDateTime fin = cirugia.getFechaHoraFin();
        if (inicio == null) {
            throw new BLException("La fecha y hora de inicio es obligatoria");
        }
        if (fin != null && !inicio.isBefore(fin)) {
            throw new BLException("La fecha y hora de inicio debe ser anterior a la de fin");
        }

        if (cirugia.getEstado() == EstadoCirugia.FINALIZADA && fin == null) {
            throw new BLException("Una cirugía finalizada debe tener fecha y hora de fin");
        }
        if (cirugia.getEstado() == EstadoCirugia.CANCELADA
                && (cirugia.getMotivoCancelacion() == null || cirugia.getMotivoCancelacion().isBlank())) {
            throw new BLException("Una cirugía cancelada debe tener un motivo de cancelación");
        }

        if (cirugia.getCliente() == null) {
            throw new BLException("La cirugía debe tener un cliente");
        }
        validarClienteExiste(cirugia.getCliente());

        // Los recursos solo deben estar operativos mientras la cirugia este vigente
        boolean vigente = cirugia.getEstado() == EstadoCirugia.PROGRAMADA
                || cirugia.getEstado() == EstadoCirugia.EN_PROCESO;

        if (cirugia.getEquipo() != null) {
            Equipo equipo = buscarEquipo(cirugia.getEquipo().getId_equipo());
            if (vigente && !equipo.verificar()) {
                throw new BLException("El equipo " + equipo.getNombre() + " no está disponible");
            }
        }

        if (cirugia.getBandejaInstrumental() != null) {
            BandejaInstrumental bandeja = buscarBandeja(cirugia.getBandejaInstrumental().getId_bandeja());
            if (vigente && !bandeja.verificar()) {
                throw new BLException("La bandeja " + bandeja.getTipo() + " no está esterilizada");
            }
        }
    }

    private Cirugia buscarCirugia(int id) throws BLException {
        try {
            Cirugia cirugia = cirugiaDAO.findById(id);
            if (cirugia == null) {
                throw new BLException("No existe una cirugía con id " + id);
            }
            return cirugia;
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de la cirugía", e);
        }
    }

    private void validarClienteExiste(Cliente cliente) throws BLException {
        try {
            boolean existe;
            if (cliente instanceof ClinicaHospital) {
                existe = clinicaHospitalDAO.findById(cliente.getId_cliente()) != null;
            } else if (cliente instanceof PacienteParticular) {
                existe = pacienteParticularDAO.findById(cliente.getId_cliente()) != null;
            } else {
                throw new BLException("Tipo de cliente no soportado");
            }

            if (!existe) {
                throw new BLException("No existe un cliente con id " + cliente.getId_cliente());
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia del cliente", e);
        }
    }

    private Equipo buscarEquipo(int idEquipo) throws BLException {
        try {
            Equipo equipo = equipoDAO.findById(idEquipo);
            if (equipo == null) {
                throw new BLException("No existe un equipo con id " + idEquipo);
            }
            return equipo;
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el equipo de la cirugía", e);
        }
    }

    private BandejaInstrumental buscarBandeja(int idBandeja) throws BLException {
        try {
            BandejaInstrumental bandeja = bandejaDAO.findById(idBandeja);
            if (bandeja == null) {
                throw new BLException("No existe una bandeja instrumental con id " + idBandeja);
            }
            return bandeja;
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la bandeja de la cirugía", e);
        }
    }
}
