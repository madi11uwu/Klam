package pe.edu.pucp.klam.bl;

import java.util.List;
import java.util.Map;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.EstadoValidacionDocumento;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.TipoDocumentoIngreso;

/**
 * Documentos de ingreso con versionado: la resubida de un documento rechazado
 * NO lo borra; guarda una version nueva y conserva el historial completo.
 */
public interface DocumentoIngresoBL extends BaseBL<DocumentoIngreso,Integer> {
    void cambiarEstadoValidacion(Integer idDocumento, EstadoValidacionDocumento nuevoEstado) throws BLException;

    // ---- Consulta ----
    List<DocumentoIngreso> listarPorSolicitud(int idSolicitud) throws BLException;
    List<DocumentoIngreso> listarPorCirugia(int idCirugia) throws BLException;

    /** Historial documental: fechaHora, documento, version, responsable, estado, motivoRechazo, referencia. */
    List<Map<String, Object>> consultarHistorialPorSolicitud(int idSolicitud) throws BLException;
    List<Map<String, Object>> consultarHistorialPorCirugia(int idCirugia) throws BLException;

    /** Registro inicial por un administrador autenticado, sin atribuir la carga al titular. */
    DocumentoIngreso registrarDocumentoSolicitud(int idSolicitud, TipoDocumentoIngreso tipo,
            String archivoPath, int idUsuarioAutenticado, String rolAutenticado,
            String responsable) throws BLException;

    /** Consulta solo documentos cuya primera versión pertenece al usuario autenticado. */
    List<DocumentoIngreso> listarPresentadosPorUsuario(Integer idSolicitud, int idUsuarioAutenticado,
            String rolAutenticado) throws BLException;

    /** Identidad de quien presentó la primera versión, comprobada por ID y rol. */
    boolean esPresentadorOriginal(int idDocumento, int idUsuarioAutenticado,
            String rolAutenticado) throws BLException;

    /** Verifica estado, versión, autorización e identidad antes de recibir una nueva versión. */
    DocumentoIngreso verificarResubida(int idDocumento, int idUsuarioAutenticado,
            String rolAutenticado) throws BLException;

    // ---- Validacion y subsanacion ----
    void validarDocumento(int idDocumento, String responsable) throws BLException;

    /** El motivo es obligatorio. Si el documento es de una solicitud, esta pasa a DOCUMENTACION_INCOMPLETA. */
    void rechazarDocumento(int idDocumento, String motivo, String responsable) throws BLException;
    void rechazarDocumento(int idDocumento, String motivo, boolean habilitarResubida,
                           String responsable) throws BLException;

    /** Sube una nueva version de un documento RECHAZADO; la anterior se conserva. */
    DocumentoIngreso resubirDocumento(int idDocumentoRechazado, String nuevoArchivoPath,
                                      int idUsuarioAutenticado, String rolAutenticado,
                                      String responsable) throws BLException;

    // ---- Documentos obligatorios de una solicitud ----
    /** Tipos obligatorios segun el cliente (particular: DNI, receta y comprobante; clinica: receta). */
    List<TipoDocumentoIngreso> listarDocumentosObligatorios(String tipoCliente) throws BLException;

    /** true si cada documento obligatorio de la solicitud tiene su version vigente VALIDADA. */
    boolean verificarDocumentosObligatoriosValidados(int idSolicitud) throws BLException;

    // ---- Orden de compra (clinicas y hospitales) y cierre de cirugia ----
    /** El cliente adjunta la orden de compra de una cirugia FINALIZADA; queda PENDIENTE. */
    DocumentoIngreso adjuntarOrdenCompra(int idCirugia, String archivoPath, String responsable) throws BLException;
    void validarOrdenCompra(int idDocumento, String responsable) throws BLException;
    void rechazarOrdenCompra(int idDocumento, String motivo, String responsable) throws BLException;

    /**
     * Clinica/hospital: orden de compra vigente VALIDADA. Paciente particular: comprobante de
     * pago vigente VALIDADO (de su solicitud o, sin solicitud, de la cirugia).
     */
    boolean verificarDocumentacionCierre(int idCirugia) throws BLException;

    /** Igual que verificarDocumentacionCierre, pero lanza BLException indicando que falta. */
    void validarDocumentacionCierre(int idCirugia) throws BLException;
}
