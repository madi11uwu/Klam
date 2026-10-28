package pe.edu.pucp.klam.bl.impl;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.DocumentoIngresoBL;
import pe.edu.pucp.klam.dao.CirugiaDAO;
import pe.edu.pucp.klam.dao.DocumentoIngresoDAO;
import pe.edu.pucp.klam.dao.OrdenCompraDAO;
import pe.edu.pucp.klam.dao.impl.CirugiaDAOImpl;
import pe.edu.pucp.klam.dao.impl.DocumentoIngresoDAOImpl;
import pe.edu.pucp.klam.dao.impl.compras.OrdenCompraDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.EstadoValidacionDocumento;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.OrdenCompra;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.TipoDocumentoIngreso;

public class DocumentoIngresoBLImpl implements DocumentoIngresoBL {

    private final DocumentoIngresoDAO documentoDAO = new DocumentoIngresoDAOImpl();
    private final OrdenCompraDAO ordenCompraDAO = new OrdenCompraDAOImpl();
    private final CirugiaDAO cirugiaDAO = new CirugiaDAOImpl();

    // ------------------------------------------------------------------
    // CRUD existente
    // ------------------------------------------------------------------
    @Override
    public void insert(DocumentoIngreso doc) throws BLException {
        validarDocumento(doc);
        if (doc.getEstadoValidacion() == null) {
            doc.setEstadoValidacion(EstadoValidacionDocumento.PENDIENTE);
        }

        // Un documento de una solicitud o cirugia recibe automaticamente su numero de version
        boolean conOrigen = doc.getIdSolicitud() != null || doc.getIdCirugia() != null;
        if (!conOrigen) {
            try {
                documentoDAO.insert(doc);
            } catch (SQLException e) {
                throw new BLException("Error al registrar el documento de ingreso en la base de datos", e);
            }
            return;
        }

        TransactionsManager.iniciar();
        try {
            insertarConVersion(doc);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("Error al registrar el documento de ingreso: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    // Debe llamarse dentro de una transaccion. Solo se acepta un documento nuevo si no hay uno
    // vigente, o si el vigente fue RECHAZADO (en ese caso es una resubida).
    private void insertarConVersion(DocumentoIngreso doc) throws SQLException, BLException {
        DocumentoIngreso vigente = buscarVigente(doc.getIdSolicitud(), doc.getIdCirugia(), doc.getTipoDocumento());
        if (vigente != null && vigente.getEstadoValidacion() != EstadoValidacionDocumento.RECHAZADO) {
            throw new BLException("Ya existe un documento " + doc.getTipoDocumento()
                    + " vigente (" + vigente.getEstadoValidacion() + "); solo se puede resubir si fue rechazado");
        }
        doc.setVersion(documentoDAO.siguienteVersion(doc.getIdSolicitud(), doc.getIdCirugia(), doc.getTipoDocumento()));
        documentoDAO.insert(doc);
    }

    @Override
    public void update(DocumentoIngreso doc) throws BLException {
        if (doc == null || doc.getIdDocumento() <= 0) {
            throw new BLException("El ID del documento de ingreso debe ser valido para actualizar");
        }
        validarDocumento(doc);
        try {
            documentoDAO.update(doc);
        } catch (SQLException e) {
            throw new BLException("Error al actualizar el documento de ingreso", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("Debe proporcionar un ID valido de documento de ingreso para eliminar");
        }
        try {
            documentoDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("Error al eliminar el documento de ingreso", e);
        }
    }

    @Override
    public DocumentoIngreso findById(Integer id) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("Debe proporcionar un ID valido de documento de ingreso");
        }
        try {
            return documentoDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("Error al consultar el documento de ingreso", e);
        }
    }

    @Override
    public List<DocumentoIngreso> findAll() throws BLException {
        try {
            return documentoDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("Error al listar los documentos de ingreso", e);
        }
    }

    @Override
    public void cambiarEstadoValidacion(Integer idDocumento, EstadoValidacionDocumento nuevoEstado) throws BLException {
        if (idDocumento == null || idDocumento <= 0) {
            throw new BLException("El ID del documento debe ser valido");
        }
        if (nuevoEstado == null) {
            throw new BLException("El nuevo estado de validacion no puede ser nulo");
        }

        // Validar y rechazar tienen reglas propias (responsable, motivo, trazabilidad)
        if (nuevoEstado == EstadoValidacionDocumento.VALIDADO) {
            validarDocumento(idDocumento, null);
            return;
        }
        if (nuevoEstado == EstadoValidacionDocumento.RECHAZADO) {
            throw new BLException("Para rechazar un documento use rechazarDocumento, que exige un motivo");
        }

        DocumentoIngreso doc = findById(idDocumento);
        if (doc == null) {
            throw new BLException("No existe un documento de ingreso registrado con el ID proporcionado");
        }

        doc.setEstadoValidacion(nuevoEstado);
        update(doc);
    }

    // ------------------------------------------------------------------
    // Consulta e historial
    // ------------------------------------------------------------------
    @Override
    public List<DocumentoIngreso> listarPorSolicitud(int idSolicitud) throws BLException {
        validarId(idSolicitud, "solicitud");
        try {
            return documentoDAO.listarPorSolicitud(idSolicitud);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los documentos de la solicitud " + idSolicitud, e);
        }
    }

    @Override
    public List<DocumentoIngreso> listarPorCirugia(int idCirugia) throws BLException {
        validarId(idCirugia, "cirugía");
        try {
            return documentoDAO.listarPorCirugia(idCirugia);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los documentos de la cirugía " + idCirugia, e);
        }
    }

    @Override
    public List<Map<String, Object>> consultarHistorialPorSolicitud(int idSolicitud) throws BLException {
        validarId(idSolicitud, "solicitud");
        try {
            return documentoDAO.historialDocumental(idSolicitud, null);
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar el historial documental de la solicitud " + idSolicitud, e);
        }
    }

    @Override
    public List<Map<String, Object>> consultarHistorialPorCirugia(int idCirugia) throws BLException {
        validarId(idCirugia, "cirugía");
        try {
            return documentoDAO.historialDocumental(null, idCirugia);
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar el historial documental de la cirugía " + idCirugia, e);
        }
    }

    // ------------------------------------------------------------------
    // Validacion, rechazo y resubida
    // ------------------------------------------------------------------
    @Override
    public void validarDocumento(int idDocumento, String responsable) throws BLException {
        DocumentoIngreso doc = buscarDocumento(idDocumento);
        validarEsVigente(doc);
        if (doc.getEstadoValidacion() != EstadoValidacionDocumento.PENDIENTE) {
            throw new BLException("Solo se puede validar un documento PENDIENTE (estado actual: "
                    + doc.getEstadoValidacion() + ")");
        }
        validarOrdenCompraEnCirugiaFinalizada(doc);

        TransactionsManager.iniciar();
        try {
            documentoDAO.actualizarEstadoValidacion(idDocumento, EstadoValidacionDocumento.VALIDADO.name(),
                    LocalDateTime.now(), null, textoResponsable(responsable));
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo validar el documento: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public void rechazarDocumento(int idDocumento, String motivo, String responsable) throws BLException {
        if (motivo == null || motivo.isBlank()) {
            throw new BLException("El rechazo de un documento exige un motivo");
        }
        if (motivo.length() > 200) {
            throw new BLException("El motivo de rechazo no puede superar los 200 caracteres");
        }
        DocumentoIngreso doc = buscarDocumento(idDocumento);
        validarEsVigente(doc);
        if (doc.getEstadoValidacion() != EstadoValidacionDocumento.PENDIENTE) {
            throw new BLException("Solo se puede rechazar un documento PENDIENTE (estado actual: "
                    + doc.getEstadoValidacion() + ")");
        }
        validarOrdenCompraEnCirugiaFinalizada(doc);

        // Documento rechazado + solicitud en DOCUMENTACION_INCOMPLETA: ambos o ninguno
        TransactionsManager.iniciar();
        try {
            LocalDateTime ahora = LocalDateTime.now();
            documentoDAO.actualizarEstadoValidacion(idDocumento, EstadoValidacionDocumento.RECHAZADO.name(),
                    ahora, motivo.trim(), textoResponsable(responsable));

            if (doc.getIdSolicitud() != null) {
                Map<String, Object> solicitud = buscarSolicitud(doc.getIdSolicitud());
                if (CirugiaBL.SOLICITUD_POR_REVISAR.equals(solicitud.get("estadoSolicitud"))) {
                    cirugiaDAO.cambiarEstadoSolicitud(doc.getIdSolicitud(), CirugiaBL.SOLICITUD_DOCUMENTACION_INCOMPLETA,
                            motivo.trim(), ahora);
                }
            }
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo rechazar el documento: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public DocumentoIngreso resubirDocumento(int idDocumentoRechazado, String nuevoArchivoPath,
                                             String responsable) throws BLException {
        if (nuevoArchivoPath == null || nuevoArchivoPath.isBlank()) {
            throw new BLException("Debe indicar el archivo de la nueva versión del documento");
        }
        DocumentoIngreso anterior = buscarDocumento(idDocumentoRechazado);
        validarEsVigente(anterior);
        if (anterior.getEstadoValidacion() != EstadoValidacionDocumento.RECHAZADO) {
            throw new BLException("Solo se puede resubir un documento RECHAZADO (estado actual: "
                    + anterior.getEstadoValidacion() + ")");
        }

        DocumentoIngreso nuevo = new DocumentoIngreso();
        nuevo.setTipoDocumento(anterior.getTipoDocumento());
        nuevo.setArchivoPath(nuevoArchivoPath.trim());
        nuevo.setEstadoValidacion(EstadoValidacionDocumento.PENDIENTE);
        nuevo.setFechaCarga(LocalDateTime.now());
        nuevo.setIdSolicitud(anterior.getIdSolicitud());
        nuevo.setIdCirugia(anterior.getIdCirugia());
        nuevo.setResponsable(textoResponsable(responsable));

        // La version anterior NO se borra ni se modifica: se inserta una version nueva
        TransactionsManager.iniciar();
        try {
            if (anterior.getTipoDocumento() == TipoDocumentoIngreso.ORDEN_COMPRA) {
                nuevo.setOrdenCompra(crearOrdenCompra(nuevo.getArchivoPath()));
            }
            insertarConVersion(nuevo);

            // La subsanacion devuelve la solicitud a revision
            if (anterior.getIdSolicitud() != null) {
                Map<String, Object> solicitud = buscarSolicitud(anterior.getIdSolicitud());
                String estado = (String) solicitud.get("estadoSolicitud");
                if (CirugiaBL.SOLICITUD_DOCUMENTACION_INCOMPLETA.equals(estado)
                        || CirugiaBL.SOLICITUD_RECHAZADA.equals(estado)) {
                    cirugiaDAO.cambiarEstadoSolicitud(anterior.getIdSolicitud(), CirugiaBL.SOLICITUD_POR_REVISAR,
                            (String) solicitud.get("motivoRechazo"), null);
                }
            }
            TransactionsManager.commit();
            return nuevo;
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo resubir el documento: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    // ------------------------------------------------------------------
    // Documentos obligatorios de la solicitud
    // ------------------------------------------------------------------
    @Override
    public List<TipoDocumentoIngreso> listarDocumentosObligatorios(String tipoCliente) throws BLException {
        if (CirugiaBL.TIPO_CLIENTE_PARTICULAR.equals(tipoCliente)) {
            return List.of(TipoDocumentoIngreso.DNI, TipoDocumentoIngreso.RECETA_MEDICA,
                    TipoDocumentoIngreso.COMPROBANTE_PAGO);
        }
        if (CirugiaBL.TIPO_CLIENTE_CLINICA.equals(tipoCliente)) {
            // No se pide comprobante bancario; la orden de compra se adjunta tras culminar la cirugia
            return List.of(TipoDocumentoIngreso.RECETA_MEDICA);
        }
        throw new BLException("Tipo de cliente no soportado: " + tipoCliente);
    }

    @Override
    public boolean verificarDocumentosObligatoriosValidados(int idSolicitud) throws BLException {
        validarId(idSolicitud, "solicitud");
        Map<String, Object> solicitud = buscarSolicitud(idSolicitud);
        List<TipoDocumentoIngreso> obligatorios = listarDocumentosObligatorios((String) solicitud.get("tipoCliente"));

        try {
            List<DocumentoIngreso> vigentes = documentoDAO.listarVigentesPorSolicitud(idSolicitud);
            for (TipoDocumentoIngreso tipo : obligatorios) {
                boolean validado = vigentes.stream().anyMatch(d -> d.getTipoDocumento() == tipo
                        && d.getEstadoValidacion() == EstadoValidacionDocumento.VALIDADO);
                if (!validado) {
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar los documentos de la solicitud " + idSolicitud, e);
        }
    }

    // ------------------------------------------------------------------
    // Orden de compra y cierre
    // ------------------------------------------------------------------
    @Override
    public DocumentoIngreso adjuntarOrdenCompra(int idCirugia, String archivoPath, String responsable) throws BLException {
        validarId(idCirugia, "cirugía");
        if (archivoPath == null || archivoPath.isBlank()) {
            throw new BLException("Debe adjuntar el archivo de la orden de compra");
        }
        Cirugia cirugia = buscarCirugia(idCirugia);
        if (!(cirugia.getCliente() instanceof ClinicaHospital)) {
            throw new BLException("La orden de compra solo aplica a clínicas y hospitales");
        }
        if (cirugia.getEstado() != EstadoCirugia.FINALIZADA) {
            throw new BLException("La orden de compra se habilita cuando la cirugía está FINALIZADA (estado actual: "
                    + cirugia.getEstado().getTexto() + ")");
        }

        DocumentoIngreso doc = new DocumentoIngreso();
        doc.setTipoDocumento(TipoDocumentoIngreso.ORDEN_COMPRA);
        doc.setArchivoPath(archivoPath.trim());
        doc.setEstadoValidacion(EstadoValidacionDocumento.PENDIENTE);
        doc.setFechaCarga(LocalDateTime.now());
        doc.setIdCirugia(idCirugia);
        doc.setResponsable(textoResponsable(responsable));

        // Orden de compra + documento (version nueva): ambos o ninguno
        TransactionsManager.iniciar();
        try {
            doc.setOrdenCompra(crearOrdenCompra(doc.getArchivoPath()));
            insertarConVersion(doc);
            TransactionsManager.commit();
            return doc;
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo adjuntar la orden de compra: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public void validarOrdenCompra(int idDocumento, String responsable) throws BLException {
        validarEsOrdenCompra(buscarDocumento(idDocumento));
        validarDocumento(idDocumento, responsable);
    }

    @Override
    public void rechazarOrdenCompra(int idDocumento, String motivo, String responsable) throws BLException {
        validarEsOrdenCompra(buscarDocumento(idDocumento));
        rechazarDocumento(idDocumento, motivo, responsable);
    }

    @Override
    public boolean verificarDocumentacionCierre(int idCirugia) throws BLException {
        return motivoDocumentacionCierrePendiente(idCirugia) == null;
    }

    @Override
    public void validarDocumentacionCierre(int idCirugia) throws BLException {
        String pendiente = motivoDocumentacionCierrePendiente(idCirugia);
        if (pendiente != null) {
            throw new BLException(pendiente);
        }
    }

    // Unica definicion de la regla de cierre: null = documentacion completa
    private String motivoDocumentacionCierrePendiente(int idCirugia) throws BLException {
        validarId(idCirugia, "cirugía");
        Cirugia cirugia = buscarCirugia(idCirugia);

        try {
            if (cirugia.getCliente() instanceof ClinicaHospital) {
                DocumentoIngreso oc = buscarVigente(null, idCirugia, TipoDocumentoIngreso.ORDEN_COMPRA);
                if (oc == null || oc.getEstadoValidacion() != EstadoValidacionDocumento.VALIDADO) {
                    return "La orden de compra de la cirugía " + idCirugia + " no está VALIDADA";
                }
                return null;
            }
            if (cirugia.getCliente() instanceof PacienteParticular) {
                // El comprobante de pago esta en la solicitud de origen; sin solicitud, en la cirugia
                DocumentoIngreso comprobante = cirugia.getIdSolicitudOrigen() != null
                        ? buscarVigente(cirugia.getIdSolicitudOrigen(), null, TipoDocumentoIngreso.COMPROBANTE_PAGO)
                        : null;
                if (comprobante == null) {
                    comprobante = buscarVigente(null, idCirugia, TipoDocumentoIngreso.COMPROBANTE_PAGO);
                }
                if (comprobante == null || comprobante.getEstadoValidacion() != EstadoValidacionDocumento.VALIDADO) {
                    return "El comprobante de pago del paciente particular no está VALIDADO";
                }
                return null;
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la documentación de la cirugía " + idCirugia, e);
        }
        throw new BLException("La cirugía " + idCirugia + " no tiene un cliente válido");
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------
    private void validarDocumento(DocumentoIngreso doc) throws BLException {
        if (doc == null) {
            throw new BLException("El documento de ingreso no puede ser nulo");
        }
        if (doc.getTipoDocumento() == null) {
            throw new BLException("El tipo de documento de ingreso es obligatorio");
        }
        if (doc.getArchivoPath() == null || doc.getArchivoPath().isBlank()) {
            throw new BLException("La ruta del archivo de respaldo es obligatoria");
        }

        // Quien carga el documento: un usuario de la plataforma o, para los clientes, un responsable (texto)
        boolean sinUsuario = doc.getUsuarioCarga() == null || doc.getUsuarioCarga().getIdUsuario() <= 0;
        boolean sinResponsable = doc.getResponsable() == null || doc.getResponsable().isBlank();
        if (sinUsuario && sinResponsable) {
            throw new BLException("Debe indicar el usuario o responsable que carga el documento");
        }
        if (doc.getIdSolicitud() != null && doc.getIdCirugia() != null) {
            throw new BLException("Un documento pertenece a una solicitud o a una cirugía, no a ambas");
        }

        // Coherencia entre el tipo y la orden de compra asociada
        boolean tieneOrden = doc.getOrdenCompra() != null && doc.getOrdenCompra().getIdOrdenCompra() > 0;
        if (doc.getTipoDocumento() == TipoDocumentoIngreso.ORDEN_COMPRA && !tieneOrden) {
            throw new BLException("Un documento de tipo ORDEN_COMPRA debe tener una orden de compra asociada");
        }
        if (doc.getTipoDocumento() != TipoDocumentoIngreso.ORDEN_COMPRA && tieneOrden) {
            throw new BLException("Solo un documento de tipo ORDEN_COMPRA puede tener una orden de compra asociada");
        }
    }

    // El documento debe ser la version vigente: no se valida ni rechaza una version vieja
    private void validarEsVigente(DocumentoIngreso doc) throws BLException {
        try {
            DocumentoIngreso vigente = buscarVigente(doc.getIdSolicitud(), doc.getIdCirugia(), doc.getTipoDocumento());
            if (vigente != null && vigente.getIdDocumento() != doc.getIdDocumento()) {
                throw new BLException("El documento " + doc.getIdDocumento() + " es una versión anterior; la vigente es la "
                        + vigente.getVersion());
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la versión vigente del documento", e);
        }
    }

    // Un documento sin origen no tiene versiones: solo se puede validar y rechazar el vigente de un origen
    private DocumentoIngreso buscarVigente(Integer idSolicitud, Integer idCirugia, TipoDocumentoIngreso tipo)
            throws SQLException {
        List<DocumentoIngreso> vigentes;
        if (idSolicitud != null) {
            vigentes = documentoDAO.listarVigentesPorSolicitud(idSolicitud);
        } else if (idCirugia != null) {
            vigentes = documentoDAO.listarVigentesPorCirugia(idCirugia);
        } else {
            return null;
        }
        for (DocumentoIngreso d : vigentes) {
            if (d.getTipoDocumento() == tipo) {
                return d;
            }
        }
        return null;
    }

    // Una orden de compra solo se valida/rechaza cuando la cirugia esta FINALIZADA
    private void validarOrdenCompraEnCirugiaFinalizada(DocumentoIngreso doc) throws BLException {
        if (doc.getTipoDocumento() != TipoDocumentoIngreso.ORDEN_COMPRA) {
            return;
        }
        if (doc.getIdCirugia() == null) {
            return; // documento anterior al flujo de cierre: sin cirugía no hay estado que comprobar
        }
        Cirugia cirugia = buscarCirugia(doc.getIdCirugia());
        if (cirugia.getEstado() != EstadoCirugia.FINALIZADA) {
            throw new BLException("La orden de compra solo se valida o rechaza con la cirugía FINALIZADA");
        }
    }

    private void validarEsOrdenCompra(DocumentoIngreso doc) throws BLException {
        if (doc.getTipoDocumento() != TipoDocumentoIngreso.ORDEN_COMPRA) {
            throw new BLException("El documento " + doc.getIdDocumento() + " no es una orden de compra");
        }
    }

    // Debe llamarse dentro de una transaccion (OrdenCompraDAOImpl se une a ella)
    private OrdenCompra crearOrdenCompra(String archivoPath) throws SQLException {
        OrdenCompra orden = new OrdenCompra();
        orden.setArchivoRespaldoPath(archivoPath);
        orden.setFechaRecepcion(LocalDateTime.now());
        ordenCompraDAO.insert(orden);
        return orden;
    }

    private DocumentoIngreso buscarDocumento(int idDocumento) throws BLException {
        validarId(idDocumento, "documento");
        DocumentoIngreso doc = findById(idDocumento);
        if (doc == null) {
            throw new BLException("No existe un documento de ingreso con id " + idDocumento);
        }
        if (!doc.isActivo()) {
            throw new BLException("El documento " + idDocumento + " está dado de baja");
        }
        return doc;
    }

    private Cirugia buscarCirugia(int idCirugia) throws BLException {
        try {
            Cirugia cirugia = cirugiaDAO.findById(idCirugia);
            if (cirugia == null) {
                throw new BLException("No existe una cirugía con id " + idCirugia);
            }
            return cirugia;
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la cirugía " + idCirugia, e);
        }
    }

    private Map<String, Object> buscarSolicitud(int idSolicitud) throws BLException {
        try {
            List<Map<String, Object>> encontradas = cirugiaDAO.buscarSolicitudes(idSolicitud, null, null, null);
            if (encontradas.isEmpty()) {
                throw new BLException("No existe una solicitud con id " + idSolicitud);
            }
            return encontradas.get(0);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la solicitud " + idSolicitud, e);
        }
    }

    private String textoResponsable(String responsable) {
        return (responsable == null || responsable.isBlank()) ? CirugiaBL.RESPONSABLE_SISTEMA : responsable.trim();
    }

    private void validarId(int id, String entidad) throws BLException {
        if (id <= 0) {
            throw new BLException("El id de " + entidad + " no es válido");
        }
    }
}
