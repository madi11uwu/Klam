package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.BaseBL;
import pe.edu.pucp.klam.dao.DAO;
import pe.edu.pucp.klam.modelo.clientes.Cliente;

import java.sql.SQLException;
import java.util.List;

public abstract class ClienteBLImpl<T extends Cliente> implements BaseBL<T, Integer> {
    private final DAO<T, Integer> clienteDAO;
    private final String descripcion;
    private final String descripcionPlural;

    protected ClienteBLImpl(DAO<T, Integer> clienteDAO, String descripcion, String descripcionPlural) {
        this.clienteDAO = clienteDAO;
        this.descripcion = descripcion;
        this.descripcionPlural = descripcionPlural;
    }

    protected abstract void validarDatos(T cliente) throws BLException;
    protected abstract void validarDocumentoUnico(T cliente, boolean modificacion) throws BLException;

    @Override
    public List<T> findAll() throws BLException {
        try {
            return clienteDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar " + descripcionPlural, e);
        }
    }

    @Override
    public T findById(Integer id) throws BLException {
        validarId(id);
        try {
            return clienteDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar " + descripcion, e);
        }
    }

    @Override
    public void insert(T cliente) throws BLException {
        validarDatos(cliente);
        validarDocumentoUnico(cliente, false);
        try {
            clienteDAO.insert(cliente);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar " + descripcion, e);
        }
    }

    @Override
    public void update(T cliente) throws BLException {
        validarDatos(cliente);
        validarExiste(cliente.getId_cliente());
        validarDocumentoUnico(cliente, true);
        try {
            clienteDAO.update(cliente);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar " + descripcion, e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarId(id);
        try {
            clienteDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar " + descripcion, e);
        }
    }

    protected void validarExiste(Integer id) throws BLException {
        validarId(id);
        try {
            if (clienteDAO.findById(id) == null) {
                throw new BLException("No existe " + descripcion + " con id " + id);
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de " + descripcion, e);
        }
    }

    protected void validarDatosCliente(String nombre, String direccion, String correo, String telefono)
            throws BLException {
        if (nombre == null || nombre.isBlank()) {
            throw new BLException("El nombre es obligatorio");
        }
        validarLongitud(nombre, 150, "El nombre");
        validarLongitud(direccion, 200, "La dirección");
        validarLongitud(correo, 120, "El correo de contacto");
        validarLongitud(telefono, 20, "El teléfono");
        if (correo != null && !correo.isBlank()
                && (!correo.contains("@") || !correo.contains("."))) {
            throw new BLException("El correo de contacto tiene un formato inválido");
        }
    }

    public void validarLongitud(String valor, int maximo, String campo) {
        if (valor != null && valor.length() > maximo) {
            throw new BLException(campo + " no puede exceder " + maximo + " caracteres");
        }
    }

    public void validarId(Integer id) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("El id debe ser un número positivo");
        }
    }
}
