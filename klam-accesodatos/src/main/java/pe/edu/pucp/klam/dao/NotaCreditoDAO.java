package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.documentacionfinanzas.NotaCredito;

import java.sql.SQLException;
import java.util.List;

public interface NotaCreditoDAO extends DAO<NotaCredito, Integer>{
    List<NotaCredito> findByFacturaId(int idFactura) throws SQLException;
    List<NotaCredito> findByBoletaId(int idBoleta) throws SQLException;

}
