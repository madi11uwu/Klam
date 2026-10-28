package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.TipoDocumentoIngreso;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DocumentoIngresoDAO extends DAO<DocumentoIngreso, Integer> {
    /** Todas las versiones de los documentos de una solicitud. */
    List<DocumentoIngreso> listarPorSolicitud(int idSolicitud) throws SQLException;

    /** Todas las versiones de los documentos de una cirugia. */
    List<DocumentoIngreso> listarPorCirugia(int idCirugia) throws SQLException;

    /** Solo la ultima version de cada tipo de documento de la solicitud. */
    List<DocumentoIngreso> listarVigentesPorSolicitud(int idSolicitud) throws SQLException;

    /** Solo la ultima version de cada tipo de documento de la cirugia. */
    List<DocumentoIngreso> listarVigentesPorCirugia(int idCirugia) throws SQLException;

    /** Numero de la siguiente version de un documento del mismo origen y tipo. */
    int siguienteVersion(Integer idSolicitud, Integer idCirugia, TipoDocumentoIngreso tipo) throws SQLException;

    /** Marca el documento como VALIDADO o RECHAZADO (el rechazo exige motivo). */
    void actualizarEstadoValidacion(int idDocumento, String estado, LocalDateTime fechaValidacion,
                                    String motivoRechazo, String responsable) throws SQLException;

    /**
     * Historial documental (pantalla "Historial documental"): una fila por version con
     * fechaHora, documento, version, responsable, estado, motivoRechazo y referencia.
     */
    List<Map<String, Object>> historialDocumental(Integer idSolicitud, Integer idCirugia) throws SQLException;
}
