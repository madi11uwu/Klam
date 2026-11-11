
package pe.edu.pucp.klam.bl.impl;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.DocumentoIngresoBL;
import pe.edu.pucp.klam.bl.NotificacionBL;
import pe.edu.pucp.klam.bl.SolicitudCirugiaBL;

import pe.edu.pucp.klam.dao.CirugiaDAO;
import pe.edu.pucp.klam.dao.ClinicaHospitalDAO;
import pe.edu.pucp.klam.dao.PacienteParticularDAO;
import pe.edu.pucp.klam.dao.DocumentoIngresoDAO;
import pe.edu.pucp.klam.dao.SolicitudCirugiaDAO;
import pe.edu.pucp.klam.dao.VendedorDAO;

import pe.edu.pucp.klam.dao.impl.CirugiaDAOImpl;
import pe.edu.pucp.klam.dao.impl.ClinicaHospitalDAOImpl;
import pe.edu.pucp.klam.dao.impl.DocumentoIngresoDAOImpl;
import pe.edu.pucp.klam.dao.impl.PacienteParticularDAOImpl;
import pe.edu.pucp.klam.dao.impl.SolicitudCirugiaDAOImpl;
import pe.edu.pucp.klam.dao.impl.VendedorDAOImpl;

import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoSolicitudCirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.SolicitudCirugia;

import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;

import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.EstadoValidacionDocumento;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.TipoDocumentoIngreso;

public class SolicitudCirugiaBLImpl implements SolicitudCirugiaBL {

    private static final int MAX_PROCEDIMIENTO = 100;
    private static final int MAX_DOCTOR = 100;
    private static final int MAX_DIAGNOSTICO = 300;
    private static final int MAX_MOTIVO = 200;
    private static final int MAX_PACIENTE = 150;
    private static final int MAX_OBSERVACIONES = 500;

    private final SolicitudCirugiaDAO solicitudDAO =
            new SolicitudCirugiaDAOImpl();

    private final VendedorDAO vendedorDAO =
            new VendedorDAOImpl();

    private final CirugiaDAO cirugiaDAO =
            new CirugiaDAOImpl();

    private final ClinicaHospitalDAO clinicaDAO =
            new ClinicaHospitalDAOImpl();

    private final PacienteParticularDAO pacienteDAO =
            new PacienteParticularDAOImpl();

    private final DocumentoIngresoDAO documentoDAO =
            new DocumentoIngresoDAOImpl();

    private final DocumentoIngresoBL documentoBL =
            new DocumentoIngresoBLImpl();

    private final NotificacionBL notificacionBL =
            new NotificacionBLImpl();

