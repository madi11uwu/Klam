package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.dao.BoletaDAO;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Boleta;

import java.sql.SQLException;
import java.util.List;

public class BoletaDAOImpl implements BoletaDAO {

    @Override
    public List<Boleta> findAll() throws SQLException {
        return List.of();
    }

    @Override
    public Boleta findById(Integer integer) throws SQLException {
        return null;
    }

    @Override
    public void insert(Boleta modelo) throws SQLException {

    }

    @Override
    public void update(Boleta modelo) throws SQLException {

    }

    @Override
    public void delete(Integer integer) throws SQLException {

    }
}
