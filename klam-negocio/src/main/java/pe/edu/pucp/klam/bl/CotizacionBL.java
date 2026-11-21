package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.documentacionfinanzas.Cotizacion;

import java.util.List;

public interface CotizacionBL extends BaseBL<Cotizacion, Integer> {
    List<Cotizacion> findByCirugia(Integer idCirugia) throws BLException;
    List<Cotizacion> listarPorSolicitud(Integer idSolicitud) throws BLException;
    Cotizacion buscarVigentePorSolicitud(Integer idSolicitud) throws BLException;
    void aceptar(Integer id) throws BLException;
    void rechazar(Integer id) throws BLException;
    void vencer(Integer id) throws BLException;
    void registrarVersion(Cotizacion cotizacion, int idAdministrador, String responsable) throws BLException;
    void decidirVersion(int idCotizacion, int idCuentaUsuario, boolean aceptar, String motivo, String responsable) throws BLException;
}
