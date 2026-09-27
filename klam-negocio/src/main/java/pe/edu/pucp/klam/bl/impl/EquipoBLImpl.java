package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.EquipoBL;
import pe.edu.pucp.klam.dao.EquipoDAO;
import pe.edu.pucp.klam.dao.impl.inventario.EquipoDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;

import java.sql.SQLException;
import java.util.List;

public class EquipoBLImpl implements EquipoBL {
    private final EquipoDAO equipoDAO = new EquipoDAOImpl();

    @Override
    public List<Equipo> findAll() throws BLException {
        try {
            return equipoDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los equipos", e);
        }
    }

    @Override
    public Equipo findById(Integer id) throws BLException {
        try {
            return equipoDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el equipo", e);
        }
    }

    @Override
    public void insert(Equipo equipo) throws BLException {
        validar(equipo);

        TransactionsManager.iniciar();
        try {
            equipoDAO.insert(equipo);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollback();
            throw new BLException("No se pudo registrar el equipo", e);
        }
    }

    @Override
    public void update(Equipo equipo) throws BLException {
        validar(equipo);
        validarExiste(equipo.getId_equipo());

        TransactionsManager.iniciar();
        try {
            equipoDAO.update(equipo);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollback();
            throw new BLException("No se pudo actualizar el equipo", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarExiste(id);

        TransactionsManager.iniciar();
        try {
            equipoDAO.delete(id);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollback();
            throw new BLException("No se pudo eliminar el equipo", e);
        }
    }

    private void validar(Equipo equipo) throws BLException {
        if (equipo == null) {
            throw new BLException("El equipo no puede ser nulo");
        }
        if (equipo.getNombre() == null || equipo.getNombre().isBlank()) {
            throw new BLException("El nombre del equipo es obligatorio");
        }
        if (equipo.getCategoria() == null) {
            throw new BLException("La categoria del equipo es obligatoria");
        }
    }

    private void validarExiste(Integer id) throws BLException {
        try{
            if(equipoDAO.findById(id)==null){
                throw new BLException("No existe un equipo con id " + id);
            }
        }catch (SQLException e){
            throw new BLException("No se pudo verificar la existencia del equipo",e);
        }
    }
}