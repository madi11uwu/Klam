package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.documentacionfinanzas.NotaCredito;

import java.util.List;

public interface NotaCreditoBL extends BaseBL<NotaCredito, Integer>{
    List<NotaCredito> findByFacturaId(int idFactura) throws BLException;
    List<NotaCredito> findByBoletaId(int idBoleta) throws BLException;
}
