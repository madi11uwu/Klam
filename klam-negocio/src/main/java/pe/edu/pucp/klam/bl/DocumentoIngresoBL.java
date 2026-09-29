package pe.edu.pucp.klam.bl;

import java.util.List;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.EstadoValidacionDocumento;

public interface DocumentoIngresoBL {
    void insertar(DocumentoIngreso doc) throws BLException;
    void actualizar(DocumentoIngreso doc) throws BLException;
    void eliminar(Integer id) throws BLException;
    DocumentoIngreso obtenerPorId(Integer id) throws BLException;
    List<DocumentoIngreso> listarTodos() throws BLException;
    void cambiarEstadoValidacion(Integer idDocumento, EstadoValidacionDocumento nuevoEstado) throws BLException;
}
