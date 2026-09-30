package pe.edu.pucp.klam.bl;

import java.util.List;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.EstadoValidacionDocumento;

public interface DocumentoIngresoBL extends BaseBL<DocumentoIngreso,Integer> {
    void cambiarEstadoValidacion(Integer idDocumento, EstadoValidacionDocumento nuevoEstado) throws BLException;
}
