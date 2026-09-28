package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.VendedorBL;
import pe.edu.pucp.klam.dao.VendedorDAO;
import pe.edu.pucp.klam.dao.impl.VendedorDAOImpl;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;

import java.sql.SQLException;
import java.util.List;

public class VendedorBLImpl implements VendedorBL {

    private final VendedorDAO vendedorDAO = new VendedorDAOImpl();

    @Override
    public List findAll() throws BLException {
        try {
            return vendedorDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los vendedores", e);
        }
    }

    @Override
    public Vendedor findById(Integer id) throws BLException {
        if (id == null) {
            throw new BLException("El ID no puede ser nulo");
        }
        try {
            return vendedorDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el vendedor con ID: " + id, e);
        }
    }

    @Override
    public void insert(Vendedor vendedor) throws BLException {
        validarDatos(vendedor);

        try {
            vendedorDAO.insert(vendedor);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar el vendedor", e);
        }
    }

    @Override
    public void update(Vendedor vendedor) throws BLException {
        validarDatos(vendedor);
        validarExiste(vendedor.getIdUsuario());

        try {
            vendedorDAO.update(vendedor);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar el vendedor", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarExiste(id);

        try {
            vendedorDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar el vendedor", e);
        }
    }

    private void validarDatos(Vendedor vendedor) throws BLException {
        if (vendedor == null) {
            throw new BLException("El vendedor no puede ser nulo");
        }
        if (vendedor.getUsername() == null || vendedor.getUsername().isBlank()) {
            throw new BLException("El nombre de usuario (username) es obligatorio");
        }
        if (vendedor.getEmail() == null || vendedor.getEmail().isBlank()) {
            throw new BLException("El correo electrónico es obligatorio");
        }
        if (!vendedor.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BLException("El formato del correo electrónico es inválido");
        }
        if (vendedor.getNombres() == null || vendedor.getNombres().isBlank()) {
            throw new BLException("Los nombres del vendedor son obligatorios");
        }
        if (vendedor.getApellidos() == null || vendedor.getApellidos().isBlank()) {
            throw new BLException("Los apellidos del vendedor son obligatorios");
        }
        if (vendedor.getComisionAcumulada() < 0) {
            throw new BLException("La comisión acumulada no puede ser un valor negativo");
        }
    }

    private void validarExiste(Integer id) throws BLException {
        if (id == null) {
            throw new BLException("El ID no puede ser nulo");
        }
        try {
            if (vendedorDAO.findById(id) == null) {
                throw new BLException("No existe un vendedor con el ID " + id);
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia del vendedor", e);
        }
    }
}