    @Override
    public int registrarSolicitud(
            SolicitudCirugia solicitud) throws BLException {

        if (solicitud == null) {
            throw new BLException(
                    "La solicitud no puede ser nula");
        }

        Cliente cliente = solicitud.getCliente();

        if (cliente == null) {
            throw new BLException(
                    "La solicitud debe tener un cliente solicitante");
        }

        cliente = validarClienteExiste(cliente);
        solicitud.setCliente(cliente);

        Vendedor vendedor = solicitud.getVendedorRegistrador();
        if (vendedor != null) {
            validarVendedorExiste(vendedor);
        }

        ClinicaHospital destino = solicitud.getClinicaDestino();

        if (destino == null && cliente instanceof ClinicaHospital clinica) {
            destino = clinica;
            solicitud.setClinicaDestino(clinica);
        }

        if (destino == null) {
            throw new BLException(
                    "Debe indicar la clínica u hospital de destino");
        }

        destino = (ClinicaHospital) validarClienteExiste(destino);
        solicitud.setClinicaDestino(destino);

        String paciente = solicitud.getPacienteNombre();

        if (cliente instanceof PacienteParticular particular) {
            if (particular.getDni() == null
                    || !particular.getDni().matches("\\d{8}")) {
                throw new BLException(
                        "El DNI del paciente debe tener 8 dígitos");
            }

            if (paciente == null || paciente.isBlank()) {
                paciente = particular.getNombre();
            }
        }

        textoObligatorio(paciente,
                "El nombre del paciente", MAX_PACIENTE);

        textoObligatorio(solicitud.getTipoProcedimiento(),
                "El tipo de procedimiento", MAX_PROCEDIMIENTO);

        textoObligatorio(solicitud.getDoctorNombre(),
                "El nombre del médico", MAX_DOCTOR);

        textoObligatorio(solicitud.getDiagnostico(),
                "El diagnóstico", MAX_DIAGNOSTICO);

        if (solicitud.getFechaHoraSolicitada() == null) {
            throw new BLException(
                    "La fecha y hora solicitadas son obligatorias");
        }

        if (solicitud.getObservaciones() != null
                && solicitud.getObservaciones().trim().length()
                > MAX_OBSERVACIONES) {
            throw new BLException(
                    "Las observaciones no pueden superar "
                            + MAX_OBSERVACIONES + " caracteres");
        }

        solicitud.setPacienteNombre(paciente.trim());

        solicitud.setTipoProcedimiento(
                solicitud.getTipoProcedimiento().trim());

        solicitud.setDoctorNombre(
                solicitud.getDoctorNombre().trim());

        solicitud.setDiagnostico(
                solicitud.getDiagnostico().trim());

        if (solicitud.getObservaciones() != null) {
            solicitud.setObservaciones(
                    solicitud.getObservaciones().trim());
        }

        solicitud.setEstado(
                EstadoSolicitudCirugia.POR_REVISAR);

        solicitud.setFechaSolicitud(LocalDateTime.now());

        TransactionsManager.iniciar();

        try {
            solicitudDAO.insert(solicitud);

            String resumen = "Solicitud " + solicitud.getIdSolicitud()
                    + " · Cliente: " + cliente.getNombre()
                    + " · Procedimiento: " + solicitud.getTipoProcedimiento()
                    + " · Fecha solicitada: "
                    + solicitud.getFechaHoraSolicitada();

            notificacionBL.notificarAdministradores(
                    "Nueva solicitud de cirugía "
                            + solicitud.getIdSolicitud(),
                    resumen);

            TransactionsManager.commit();

            return solicitud.getIdSolicitud();

        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException(
                    "No se pudo registrar la solicitud", e);

        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public SolicitudCirugia findSolicitudById(
            int idSolicitud) throws BLException {

        if (idSolicitud <= 0) {
            throw new BLException(
                    "El ID de la solicitud no es válido");
        }

        try {
            return solicitudDAO.findById(idSolicitud);

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo consultar la solicitud", e);
        }
    }

    @Override
    public List<SolicitudCirugia> listarSolicitudes(
            String estado) throws BLException {

        EstadoSolicitudCirugia filtro = null;

        if (estado != null && !estado.isBlank()) {
            try {
                filtro = EstadoSolicitudCirugia.valueOf(estado);
            } catch (IllegalArgumentException e) {
                throw new BLException(
                        "Estado de solicitud no soportado: " + estado);
            }
        }

        try {
            return filtro == null
                    ? solicitudDAO.findAll()
                    : solicitudDAO.listarPorEstado(filtro);

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudieron listar las solicitudes", e);
        }
    }

    @Override
    public List<SolicitudCirugia> listarSolicitudesPorCliente(
            Cliente cliente) throws BLException {

        tipoDe(cliente);

        try {
            return solicitudDAO.listarPorCliente(cliente);

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudieron listar las solicitudes del cliente", e);
        }
    }

    @Override
    public List<SolicitudCirugia> listarSolicitudesPorVendedor(
            Vendedor vendedor) throws BLException {
        validarVendedorExiste(vendedor);
        try {
            return solicitudDAO.listarPorVendedor(vendedor);
        } catch (SQLException e) {
            throw new BLException("No se pudieron listar las solicitudes del vendedor", e);
        }
    }

    @Override
    public boolean verificarDocumentacionCompleta(
            int idSolicitud) throws BLException {

        buscarSolicitud(idSolicitud);

        return documentoBL
                .verificarDocumentosObligatoriosValidados(idSolicitud);
    }

    @Override
    public void aprobarSolicitud(
            int idSolicitud) throws BLException {

        TransactionsManager.iniciar();

        try {
            SolicitudCirugia solicitud =
                    buscarSolicitud(idSolicitud);

            if (solicitud.getEstado()
                    != EstadoSolicitudCirugia.POR_REVISAR) {
                throw new BLException(
                        "Solo se puede aprobar una solicitud POR_REVISAR");
            }

            if (!documentoBL
                    .verificarDocumentosObligatoriosValidados(
                            idSolicitud)) {
                throw new BLException(
                        "Faltan documentos obligatorios validados: "
                                + faltantes(solicitud));
            }

            solicitud.setEstado(
                    EstadoSolicitudCirugia.APROBADA);

            solicitud.setMotivoRechazo(null);
            solicitud.setFechaRespuesta(LocalDateTime.now());

            solicitudDAO.cambiarEstado(solicitud);

            TransactionsManager.commit();

        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException(
                    "No se pudo aprobar la solicitud", e);

        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public void rechazarSolicitud(
            int idSolicitud,
            String motivo) throws BLException {

        textoObligatorio(motivo,
                "El motivo de rechazo", MAX_MOTIVO);

        TransactionsManager.iniciar();

        try {
            SolicitudCirugia solicitud =
                    buscarSolicitud(idSolicitud);

            if (solicitud.getEstado()
                    != EstadoSolicitudCirugia.POR_REVISAR
                    && solicitud.getEstado()
                    != EstadoSolicitudCirugia.DOCUMENTACION_INCOMPLETA) {

                throw new BLException(
                        "Solo se puede rechazar una solicitud pendiente de revisión");
            }

            solicitud.setEstado(
                    EstadoSolicitudCirugia.RECHAZADA);

            solicitud.setMotivoRechazo(motivo.trim());
            solicitud.setFechaRespuesta(LocalDateTime.now());

            solicitudDAO.cambiarEstado(solicitud);

            // Si la registró un vendedor, le llega el aviso con el motivo (misma transacción que el rechazo).
            Vendedor registrador = solicitud.getVendedorRegistrador();
            if (registrador != null) {
                notificacionBL.notificarVendedor(
                        registrador.getIdUsuario(),
                        "Solicitud " + idSolicitud + " rechazada",
                        "Se rechazó la solicitud " + idSolicitud + " (" + solicitud.getTipoProcedimiento()
                                + "). Motivo: " + motivo.trim());
            }

            TransactionsManager.commit();

        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException(
                    "No se pudo rechazar la solicitud", e);

        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public void reenviarSolicitud(
            int idSolicitud) throws BLException {

        TransactionsManager.iniciar();

        try {
            SolicitudCirugia solicitud =
                    buscarSolicitud(idSolicitud);

            if (solicitud.getEstado()
                    != EstadoSolicitudCirugia.RECHAZADA
                    && solicitud.getEstado()
                    != EstadoSolicitudCirugia.DOCUMENTACION_INCOMPLETA) {

                throw new BLException(
                        "Solo se puede reenviar una solicitud rechazada o incompleta");
            }

            List<DocumentoIngreso> vigentes =
                    documentoDAO.listarVigentesPorSolicitud(idSolicitud);

            for (TipoDocumentoIngreso tipo :
                    documentoBL.listarDocumentosObligatorios(
                            tipoDe(solicitud.getCliente()))) {

                boolean presente = vigentes.stream()
                        .anyMatch(d ->
                                d.getTipoDocumento() == tipo
                                        && d.getEstadoValidacion()
                                        != EstadoValidacionDocumento.RECHAZADO);

                if (!presente) {
                    throw new BLException(
                            "Falta subsanar el documento " + tipo);
                }
            }

            solicitud.setEstado(
                    EstadoSolicitudCirugia.POR_REVISAR);

            solicitud.setFechaRespuesta(null);

            solicitudDAO.cambiarEstado(solicitud);

            TransactionsManager.commit();

        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException(
                    "No se pudo reenviar la solicitud", e);

        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public int registrarCirugiaDesdeSolicitud(
            int idSolicitud,
            Cirugia cirugia,
            String responsable) throws BLException {

        SolicitudCirugia solicitud =
                buscarSolicitud(idSolicitud);

        if (solicitud.getEstado()
                != EstadoSolicitudCirugia.APROBADA) {
            throw new BLException(
                    "Solo se registra una cirugía desde una solicitud APROBADA");
        }

        try {
            Integer existente =
                    cirugiaDAO.buscarCirugiaVigentePorSolicitud(
                            idSolicitud);

            if (existente != null) {
                throw new BLException(
                        "La solicitud ya tiene una cirugía activa (id "
                                + existente + ")");
            }

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo verificar la cirugía de origen", e);
        }

        Cirugia nueva = cirugia != null
                ? new Cirugia(cirugia)
                : new Cirugia();

        nueva.setIdCirugia(0);

        if (nueva.getFechaHoraInicio() == null) {
            nueva.setFechaHoraInicio(
                    solicitud.getFechaHoraSolicitada());
        }

        if (nueva.getTipoProcedimiento() == null
                || nueva.getTipoProcedimiento().isBlank()) {
            nueva.setTipoProcedimiento(
                    solicitud.getTipoProcedimiento());
        }

        if (nueva.getDoctorNombre() == null
                || nueva.getDoctorNombre().isBlank()) {
            nueva.setDoctorNombre(
                    solicitud.getDoctorNombre());
        }

        if (nueva.getDiagnostico() == null
                || nueva.getDiagnostico().isBlank()) {
            nueva.setDiagnostico(
                    solicitud.getDiagnostico());
        }

        Cliente solicitante = solicitud.getCliente();

        if (nueva.getCliente() != null
                && (!tipoDe(nueva.getCliente()).equals(
                tipoDe(solicitante))
                || nueva.getCliente().getIdCliente()
                != solicitante.getIdCliente())) {

            throw new BLException(
                    "El cliente de la cirugía no coincide con el de la solicitud");
        }

        nueva.setCliente(solicitante);
        nueva.setSolicitudOrigen(solicitud);
        nueva.setActivo(true);

        return new CirugiaBLImpl()
                .registrarInterno(nueva, responsable);
    }

    private SolicitudCirugia buscarSolicitud(
            int idSolicitud) throws BLException {

        SolicitudCirugia solicitud =
                findSolicitudById(idSolicitud);

        if (solicitud == null) {
            throw new BLException(
                    "No existe una solicitud con id " + idSolicitud);
        }

        return solicitud;
    }

    private String faltantes(
            SolicitudCirugia solicitud) throws BLException {

        try {
            List<DocumentoIngreso> vigentes =
                    documentoDAO.listarVigentesPorSolicitud(
                            solicitud.getIdSolicitud());

            List<String> faltan = new ArrayList<>();

            for (TipoDocumentoIngreso tipo :
                    documentoBL.listarDocumentosObligatorios(
                            tipoDe(solicitud.getCliente()))) {

                boolean ok = vigentes.stream()
                        .anyMatch(d ->
                                d.getTipoDocumento() == tipo
                                        && d.getEstadoValidacion()
                                        == EstadoValidacionDocumento.VALIDADO);

                if (!ok) {
                    faltan.add(tipo.name());
                }
            }

            return String.join(", ", faltan);

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudieron consultar los documentos", e);
        }
    }

    private String tipoDe(
            Cliente cliente) throws BLException {

        if (cliente instanceof ClinicaHospital) {
            return CirugiaBL.TIPO_CLIENTE_CLINICA;
        }

        if (cliente instanceof PacienteParticular) {
            return CirugiaBL.TIPO_CLIENTE_PARTICULAR;
        }

        throw new BLException(
                "Tipo de cliente no soportado");
    }

    private Cliente validarClienteExiste(
            Cliente cliente) throws BLException {

        try {
            Cliente encontrado =
                    cliente instanceof ClinicaHospital
                            ? clinicaDAO.findById(cliente.getIdCliente())
                            : cliente instanceof PacienteParticular
                              ? pacienteDAO.findById(cliente.getIdCliente())
                              : null;

            if (encontrado == null) {
                throw new BLException(
                        "El cliente no existe (id "
                                + cliente.getIdCliente() + ")");
            }

            if (!encontrado.isActivo()) {
                throw new BLException(
                        "El cliente está inactivo");
            }

            return encontrado;

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo verificar el cliente", e);
        }
    }

    private void validarVendedorExiste(Vendedor vendedor) throws BLException {
        if (vendedor == null || vendedor.getIdUsuario() <= 0) {
            throw new BLException("El vendedor registrador no es válido");
        }
        try {
            Vendedor encontrado = vendedorDAO.findById(vendedor.getIdUsuario());
            if (encontrado == null || !encontrado.isActivo()) {
                throw new BLException("El vendedor registrador no existe o está inactivo");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar el vendedor registrador", e);
        }
    }

    private void textoObligatorio(
            String valor,
            String nombre,
            int max) throws BLException {

        if (valor == null || valor.isBlank()) {
            throw new BLException(
                    nombre + " es obligatorio");
        }

        if (valor.trim().length() > max) {
            throw new BLException(
                    nombre + " supera " + max + " caracteres");
        }
    }
}
