package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.BandejaInstrumentalBL;
import pe.edu.pucp.klam.dao.BandejaInstrumentalDAO;
import pe.edu.pucp.klam.dao.ConsumibleDAO;
import pe.edu.pucp.klam.dao.impl.BandejaInstrumentalDAOImpl;
import pe.edu.pucp.klam.dao.impl.ConsumibleDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class BandejaInstrumentalBLImpl implements BandejaInstrumentalBL {
    private final BandejaInstrumentalDAO bandejaInstrumentalDAO = new BandejaInstrumentalDAOImpl();
    private final ConsumibleDAO consumibleDAO = new ConsumibleDAOImpl();

    @Override
    public List<BandejaInstrumental> findAll() throws BLException {
        try {
            return bandejaInstrumentalDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las bandejas instrumentales", e);
        }
    }

    @Override
    public BandejaInstrumental findById(Integer id) throws BLException {
        try {
            return bandejaInstrumentalDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la bandeja instrumental", e);
        }
    }

    @Override
    public void insert(BandejaInstrumental bandejaInstrumental) throws BLException {
        validar(bandejaInstrumental);

        TransactionsManager.iniciar();
        try {
            bandejaInstrumentalDAO.insert(bandejaInstrumental);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollback();
            throw new BLException("No se pudo registrar la bandeja instrumental", e);
        }
    }

    @Override
    public void update(BandejaInstrumental bandejaInstrumental) throws BLException {
        validar(bandejaInstrumental);
        validarExiste(bandejaInstrumental.getId_bandeja());

        TransactionsManager.iniciar();
        try {
            bandejaInstrumentalDAO.update(bandejaInstrumental);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollback();
            throw new BLException("No se pudo actualizar la bandeja instrumental", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarExiste(id);

        TransactionsManager.iniciar();
        try {
            bandejaInstrumentalDAO.delete(id);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollback();
            throw new BLException("No se pudo eliminar la bandeja instrumental", e);
        }
    }

    private void validarExiste(Integer id) throws BLException {
        try {
            if (bandejaInstrumentalDAO.findById(id) == null) {
                throw new BLException("No existe una bandeja instrumental con id " + id);
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de la bandeja instrumental", e);
        }
    }

    private void validar(BandejaInstrumental bandeja) throws BLException {
        if (bandeja == null) {
            throw new BLException("La bandeja no puede ser nula");
        }
        if (bandeja.getTipo() == null || bandeja.getTipo().isBlank()) {
            throw new BLException("El tipo de bandeja es obligatorio");
        }

        Map<Consumible, Integer> despachados = bandeja.getConsumibles();
        Map<Consumible, Integer> consumidos = bandeja.getConsumiblesConsumidos();

        for (Map.Entry<Consumible, Integer> e : despachados.entrySet()) {
            if (e.getValue() < 0) {
                throw new BLException("La cantidad despachada no puede ser negativa");
            }
            validarConsumibleExiste(e.getKey().getId_consumible());
        }

        for (Map.Entry<Consumible, Integer> e : consumidos.entrySet()) {
            int consumida = e.getValue();
            int despachada = despachados.getOrDefault(e.getKey(), 0);
            if (consumida < 0) {
                throw new BLException("La cantidad consumida no puede ser negativa");
            }
            if (consumida > despachada) {
                throw new BLException("No se puede consumir mas de lo despachado (consumible "
                        + e.getKey().getId_consumible() + ")");
            }
        }
    }

    private void validarConsumibleExiste(int idConsumible) throws BLException {
        try {
            if (consumibleDAO.findById(idConsumible) == null) {
                throw new BLException("No existe un consumible con id " + idConsumible);
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar el consumible", e);
        }
    }

}
