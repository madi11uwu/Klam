package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.documentacionfinanzas.Cotizacion;

import java.util.List;

public interface CotizacionBL extends RegistroBL<Cotizacion, Integer> {
    List<Cotizacion> findByCirugia(Integer idCirugia) throws BLException;
    void aceptar(Integer id) throws BLException;
    void rechazar(Integer id) throws BLException;
    void vencer(Integer id) throws BLException;
}
