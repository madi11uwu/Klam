package pe.edu.pucp.klam.bl;

import java.util.List;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.OrdenCompra;

public interface OrdenCompraBL {
    void insertar(OrdenCompra orden) throws BLException;
    void actualizar(OrdenCompra orden) throws BLException;
    void eliminar(Integer id) throws BLException;
    OrdenCompra obtenerPorId(Integer id) throws BLException;
    List<OrdenCompra> listarTodas() throws BLException;
}