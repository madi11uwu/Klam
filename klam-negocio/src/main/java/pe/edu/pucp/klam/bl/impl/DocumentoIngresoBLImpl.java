package pe.edu.pucp.klam.bl.impl;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.DocumentoIngresoBL;
import pe.edu.pucp.klam.dao.CirugiaDAO;
import pe.edu.pucp.klam.dao.DocumentoIngresoDAO;
import pe.edu.pucp.klam.dao.OrdenCompraDAO;
import pe.edu.pucp.klam.dao.SolicitudCirugiaDAO;
import pe.edu.pucp.klam.dao.impl.CirugiaDAOImpl;
import pe.edu.pucp.klam.dao.impl.DocumentoIngresoDAOImpl;
import pe.edu.pucp.klam.dao.impl.compras.OrdenCompraDAOImpl;
import pe.edu.pucp.klam.dao.impl.SolicitudCirugiaDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoSolicitudCirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.SolicitudCirugia;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.EstadoValidacionDocumento;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.OrdenCompra;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.TipoDocumentoIngreso;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Administrador;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;

public class DocumentoIngresoBLImpl implements DocumentoIngresoBL {

    private final DocumentoIngresoDAO documentoDAO = new DocumentoIngresoDAOImpl();
    private final OrdenCompraDAO ordenCompraDAO = new OrdenCompraDAOImpl();
    private final CirugiaDAO cirugiaDAO = new CirugiaDAOImpl();
    private final SolicitudCirugiaDAO solicitudDAO = new SolicitudCirugiaDAOImpl();

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
        boolean conOrigen = doc.getCirugia() != null || doc.getSolicitud() != null;
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
            insertarConVersion(doc, false);
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
    private void insertarConVersion(DocumentoIngreso doc, boolean resubidaAutenticada) throws SQLException, BLException {
        DocumentoIngreso vigente = buscarVigente(doc.getSolicitud() != null ? doc.getSolicitud().getIdSolicitud() : null, doc.getCirugia() != null ? doc.getCirugia().getIdCirugia() : null, doc.getTipoDocumento());
        if (vigente != null && vigente.getEstadoValidacion() != EstadoValidacionDocumento.RECHAZADO) {
            throw new BLException("Ya existe un documento " + doc.getTipoDocumento()
                    + " vigente (" + vigente.getEstadoValidacion() + "); solo se puede resubir si fue rechazado");
        }
        if (vigente != null && doc.getSolicitud() != null && !resubidaAutenticada) {
            throw new BLException("La resubida de una solicitud exige verificar al presentador original");
        }
        if (vigente != null && doc.getSolicitud() != null && !vigente.isHabilitarResubida()) {
            throw new BLException("La resubida de este documento no fue habilitada por el administrador");
        }
        doc.setVersion(documentoDAO.siguienteVersion(doc.getSolicitud() != null ? doc.getSolicitud().getIdSolicitud() : null, doc.getCirugia() != null ? doc.getCirugia().getIdCirugia() : null, doc.getTipoDocumento()));
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
            List<DocumentoIngreso> documentos = new ArrayList<>(documentoDAO.listarPorCirugia(idCirugia));
            Cirugia cirugia = cirugiaDAO.findById(idCirugia);
            if (cirugia != null && cirugia.getSolicitudOrigen() != null) {
                documentos.addAll(documentoDAO.listarPorSolicitud(cirugia.getSolicitudOrigen().getIdSolicitud()));
            }
            return documentos;
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
            List<Map<String, Object>> historial = new ArrayList<>(documentoDAO.historialDocumental(null, idCirugia));
            Cirugia cirugia = cirugiaDAO.findById(idCirugia);
            if (cirugia != null && cirugia.getSolicitudOrigen() != null) {
                historial.addAll(documentoDAO.historialDocumental(cirugia.getSolicitudOrigen().getIdSolicitud(), null));
            }
            return historial;
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar el historial documental de la cirugía " + idCirugia, e);
        }
    }

    @Override
    public DocumentoIngreso registrarDocumentoSolicitud(int idSolicitud, TipoDocumentoIngreso tipo,
            String archivoPath, int idUsuarioAutenticado, String rolAutenticado,
            String responsable) throws BLException {
        validarId(idSolicitud, "solicitud");
        if (idUsuarioAutenticado <= 0 || (!"ADMINISTRADOR".equals(rolAutenticado)
                && !"VENDEDOR".equals(rolAutenticado) && !esRolCliente(rolAutenticado))) {
            throw new SecurityException("La carga exige un usuario autenticado del personal o el cliente de la solicitud");
        }
        if (tipo == null || archivoPath == null || archivoPath.isBlank()) {
            throw new BLException("El tipo y el archivo son obligatorios");
        }
        SolicitudCirugia datosSolicitud = buscarSolicitud(idSolicitud);
        if ("VENDEDOR".equals(rolAutenticado)
                && (datosSolicitud.getVendedorRegistrador() == null
                || datosSolicitud.getVendedorRegistrador().getIdUsuario() != idUsuarioAutenticado)) {
            throw new SecurityException("El vendedor solo puede adjuntar documentos a sus solicitudes");
        }
        String estado = datosSolicitud.getEstado().name();
        if (!EstadoSolicitudCirugia.POR_REVISAR.name().equals(estado)
                && !EstadoSolicitudCirugia.DOCUMENTACION_INCOMPLETA.name().equals(estado)) {
            throw new BLException("La solicitud no admite documentos en estado " + estado);
        }
        if (!listarDocumentosObligatorios(tipoCliente(datosSolicitud)).contains(tipo)) {
            throw new BLException("El tipo de documento no corresponde a esta solicitud");
        }

        DocumentoIngreso doc = new DocumentoIngreso();
        doc.setTipoDocumento(tipo);
        doc.setArchivoPath(archivoPath);
        doc.setFechaCarga(LocalDateTime.now());
        pe.edu.pucp.klam.modelo.agendaoperaciones.SolicitudCirugia solicitud =
                new pe.edu.pucp.klam.modelo.agendaoperaciones.SolicitudCirugia();
        solicitud.setIdSolicitud(idSolicitud);
        doc.setSolicitud(solicitud);
        // La propiedad de la solicitud del cliente la comprueba el recurso REST con los datos de la sesión.
        UsuarioPlataforma presentador = "VENDEDOR".equals(rolAutenticado) ? new Vendedor()
                : esRolCliente(rolAutenticado) ? new pe.edu.pucp.klam.modelo.usuariosPermisos.CuentaUsuario()
                : new Administrador();
        presentador.setIdUsuario(idUsuarioAutenticado);
        doc.setUsuarioCarga(presentador);
        doc.setResponsable(textoResponsable(responsable));
        insert(doc);
        return doc;
    }

    @Override
    public List<DocumentoIngreso> listarPresentadosPorUsuario(Integer idSolicitud,
            int idUsuarioAutenticado, String rolAutenticado) throws BLException {
        if (idUsuarioAutenticado <= 0 || rolAutenticado == null) {
            throw new SecurityException("La consulta exige un usuario autenticado");
        }
        try {
            List<DocumentoIngreso> documentos = idSolicitud == null
                    ? documentoDAO.findAll() : documentoDAO.listarPorSolicitud(idSolicitud);
            Map<String, UsuarioPlataforma> originales = new HashMap<>();
            for (DocumentoIngreso doc : documentos) {
                if (doc.getVersion() == 1) originales.put(claveOrigen(doc), doc.getUsuarioCarga());
            }
            List<DocumentoIngreso> propios = new ArrayList<>();
            for (DocumentoIngreso doc : documentos) {
                UsuarioPlataforma original = originales.get(claveOrigen(doc));
                if (doc.isActivo() && coincidePresentador(original, idUsuarioAutenticado, rolAutenticado)) propios.add(doc);
            }
            return propios;
        } catch (SQLException e) {
            throw new BLException("No se pudieron consultar los documentos presentados", e);
        }
    }

    private String claveOrigen(DocumentoIngreso doc) {
        String origen = doc.getSolicitud() != null
                ? "S" + doc.getSolicitud().getIdSolicitud()
                : doc.getCirugia() != null ? "C" + doc.getCirugia().getIdCirugia() : "D" + doc.getIdDocumento();
        return origen + ":" + doc.getTipoDocumento();
    }

    // El cliente de la sesión es dueño de la solicitud del documento (mismo tipo e id de cliente).
    private boolean esClienteDuenio(DocumentoIngreso doc, String rol,
                                    pe.edu.pucp.klam.modelo.clientes.Cliente clienteSesion) throws BLException {
        if (clienteSesion == null || !esRolCliente(rol) || doc.getSolicitud() == null) return false;
        SolicitudCirugia solicitud = buscarSolicitud(doc.getSolicitud().getIdSolicitud());
        var cliente = solicitud.getCliente();
        return cliente != null && cliente.getIdCliente() == clienteSesion.getIdCliente()
                && cliente.getClass() == clienteSesion.getClass();
    }

    private static boolean esRolCliente(String rol) {
        return "CLINICA_HOSPITAL".equals(rol) || "PACIENTE_PARTICULAR".equals(rol);
    }

    // El DAO carga al presentador solo con su id y su tipo: una cuenta de cliente se reconoce por su tipo
    // (CuentaUsuario) y un rol de cliente; el personal, por su rol.
    private static boolean coincidePresentador(UsuarioPlataforma usuario, int idUsuario, String rol) {
        if (usuario == null || rol == null || usuario.getIdUsuario() != idUsuario) return false;
        if (usuario instanceof pe.edu.pucp.klam.modelo.usuariosPermisos.CuentaUsuario) return esRolCliente(rol);
        return rol.equals(usuario.getRol());
    }

    @Override
    public boolean esPresentadorOriginal(int idDocumento, int idUsuarioAutenticado,
            String rolAutenticado) throws BLException {
        DocumentoIngreso doc = buscarDocumento(idDocumento);
        if (idUsuarioAutenticado <= 0 || rolAutenticado == null
                || (doc.getSolicitud() == null && doc.getCirugia() == null)) {
            return false;
        }
        try {
            DocumentoIngreso original = buscarVersionOriginal(doc);
            UsuarioPlataforma usuario = original == null ? null : original.getUsuarioCarga();
            return coincidePresentador(usuario, idUsuarioAutenticado, rolAutenticado);
        } catch (SQLException e) {
            throw new BLException("No se pudo comprobar la identidad del presentador original", e);
        }
    }

    @Override
    public DocumentoIngreso verificarResubida(int idDocumento, int idUsuarioAutenticado,
            String rolAutenticado) throws BLException {
        return verificarResubida(idDocumento, idUsuarioAutenticado, rolAutenticado, null);
    }

    @Override
    public DocumentoIngreso verificarResubida(int idDocumento, int idUsuarioAutenticado,
            String rolAutenticado, pe.edu.pucp.klam.modelo.clientes.Cliente clienteSesion) throws BLException {
        DocumentoIngreso doc = buscarDocumento(idDocumento);
        validarEsVigente(doc);
        if (doc.getEstadoValidacion() != EstadoValidacionDocumento.RECHAZADO) {
            throw new BLException("Solo se puede resubir un documento RECHAZADO");
        }
        if (!doc.isHabilitarResubida()) {
            throw new BLException("La resubida de este documento no fue habilitada por el administrador");
        }
        if (!esPresentadorOriginal(idDocumento, idUsuarioAutenticado, rolAutenticado)
                && !esClienteDuenio(doc, rolAutenticado, clienteSesion)) {
            throw new SecurityException("Solo quien presentó el documento o el cliente de la solicitud puede resubirlo");
        }
        return doc;
    }

    private DocumentoIngreso buscarVersionOriginal(DocumentoIngreso doc) throws SQLException {
        List<DocumentoIngreso> versiones = doc.getSolicitud() != null
                ? documentoDAO.listarPorSolicitud(doc.getSolicitud().getIdSolicitud())
                : documentoDAO.listarPorCirugia(doc.getCirugia().getIdCirugia());
        return versiones.stream()
                .filter(version -> version.getTipoDocumento() == doc.getTipoDocumento()
                        && version.getVersion() == 1)
                .findFirst().orElse(null);
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
                    LocalDateTime.now(), null, textoResponsable(responsable), false);
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
    public void rechazarDocumento(int idDocumento, String motivo, boolean habilitarResubida,
                                  String responsable) throws BLException {
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
                    ahora, motivo.trim(), textoResponsable(responsable), habilitarResubida);

            if (doc.getSolicitud() != null) {
                SolicitudCirugia solicitud = buscarSolicitud(doc.getSolicitud().getIdSolicitud());
                if (solicitud.getEstado() == EstadoSolicitudCirugia.POR_REVISAR) {
                    solicitud.setEstado(EstadoSolicitudCirugia.DOCUMENTACION_INCOMPLETA);
                    solicitud.setMotivoRechazo(motivo.trim());
                    solicitud.setFechaRespuesta(ahora);
                    solicitudDAO.cambiarEstado(solicitud);
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
    public void rechazarDocumento(int idDocumento, String motivo, String responsable) throws BLException {
        rechazarDocumento(idDocumento, motivo, false, responsable);
    }

    @Override
    public DocumentoIngreso resubirDocumento(int idDocumentoRechazado, String nuevoArchivoPath,
                                             int idUsuarioAutenticado, String rolAutenticado,
                                             String responsable) throws BLException {
        return resubirDocumento(idDocumentoRechazado, nuevoArchivoPath, idUsuarioAutenticado, rolAutenticado,
                responsable, null);
    }

    @Override
    public DocumentoIngreso resubirDocumento(int idDocumentoRechazado, String nuevoArchivoPath,
                                             int idUsuarioAutenticado, String rolAutenticado,
                                             String responsable, pe.edu.pucp.klam.modelo.clientes.Cliente clienteSesion)
            throws BLException {
        if (nuevoArchivoPath == null || nuevoArchivoPath.isBlank()) {
            throw new BLException("Debe indicar el archivo de la nueva versión del documento");
        }
        if (idUsuarioAutenticado <= 0 || rolAutenticado == null || rolAutenticado.isBlank()) {
            throw new BLException("La resubida exige un usuario autenticado");
        }

        TransactionsManager.iniciar();
        try {
            DocumentoIngreso anterior = verificarResubida(
                    idDocumentoRechazado, idUsuarioAutenticado, rolAutenticado, clienteSesion);
            DocumentoIngreso original = buscarVersionOriginal(anterior);

            DocumentoIngreso nuevo = new DocumentoIngreso();
            nuevo.setTipoDocumento(anterior.getTipoDocumento());
            nuevo.setArchivoPath(nuevoArchivoPath.trim());
            nuevo.setEstadoValidacion(EstadoValidacionDocumento.PENDIENTE);
            nuevo.setFechaCarga(LocalDateTime.now());
            nuevo.setSolicitud(anterior.getSolicitud());
            nuevo.setCirugia(anterior.getCirugia());
            nuevo.setUsuarioCarga(original.getUsuarioCarga());
            nuevo.setResponsable(textoResponsable(responsable));

            // La version anterior NO se borra ni se modifica: se inserta una version nueva.
            if (anterior.getTipoDocumento() == TipoDocumentoIngreso.ORDEN_COMPRA) {
                nuevo.setOrdenCompra(crearOrdenCompra(nuevo.getArchivoPath()));
            }
            insertarConVersion(nuevo, true);

            if (anterior.getSolicitud() != null) {
                SolicitudCirugia solicitud = buscarSolicitud(anterior.getSolicitud().getIdSolicitud());
                if (solicitud.getEstado() == EstadoSolicitudCirugia.DOCUMENTACION_INCOMPLETA
                        || solicitud.getEstado() == EstadoSolicitudCirugia.RECHAZADA) {
                    solicitud.setEstado(EstadoSolicitudCirugia.POR_REVISAR);
                    // NULL conserva la fecha de revisión mediante COALESCE en el procedimiento.
                    solicitud.setFechaRespuesta(null);
                    solicitudDAO.cambiarEstado(solicitud);
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
        SolicitudCirugia solicitud = buscarSolicitud(idSolicitud);
        List<TipoDocumentoIngreso> obligatorios = listarDocumentosObligatorios(tipoCliente(solicitud));

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
                    + cirugia.getEstado().name() + ")");
        }

        DocumentoIngreso doc = new DocumentoIngreso();
        doc.setTipoDocumento(TipoDocumentoIngreso.ORDEN_COMPRA);
        doc.setArchivoPath(archivoPath.trim());
        doc.setEstadoValidacion(EstadoValidacionDocumento.PENDIENTE);
        doc.setFechaCarga(LocalDateTime.now());
        doc.setCirugia(cirugia);
        doc.setResponsable(textoResponsable(responsable));

        // Orden de compra + documento (version nueva): ambos o ninguno
        TransactionsManager.iniciar();
        try {
            doc.setOrdenCompra(crearOrdenCompra(doc.getArchivoPath()));
            insertarConVersion(doc, false);
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
                DocumentoIngreso comprobante = cirugia.getSolicitudOrigen() != null
                        ? buscarVigente(cirugia.getSolicitudOrigen().getIdSolicitud(), null, TipoDocumentoIngreso.COMPROBANTE_PAGO)
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
        if (doc.getSolicitud() != null && doc.getCirugia() != null) {
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
            DocumentoIngreso vigente = buscarVigente(doc.getSolicitud() != null ? doc.getSolicitud().getIdSolicitud() : null, doc.getCirugia() != null ? doc.getCirugia().getIdCirugia() : null, doc.getTipoDocumento());
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
        if (doc.getCirugia() == null) {
            return; // documento anterior al flujo de cierre: sin cirugía no hay estado que comprobar
        }
        Cirugia cirugia = buscarCirugia(doc.getCirugia().getIdCirugia());
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

    private SolicitudCirugia buscarSolicitud(int idSolicitud) throws BLException {
        try {
            SolicitudCirugia solicitud = solicitudDAO.findById(idSolicitud);
            if (solicitud == null) {
                throw new BLException("No existe una solicitud con id " + idSolicitud);
            }
            return solicitud;
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la solicitud " + idSolicitud, e);
        }
    }

    private String tipoCliente(SolicitudCirugia solicitud) {
        return solicitud.getCliente() instanceof ClinicaHospital
                ? CirugiaBL.TIPO_CLIENTE_CLINICA : CirugiaBL.TIPO_CLIENTE_PARTICULAR;
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
