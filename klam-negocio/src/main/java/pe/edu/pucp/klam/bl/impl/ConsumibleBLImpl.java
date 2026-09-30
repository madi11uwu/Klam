package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.ConsumibleBL;
import pe.edu.pucp.klam.dao.ConsumibleDAO;
import pe.edu.pucp.klam.dao.impl.inventario.ConsumibleDAOImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;

import java.sql.SQLException;
import java.util.List;

public class ConsumibleBLImpl implements ConsumibleBL {
    private final ConsumibleDAO consumibleDAO = new ConsumibleDAOImpl();

    @Override
    public List<Consumible> findByNombre(String nombre) throws BLException {
        try {
            return consumibleDAO.findByNombre(nombre);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los consumibles", e);
        }
    }

    @Override
    public List<Consumible> findAll() throws BLException {
        try {
            return consumibleDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los consumibles", e);
        }
    }

    @Override
    public Consumible findById(Integer id) throws BLException {
        try {
            return consumibleDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el consumible", e);
        }
    }

    @Override
    public void insert(Consumible consumible) throws BLException {
        validar(consumible);

        try {
            consumibleDAO.insert(consumible);

        } catch (SQLException e) {
            throw new BLException("No se pudo registrar el consumible", e);
        }
    }

    @Override
    public void update(Consumible consumible) throws BLException {
        validar(consumible);
        validarExiste(consumible.getIdConsumible());

        try {
            consumibleDAO.update(consumible);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar el consumible", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarExiste(id);

        try {
            consumibleDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar el consumible", e);
        }
    }

    private void validar(Consumible consumible) throws BLException {
        if (consumible == null) {
            throw new BLException("El consumible no puede ser nulo");
        }
        if (consumible.getNombreComercial() == null || consumible.getNombreComercial().isBlank()) {
            throw new BLException("El nombre comercial del consumible es obligatorio");
        }
    }

    private void validarExiste(Integer id) throws BLException {
        try{
            if(consumibleDAO.findById(id)==null){
                throw new BLException("No existe un consumible con id " + id);
            }
        }catch (SQLException e){
            throw new BLException("No se pudo verificar la existencia del consumible",e);
        }
    }
}
