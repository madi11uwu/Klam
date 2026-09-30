package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CotizacionBL;
import pe.edu.pucp.klam.dao.CirugiaDAO;
import pe.edu.pucp.klam.dao.CotizacionDAO;
import pe.edu.pucp.klam.dao.impl.CirugiaDAOImpl;
import pe.edu.pucp.klam.dao.impl.CotizacionDAOImpl;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Cotizacion;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.EstadoCotizacion;

import java.sql.SQLException;
import java.util.List;

public class CotizacionBLImpl implements CotizacionBL {
    private static final double PRECIO_MINIMO = 0.0;

    private final CotizacionDAO cotizacionDAO = new CotizacionDAOImpl();
    private final CirugiaDAO cirugiaDAO = new CirugiaDAOImpl();

    @Override
    public List<Cotizacion> findAll() throws BLException {
        try {
            return cotizacionDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las cotizaciones", e);
        }
    }

    @Override
    public Cotizacion findById(Integer id) throws BLException {
        try {
            return cotizacionDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la cotización", e);
        }
    }

    @Override
    public List<Cotizacion> findByCirugia(Integer idCirugia) throws BLException {
        if (idCirugia == null) {
            throw new BLException("Debe indicar la cirugía de las cotizaciones a buscar");
        }

        try {
            return cotizacionDAO.findByCirugia(idCirugia);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las cotizaciones de la cirugía", e);
        }
    }

    @Override
    public void insert(Cotizacion cotizacion) throws BLException {
        validarDatos(cotizacion);
        try {
            cotizacionDAO.insert(cotizacion);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la cotización", e);
        }
    }

    @Override
    public void update(Cotizacion cotizacion) throws BLException {
        validarDatos(cotizacion);

        Cotizacion existente = buscarCotizacion(cotizacion.getIdCotizacion());
        if (existente.estaResuelta() && existente.getEstado() != cotizacion.getEstado()) {
            throw new BLException("La cotización ya está " + existente.getEstado()
                    + " y no puede pasar a " + cotizacion.getEstado());
        }

        try {
            cotizacionDAO.update(cotizacion);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la cotización", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        buscarCotizacion(id);
        try {
            cotizacionDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar la cotización", e);
        }
    }

    @Override
    public void aceptar(Integer id) throws BLException {
        resolver(id, EstadoCotizacion.ACEPTADA);
    }

    @Override
    public void rechazar(Integer id) throws BLException {
        resolver(id, EstadoCotizacion.RECHAZADA);
    }

    @Override
    public void vencer(Integer id) throws BLException {
        resolver(id, EstadoCotizacion.VENCIDA);
    }

    // La transicion EMITIDA -> ACEPTADA/RECHAZADA/VENCIDA la controla el modelo
    private void resolver(Integer id, EstadoCotizacion nuevo) throws BLException {
        Cotizacion cotizacion = buscarCotizacion(id);
        try {
            switch (nuevo) {
                case ACEPTADA -> cotizacion.aceptar();
                case RECHAZADA -> cotizacion.rechazar();
                case VENCIDA -> cotizacion.vencer();
                default -> throw new BLException("Estado de cotización no válido: " + nuevo);
            }
        } catch (IllegalStateException e) {
            throw new BLException(e.getMessage(), e);
        }

        try {
            cotizacionDAO.actualizarEstado(id, cotizacion.getEstado());
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar el estado de la cotización", e);
        }
    }

    private void validarDatos(Cotizacion cotizacion) throws BLException {
        if (cotizacion == null) {
            throw new BLException("La cotización no puede ser nula");
        }

        if (cotizacion.getPrecioPactado() < PRECIO_MINIMO) {
            throw new BLException(String.format(
                    "El precio pactado no puede ser menor a S/ %.2f", PRECIO_MINIMO));
        }

        if (cotizacion.getFechaEmision() == null) {
            throw new BLException("La fecha de emisión es obligatoria");
        }

        if (cotizacion.getCirugia() == null) {
            throw new BLException("La cotización debe estar asociada a una cirugía");
        }
        validarCirugiaExiste(cotizacion.getCirugia().getIdCirugia());
    }

    private void validarCirugiaExiste(int idCirugia) throws BLException {
        try {
            if (cirugiaDAO.findById(idCirugia) == null) {
                throw new BLException("No existe una cirugía con id " + idCirugia);
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de la cirugía", e);
        }
    }

    private Cotizacion buscarCotizacion(int id) throws BLException {
        try {
            Cotizacion cotizacion = cotizacionDAO.findById(id);
            if (cotizacion == null) {
                throw new BLException("No existe una cotización con id " + id);
            }
            return cotizacion;
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de la cotización", e);
        }
    }
}
