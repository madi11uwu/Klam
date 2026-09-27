package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.dao.NotaCreditoDAO;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.NotaCredito;

import java.sql.SQLException;
import java.util.List;

public class NotaCreditoDAOImpl implements NotaCreditoDAO {
    @Override
    public List<NotaCredito> findAll() throws SQLException {
        return List.of();
    }

    @Override
    public NotaCredito findById(Integer integer) throws SQLException {
        return null;
    }

    @Override
    public void insert(NotaCredito modelo) throws SQLException {

    }

    @Override
    public void update(NotaCredito modelo) throws SQLException {

    }

    @Override
    public void delete(Integer integer) throws SQLException {

    }
}
