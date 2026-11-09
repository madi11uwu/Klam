package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.DocumentoIngresoBL;
import pe.edu.pucp.klam.bl.NotificacionBL;
import pe.edu.pucp.klam.bl.SolicitudCirugiaBL;
import pe.edu.pucp.klam.dao.CirugiaDAO;
import pe.edu.pucp.klam.dao.ClinicaHospitalDAO;
import pe.edu.pucp.klam.dao.DocumentoIngresoDAO;
import pe.edu.pucp.klam.dao.PacienteParticularDAO;
import pe.edu.pucp.klam.dao.impl.CirugiaDAOImpl;
import pe.edu.pucp.klam.dao.impl.ClinicaHospitalDAOImpl;
import pe.edu.pucp.klam.dao.impl.DocumentoIngresoDAOImpl;
import pe.edu.pucp.klam.dao.impl.PacienteParticularDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.EstadoValidacionDocumento;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.TipoDocumentoIngreso;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SolicitudCirugiaBLImpl implements SolicitudCirugiaBL {
    // Límites de los campos de solicitud definidos por el esquema de datos.
    private static final int MAX_PROCEDIMIENTO = 100;
    private static final int MAX_DOCTOR = 100;
    private static final int MAX_DIAGNOSTICO = 300;
    private static final int MAX_MOTIVO = 200;
    private static final int MAX_PACIENTE = 150;
    private static final int MAX_OBSERVACIONES = 500;

    private final CirugiaDAO cirugiaDAO = new CirugiaDAOImpl();
    private final ClinicaHospitalDAO clinicaHospitalDAO = new ClinicaHospitalDAOImpl();
    private final PacienteParticularDAO pacienteParticularDAO = new PacienteParticularDAOImpl();
    private final DocumentoIngresoDAO documentoDAO = new DocumentoIngresoDAOImpl();
    private final DocumentoIngresoBL documentoBL = new DocumentoIngresoBLImpl();
    private final NotificacionBL notificacionBL = new NotificacionBLImpl();

    // ------------------------------------------------------------------
    // Solicitudes de cirugia (RF05, RF07, RF08)
    // ------------------------------------------------------------------
    @Override
    public int registrarSolicitud(Cirugia datos, ClinicaHospital clinicaDestino, String pacienteNombre,
                                  String observaciones) throws BLException {
        if (datos == null) {
            throw new BLException("Los datos de la solicitud no pueden ser nulos");
        }
        Cliente cliente = datos.getCliente();
        if (cliente == null) {
            throw new BLException("La solicitud debe tener un cliente solicitante");
        }
        validarClienteExiste(cliente);

        // Clinica donde se realizara: un cliente clinica opera en su propia sede por defecto
        ClinicaHospital destino = clinicaDestino;
        if (destino == null && cliente instanceof ClinicaHospital clinica) {
            destino = clinica;
        }
        if (destino == null) {
            throw new BLException("Debe indicar la clínica u hospital donde se realizará la cirugía");
        }
        validarClienteExiste(destino);

        // Paciente particular: DNI obligatorio de 8 digitos y nombre del paciente por defecto
        String paciente = pacienteNombre;
        if (cliente instanceof PacienteParticular particular) {
            if (particular.getDni() == null || !particular.getDni().matches("\\d{8}")) {
                throw new BLException("El DNI del paciente es obligatorio y debe tener 8 dígitos");
            }
            if (paciente == null || paciente.isBlank()) {
                paciente = particular.getNombre();
            }
        }
        textoObligatorio(paciente, "El nombre del paciente", MAX_PACIENTE);
        textoObligatorio(datos.getTipoProcedimiento(), "El tipo de procedimiento", MAX_PROCEDIMIENTO);
        textoObligatorio(datos.getDoctorNombre(), "El nombre del médico", MAX_DOCTOR);
        textoObligatorio(datos.getDiagnostico(), "El diagnóstico", MAX_DIAGNOSTICO);
        if (datos.getFechaHoraInicio() == null) {
            throw new BLException("La fecha y hora solicitadas son obligatorias");
        }
        if (observaciones != null && observaciones.trim().length() > MAX_OBSERVACIONES) {
            throw new BLException("Las observaciones no pueden superar los " + MAX_OBSERVACIONES + " caracteres");
        }

        int idSolicitud;
        String resumen;
        TransactionsManager.iniciar();
        try {
            idSolicitud = cirugiaDAO.insertSolicitud(datos, destino.getIdCliente(), paciente.trim(),
                    observaciones != null ? observaciones.trim() : null, LocalDateTime.now());

            // RF11: notificacion interna para los administradores (misma transaccion)
            resumen = "Solicitud " + idSolicitud
                    + " · Cliente: " + cliente.getNombre()
                    + " · Procedimiento: " + datos.getTipoProcedimiento().trim()
                    + " · Fecha solicitada: " + datos.getFechaHoraInicio();
            notificacionBL.notificarAdministradores("Nueva solicitud de cirugía " + idSolicitud, resumen);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo registrar la solicitud de cirugía: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }

        return idSolicitud;
    }

    @Override
    public Map<String, Object> findSolicitudById(int idSolicitud) throws BLException {
        List<Map<String, Object>> filas = consultarSolicitudes(idSolicitud, null, null, null);
        return filas.isEmpty() ? null : filas.get(0);
    }

    @Override
    public List<Map<String, Object>> listarSolicitudes(String estado) throws BLException {
        if (estado != null && !estadosSolicitud().contains(estado)) {
            throw new BLException("Estado de solicitud no soportado: " + estado);
        }
        return consultarSolicitudes(null, estado, null, null);
    }

    @Override
    public List<Map<String, Object>> listarSolicitudesPorCliente(Cliente cliente) throws BLException {
        return consultarSolicitudes(null, null, tipoDe(cliente), cliente.getIdCliente());
    }

    private List<Map<String, Object>> consultarSolicitudes(Integer id, String estado, String tipoCliente,
                                                           Integer idCliente) throws BLException {
        try {
            return cirugiaDAO.buscarSolicitudes(id, estado, tipoCliente, idCliente);
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar las solicitudes de cirugía", e);
        }
    }

    @Override
    public boolean verificarDocumentacionCompleta(int idSolicitud) throws BLException {
        buscarSolicitud(idSolicitud);
        return documentoBL.verificarDocumentosObligatoriosValidados(idSolicitud);
    }

    @Override
    public void aprobarSolicitud(int idSolicitud) throws BLException {
        Map<String, Object> solicitud = buscarSolicitud(idSolicitud);
        String estado = (String) solicitud.get("estadoSolicitud");
        if (!SOLICITUD_POR_REVISAR.equals(estado)) {
            throw new BLException("Solo se puede aprobar una solicitud POR_REVISAR (estado actual: "
                    + estado + ")");
        }

        // La comprobacion y el cambio de estado van juntos: no se aprueba con documentos faltantes
        TransactionsManager.iniciar();
        try {
            if (!documentoBL.verificarDocumentosObligatoriosValidados(idSolicitud)) {
                throw new BLException("No se puede aprobar la solicitud " + idSolicitud
                        + ": faltan documentos obligatorios validados ("
                        + faltantes(idSolicitud, (String) solicitud.get("tipoCliente")) + ")");
            }
            cirugiaDAO.cambiarEstadoSolicitud(idSolicitud, SOLICITUD_APROBADA, null, LocalDateTime.now());
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo aprobar la solicitud: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    // Lista los tipos obligatorios que aún no tienen version vigente VALIDADA
    private String faltantes(int idSolicitud, String tipoCliente) throws BLException {
        try {
            List<DocumentoIngreso> vigentes = documentoDAO.listarVigentesPorSolicitud(idSolicitud);
            List<String> faltan = new ArrayList<>();
            for (TipoDocumentoIngreso tipo : documentoBL.listarDocumentosObligatorios(tipoCliente)) {
                boolean ok = vigentes.stream().anyMatch(d -> d.getTipoDocumento() == tipo && d.getEstadoValidacion() == EstadoValidacionDocumento.VALIDADO);
                if (!ok) {
                    faltan.add(tipo.name());
                }
            }
            return String.join(", ", faltan);
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar los documentos de la solicitud", e);
        }
    }

    @Override
    public void rechazarSolicitud(int idSolicitud, String motivo) throws BLException {
        if (motivo == null || motivo.isBlank()) {
            throw new BLException("El rechazo de una solicitud exige un motivo");
        }
        if (motivo.trim().length() > MAX_MOTIVO) {
            throw new BLException("El motivo de rechazo no puede superar los " + MAX_MOTIVO + " caracteres");
        }
        Map<String, Object> solicitud = buscarSolicitud(idSolicitud);
        String estado = (String) solicitud.get("estadoSolicitud");
        if (!SOLICITUD_POR_REVISAR.equals(estado) && !SOLICITUD_DOCUMENTACION_INCOMPLETA.equals(estado)) {
            throw new BLException("Solo se puede rechazar una solicitud pendiente de revisión (estado actual: "
                    + estado + ")");
        }

        TransactionsManager.iniciar();
        try {
            cirugiaDAO.cambiarEstadoSolicitud(idSolicitud, SOLICITUD_RECHAZADA, motivo.trim(), LocalDateTime.now());
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo rechazar la solicitud" + ": " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public void reenviarSolicitud(int idSolicitud) throws BLException {
        Map<String, Object> solicitud = buscarSolicitud(idSolicitud);
        String estado = (String) solicitud.get("estadoSolicitud");
        if (!SOLICITUD_RECHAZADA.equals(estado) && !SOLICITUD_DOCUMENTACION_INCOMPLETA.equals(estado)) {
            throw new BLException("Solo se puede reenviar una solicitud rechazada o con documentación incompleta "
                    + "(estado actual: " + estado + ")");
        }

        // No se reenvia con documentos obligatorios ausentes o todavia rechazados
        try {
            List<DocumentoIngreso> vigentes = documentoDAO.listarVigentesPorSolicitud(idSolicitud);
            for (TipoDocumentoIngreso tipo : documentoBL.listarDocumentosObligatorios((String) solicitud.get("tipoCliente"))) {
                boolean presente = vigentes.stream().anyMatch(d -> d.getTipoDocumento() == tipo
                        && d.getEstadoValidacion() != EstadoValidacionDocumento.RECHAZADO);
                if (!presente) {
                    throw new BLException("Falta subsanar el documento " + tipo
                            + ": resúbalo antes de reenviar la solicitud");
                }
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar los documentos de la solicitud", e);
        }

        TransactionsManager.iniciar();
        try {
            cirugiaDAO.cambiarEstadoSolicitud(idSolicitud, SOLICITUD_POR_REVISAR,
                        (String) solicitud.get("motivoRechazo"), null);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo reenviar la solicitud" + ": " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public int registrarCirugiaDesdeSolicitud(int idSolicitud, Cirugia cirugia, String responsable) throws BLException {
        Map<String, Object> solicitud = buscarSolicitud(idSolicitud);
        String estado = (String) solicitud.get("estadoSolicitud");
        if (!SOLICITUD_APROBADA.equals(estado)) {
            throw new BLException("Solo se registra una cirugía desde una solicitud APROBADA (estado actual: "
                    + estado + ")");
        }
        try {
            Integer existente = cirugiaDAO.buscarCirugiaVigentePorSolicitud(idSolicitud);
            if (existente != null) {
                throw new BLException("La solicitud " + idSolicitud
                        + " ya tiene una cirugía activa (id " + existente + ")");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar las cirugías de la solicitud", e);
        }

        // La cirugia es un registro NUEVO: lo no informado se toma de la solicitud, que no se modifica
        Cirugia nueva = cirugia != null ? new Cirugia(cirugia) : new Cirugia();
        nueva.setIdCirugia(0);
        if (nueva.getFechaHoraInicio() == null) {
            nueva.setFechaHoraInicio((LocalDateTime) solicitud.get("fechaHoraSolicitada"));
        }
        if (nueva.getTipoProcedimiento() == null || nueva.getTipoProcedimiento().isBlank()) {
            nueva.setTipoProcedimiento((String) solicitud.get("tipoProcedimiento"));
        }
        if (nueva.getDoctorNombre() == null || nueva.getDoctorNombre().isBlank()) {
            nueva.setDoctorNombre((String) solicitud.get("doctorNombre"));
        }
        if (nueva.getDiagnostico() == null || nueva.getDiagnostico().isBlank()) {
            nueva.setDiagnostico((String) solicitud.get("diagnostico"));
        }

        Cliente solicitante = clienteDeSolicitud(solicitud);
        if (nueva.getCliente() != null
                && (!tipoDe(nueva.getCliente()).equals(tipoDe(solicitante))
                    || nueva.getCliente().getIdCliente() != solicitante.getIdCliente())) {
            throw new BLException("El cliente de la cirugía no coincide con el de la solicitud");
        }
        nueva.setCliente(solicitante);
        nueva.setIdSolicitudOrigen(idSolicitud);
        nueva.setActivo(true);

        // El registro común confirma cirugía, auditoría y notificaciones en una sola transacción.
        // La restricción uq_cirugia_solicitud_vigente protege también de solicitudes duplicadas concurrentes.
        return new CirugiaBLImpl().registrarInterno(nueva, responsable);
    }

    private Cliente clienteDeSolicitud(Map<String, Object> solicitud) throws BLException {
        int idCliente = ((Number) solicitud.get("idCliente")).intValue();
        try {
            Cliente cliente = CirugiaBL.TIPO_CLIENTE_CLINICA.equals(solicitud.get("tipoCliente"))
                    ? clinicaHospitalDAO.findById(idCliente)
                    : pacienteParticularDAO.findById(idCliente);
            if (cliente == null) {
                throw new BLException("No existe el cliente de la solicitud (id " + idCliente + ")");
            }
            return cliente;
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el cliente de la solicitud", e);
        }
    }

    private Map<String, Object> buscarSolicitud(int idSolicitud) throws BLException {
        if (idSolicitud <= 0) {
            throw new BLException("El id de la solicitud no es válido");
        }
        Map<String, Object> solicitud = findSolicitudById(idSolicitud);
        if (solicitud == null) {
            throw new BLException("No existe una solicitud con id " + idSolicitud);
        }
        return solicitud;
    }

    private Set<String> estadosSolicitud() {
        return Set.of(SOLICITUD_POR_REVISAR, SOLICITUD_DOCUMENTACION_INCOMPLETA, SOLICITUD_RECHAZADA, SOLICITUD_APROBADA);
    }


    private String tipoDe(Cliente cliente) throws BLException {
        if (cliente == null) {
            throw new BLException("Debe indicar el cliente");
        }
        if (cliente instanceof ClinicaHospital) {
            return CirugiaBL.TIPO_CLIENTE_CLINICA;
        }
        if (cliente instanceof PacienteParticular) {
            return CirugiaBL.TIPO_CLIENTE_PARTICULAR;
        }
        throw new BLException("Tipo de cliente no soportado");
    }

    private void validarClienteExiste(Cliente cliente) throws BLException {
        String tipoCliente = tipoDe(cliente);
        try {
            Cliente encontrado = CirugiaBL.TIPO_CLIENTE_CLINICA.equals(tipoCliente)
                    ? clinicaHospitalDAO.findById(cliente.getIdCliente())
                    : pacienteParticularDAO.findById(cliente.getIdCliente());
            if (encontrado == null) {
                throw new BLException("No existe un cliente con id " + cliente.getIdCliente());
            }
            if (!encontrado.isActivo()) {
                throw new BLException("El cliente " + encontrado.getNombre() + " está dado de baja");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia del cliente", e);
        }
    }

    private void textoObligatorio(String texto, String campo, int maximo) throws BLException {
        if (texto == null || texto.isBlank()) {
            throw new BLException(campo + " es obligatorio");
        }
        if (texto.trim().length() > maximo) {
            throw new BLException(campo + " no puede superar los " + maximo + " caracteres");
        }
    }
}
