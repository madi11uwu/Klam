package pe.edu.pucp.klam.bl.impl;

import java.sql.SQLException;
import java.util.List;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.OrdenCompraBL;
import pe.edu.pucp.klam.dao.OrdenCompraDAO;
import pe.edu.pucp.klam.dao.impl.compras.OrdenCompraDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.LineaOrdenCompra;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.OrdenCompra;

public class OrdenCompraBLImpl implements OrdenCompraBL {

    private final OrdenCompraDAO ordenDAO = new OrdenCompraDAOImpl();

    @Override
    public void insertar(OrdenCompra orden) throws BLException {
        validarOrdenCompra(orden);
        
        // Manejo de Transaccion en Capa Negocio (BL) para Cabecera + Lineas
        TransactionsManager.iniciar();
        try {
            ordenDAO.insert(orden);
            TransactionsManager.commit();
        } catch (SQLException | RuntimeException e) {
            TransactionsManager.rollback();
            throw new BLException("Error al registrar la orden de compra y sus lineas de detalle", e);
        }
    }

    @Override
    public void actualizar(OrdenCompra orden) throws BLException {
        if (orden == null || orden.getIdOrdenCompra() <= 0) {
            throw new BLException("El ID de la orden de compra debe ser valido para actualizar");
        }
        validarOrdenCompra(orden);

        TransactionsManager.iniciar();
        try {
            ordenDAO.update(orden);
            TransactionsManager.commit();
        } catch (SQLException | RuntimeException e) {
            TransactionsManager.rollback();
            throw new BLException("Error al actualizar la orden de compra y sus detalles", e);
        }
    }

    @Override
    public void eliminar(Integer id) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("Debe proporcionar un ID valido de orden de compra para eliminar");
        }

        TransactionsManager.iniciar();
        try {
            ordenDAO.delete(id);
            TransactionsManager.commit();
        } catch (SQLException | RuntimeException e) {
            TransactionsManager.rollback();
            throw new BLException("Error al eliminar la orden de compra", e);
        }
    }

    @Override
    public OrdenCompra obtenerPorId(Integer id) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("Debe proporcionar un ID valido de orden de compra");
        }
        try {
            return ordenDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("Error al consultar la orden de compra por ID", e);
        }
    }

    @Override
    public List<OrdenCompra> listarTodas() throws BLException {
        try {
            return ordenDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("Error al consultar el listado de ordenes de compra", e);
        }
    }

    private void validarOrdenCompra(OrdenCompra orden) throws BLException {
        if (orden == null) {
            throw new BLException("La orden de compra no puede ser nula");
        }
        if (orden.getArchivoRespaldoPath() == null || orden.getArchivoRespaldoPath().isBlank()) {
            throw new BLException("El archivo de respaldo de la orden de compra es obligatorio");
        }
        if (orden.getLineasOrdenCompra() == null || orden.getLineasOrdenCompra().isEmpty()) {
            throw new BLException("La orden de compra debe contener al menos una linea de detalle");
        }
        for (LineaOrdenCompra linea : orden.getLineasOrdenCompra()) {
            if (linea.getCantidad() <= 0) {
                throw new BLException("Cada linea de la orden de compra debe tener una cantidad mayor a 0");
            }
            if (linea.getPrecioUnitario() < 0) {
                throw new BLException("El precio unitario de las lineas no puede ser negativo");
            }
        }
    }
}