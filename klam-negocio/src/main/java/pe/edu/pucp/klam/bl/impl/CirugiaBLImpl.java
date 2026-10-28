package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.DocumentoIngresoBL;
import pe.edu.pucp.klam.bl.NotificacionBL;
import pe.edu.pucp.klam.dao.BandejaInstrumentalDAO;
import pe.edu.pucp.klam.dao.CirugiaDAO;
import pe.edu.pucp.klam.dao.ClinicaHospitalDAO;
import pe.edu.pucp.klam.dao.CotizacionDAO;
import pe.edu.pucp.klam.dao.DocumentoIngresoDAO;
import pe.edu.pucp.klam.dao.EquipoDAO;
import pe.edu.pucp.klam.dao.PacienteParticularDAO;
import pe.edu.pucp.klam.dao.impl.CirugiaDAOImpl;
import pe.edu.pucp.klam.dao.impl.ClinicaHospitalDAOImpl;
import pe.edu.pucp.klam.dao.impl.CotizacionDAOImpl;
import pe.edu.pucp.klam.dao.impl.DocumentoIngresoDAOImpl;
import pe.edu.pucp.klam.dao.impl.PacienteParticularDAOImpl;
import pe.edu.pucp.klam.dao.impl.inventario.BandejaInstrumentalDAOImpl;
import pe.edu.pucp.klam.dao.impl.inventario.EquipoDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Cotizacion;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.TipoDocumentoIngreso;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CirugiaBLImpl implements CirugiaBL {
    private static final Logger LOG = Logger.getLogger(CirugiaBLImpl.class.getName());

    // Longitudes maximas: las mismas que el frontend Blazor y el DDL
    private static final int MAX_PROCEDIMIENTO = 100;
    private static final int MAX_DOCTOR = 100;
    private static final int MAX_DIAGNOSTICO = 300;
    private static final int MAX_MOTIVO = 200;
    private static final int MAX_PACIENTE = 150;
    private static final int MAX_OBSERVACIONES = 500;

    private final CirugiaDAO cirugiaDAO = new CirugiaDAOImpl();
    private final EquipoDAO equipoDAO = new EquipoDAOImpl();
    private final BandejaInstrumentalDAO bandejaDAO = new BandejaInstrumentalDAOImpl();
    private final ClinicaHospitalDAO clinicaHospitalDAO = new ClinicaHospitalDAOImpl();
    private final PacienteParticularDAO pacienteParticularDAO = new PacienteParticularDAOImpl();
    private final CotizacionDAO cotizacionDAO = new CotizacionDAOImpl();
    private final DocumentoIngresoDAO documentoDAO = new DocumentoIngresoDAOImpl();
    // La regla de documentos obligatorios vive en DocumentoIngresoBL; aqui solo se reutiliza
    private final DocumentoIngresoBL documentoBL = new DocumentoIngresoBLImpl();
    private final NotificacionBL notificacionBL = new NotificacionBLImpl();

    // ------------------------------------------------------------------
    // Consultas basicas
    // ------------------------------------------------------------------
    @Override
    public List<Cirugia> findAll() throws BLException {
        try {
            return cirugiaDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las cirugías", e);
        }
    }

    @Override
    public Cirugia findById(Integer id) throws BLException {
        try {
            return cirugiaDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la cirugía", e);
        }
    }

    @Override
    public List<Cirugia> findByEstado(EstadoCirugia estado) throws BLException {
        if (estado == null) {
            throw new BLException("Debe indicar el estado de las cirugías a buscar");
        }
        return buscar(null, null, null, null, null, estado);
    }

    @Override
    public List<Cirugia> findByRangoFechas(LocalDateTime desde, LocalDateTime hasta) throws BLException {
        if (desde == null || hasta == null) {
            throw new BLException("Debe indicar el inicio y el fin del rango de fechas");
        }
        return buscar(desde, hasta, null, null, null, null);
    }

    // ------------------------------------------------------------------
    // Filtros de calendario, lista e historial (RF06, RF10, RF17)
    // ------------------------------------------------------------------
    @Override
    public List<Cirugia> buscar(LocalDateTime desde, LocalDateTime hasta, String tipoCliente, Integer idCliente,
                                Integer idEquipo, EstadoCirugia estado) throws BLException {
        validarFiltros(desde, hasta, tipoCliente, idCliente);
        try {
            return cirugiaDAO.buscar(desde, hasta, tipoCliente, idCliente, idEquipo, estado);
        } catch (SQLException e) {
            throw new BLException("No se pudo buscar las cirugías", e);
        }
    }

    @Override
    public List<Map<String, Object>> buscarDetalle(LocalDateTime desde, LocalDateTime hasta, String tipoCliente,
                                                   Integer idCliente, Integer idEquipo,
                                                   EstadoCirugia estado) throws BLException {
        validarFiltros(desde, hasta, tipoCliente, idCliente);
        try {
            List<Map<String, Object>> filas = cirugiaDAO.buscarDetalle(desde, hasta, tipoCliente, idCliente, idEquipo, estado);
            for (Map<String, Object> fila : filas) {
                fila.put("estadoTexto", EstadoCirugia.valueOf((String) fila.get("estado")).getTexto());
            }
            return filas;
        } catch (SQLException e) {
            throw new BLException("No se pudo buscar las cirugías", e);
        }
    }

    @Override
    public List<Cirugia> listarPorTipoCliente(String tipoCliente) throws BLException {
        if (tipoCliente == null) {
            throw new BLException("Debe indicar el tipo de cliente");
        }
        return buscar(null, null, tipoCliente, null, null, null);
    }

    @Override
    public List<Cirugia> listarPorCliente(Cliente cliente) throws BLException {
        return buscar(null, null, tipoDe(cliente), cliente.getIdCliente(), null, null);
    }

    @Override
    public List<Cirugia> listarPorEquipo(int idEquipo) throws BLException {
        if (idEquipo <= 0) {
            throw new BLException("El id de equipo no es válido");
        }
        return buscar(null, null, null, null, idEquipo, null);
    }

    @Override
    public List<Map<String, Object>> historialPorCliente(Cliente cliente) throws BLException {
        return buscarDetalle(null, null, tipoDe(cliente), cliente.getIdCliente(), null, null);
    }

    @Override
    public List<Map<String, Object>> buscarCalendarioMensual(int anio, int mes) throws BLException {
        if (mes < 1 || mes > 12) {
            throw new BLException("El mes debe estar entre 1 y 12");
        }
        YearMonth ym = YearMonth.of(anio, mes);
        return buscarDetalle(ym.atDay(1).atStartOfDay(), ym.atEndOfMonth().atTime(23, 59, 59), null, null, null, null);
    }

    private void validarFiltros(LocalDateTime desde, LocalDateTime hasta, String tipoCliente,
                                Integer idCliente) throws BLException {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BLException("El inicio del rango no puede ser posterior al fin");
        }
        if (tipoCliente != null && !TIPO_CLIENTE_CLINICA.equals(tipoCliente) && !TIPO_CLIENTE_PARTICULAR.equals(tipoCliente)) {
            throw new BLException("Tipo de cliente no soportado: " + tipoCliente);
        }
        if (idCliente != null && tipoCliente == null) {
            throw new BLException("Para filtrar por cliente indique también su tipo (los ids de clínicas y pacientes se solapan)");
        }
    }

    private String tipoDe(Cliente cliente) throws BLException {
        if (cliente == null) {
            throw new BLException("Debe indicar el cliente");
        }
        if (cliente instanceof ClinicaHospital) {
            return TIPO_CLIENTE_CLINICA;
        }
        if (cliente instanceof PacienteParticular) {
            return TIPO_CLIENTE_PARTICULAR;
        }
        throw new BLException("Tipo de cliente no soportado");
    }

    // ------------------------------------------------------------------
    // Detalle de una cirugia
    // ------------------------------------------------------------------
    @Override
    public Map<String, Object> obtenerDetalle(int idCirugia) throws BLException {
        Cirugia c = buscarCirugia(idCirugia);

        Map<String, Object> d = new LinkedHashMap<>();
        d.put("idCirugia", c.getIdCirugia());
        d.put("estado", c.getEstado().name());
        d.put("estadoTexto", c.getEstado().getTexto());
        d.put("fechaHoraInicio", c.getFechaHoraInicio());
        d.put("fechaHoraFin", c.getFechaHoraFin());
        d.put("tipoProcedimiento", c.getTipoProcedimiento());
        d.put("doctorNombre", c.getDoctorNombre());
        d.put("diagnostico", c.getDiagnostico());
        d.put("motivoCancelacion", c.getMotivoCancelacion());
        d.put("idSolicitudOrigen", c.getIdSolicitudOrigen());
        d.put("consumoCompleto", c.isConsumoCompleto());
        d.put("tipoCliente", tipoDe(c.getCliente()));
        d.put("idCliente", c.getCliente().getIdCliente());
        d.put("clienteNombre", c.getCliente().getNombre());
        d.put("idEquipo", c.getEquipo() != null ? c.getEquipo().getIdEquipo() : null);
        d.put("equipoNombre", c.getEquipo() != null ? c.getEquipo().getNombre() : null);
        d.put("idBandeja", c.getBandejaInstrumental() != null ? c.getBandejaInstrumental().getIdBandeja() : null);
        d.put("bandejaTipo", c.getBandejaInstrumental() != null ? c.getBandejaInstrumental().getTipo() : null);

        try {
            // paciente y clinica salen de la solicitud de origen; sin solicitud, de la cirugia
            String pacienteNombre = null;
            String clinicaNombre = c.getCliente() instanceof ClinicaHospital ? c.getCliente().getNombre() : null;
            if (c.getIdSolicitudOrigen() != null) {
                List<Map<String, Object>> sol = cirugiaDAO.buscarSolicitudes(c.getIdSolicitudOrigen(), null, null, null);
                if (!sol.isEmpty()) {
                    pacienteNombre = (String) sol.get(0).get("pacienteNombre");
                    clinicaNombre = (String) sol.get(0).get("clinicaNombre");
                }
            } else if (c.getCliente() instanceof PacienteParticular) {
                pacienteNombre = c.getCliente().getNombre();
            }
            d.put("pacienteNombre", pacienteNombre);
            d.put("clinicaNombre", clinicaNombre);

            // estado documental: version vigente de cada documento de la solicitud y de la cirugia
            List<Map<String, Object>> documentos = new ArrayList<>();
            List<DocumentoIngreso> vigentes = new ArrayList<>();
            if (c.getIdSolicitudOrigen() != null) {
                vigentes.addAll(documentoDAO.listarVigentesPorSolicitud(c.getIdSolicitudOrigen()));
            }
            vigentes.addAll(documentoDAO.listarVigentesPorCirugia(idCirugia));
            for (DocumentoIngreso doc : vigentes) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("idDocumento", doc.getIdDocumento());
                fila.put("documento", doc.getTipoDocumento().name());
                fila.put("version", doc.getVersion());
                fila.put("estado", doc.getEstadoValidacion().name());
                fila.put("referencia", doc.getArchivoPath());
                documentos.add(fila);
            }
            d.put("documentos", documentos);
            d.put("documentacionCierreCompleta", documentoBL.verificarDocumentacionCierre(idCirugia));

            // despacho y retorno
            List<Map<String, Object>> despachos = equipoDAO.buscarDespachos(null, null, idCirugia, false);
            d.put("despacho", despachos.isEmpty() ? null : despachos.get(0));

            // cotizacion
            List<Cotizacion> cotizaciones = cotizacionDAO.findByCirugia(idCirugia);
            d.put("precioPactado", cotizaciones.isEmpty() ? null : cotizaciones.get(0).getPrecioPactado());
            d.put("estadoCotizacion", cotizaciones.isEmpty() ? null : cotizaciones.get(0).getEstado().name());

            // consumibles: solo corresponden al flujo de cierre (cirugia FINALIZADA)
            d.put("consumibles", c.getEstado() == EstadoCirugia.FINALIZADA
                    ? bandejaDAO.buscarConsumos(idCirugia, null, null, null, null)
                    : null);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el detalle de la cirugía " + idCirugia, e);
        }
        return d;
    }

    // ------------------------------------------------------------------
    // CRUD con auditoria (RF02, RF09, RF16)
    // ------------------------------------------------------------------
    @Override
    public void insert(Cirugia cirugia) throws BLException {
        registrar(cirugia, RESPONSABLE_SISTEMA);
    }

    @Override
    public void update(Cirugia cirugia) throws BLException {
        modificar(cirugia, RESPONSABLE_SISTEMA);
    }

    @Override
    public void cancelar(Integer id, String motivo) throws BLException {
        cancelar(id, motivo, RESPONSABLE_SISTEMA);
    }

    @Override
    public void delete(Integer id) throws BLException {
        eliminar(id, RESPONSABLE_SISTEMA);
    }

    @Override
    public void registrar(Cirugia cirugia, String responsable) throws BLException {
        if (cirugia != null && cirugia.getIdSolicitudOrigen() != null) {
            throw new BLException("Para registrar una cirugía desde una solicitud use registrarCirugiaDesdeSolicitud");
        }
        registrarInterno(cirugia, responsable);
    }

    // Registro comun (directo o desde una solicitud): cirugia + auditoria + notificacion en una transaccion
    private int registrarInterno(Cirugia cirugia, String responsable) throws BLException {
        validarDatos(cirugia, null);
        if (cirugia.getEstado() == EstadoCirugia.CANCELADA) {
            cirugia.setBandejaInstrumental(null); // una cirugia cancelada no retiene la bandeja fisica
        }
        String quien = textoResponsable(responsable);

        TransactionsManager.iniciar();
        try {
            cirugiaDAO.insert(cirugia);
            int id = cirugia.getIdCirugia();

            Map<String, String> despues = snapshot(cirugia);
            auditar(id, ACCION_CREAR, quien,
                    cirugia.getIdSolicitudOrigen() != null
                            ? "Cirugía registrada desde la solicitud " + codigoSolicitud(cirugia.getIdSolicitudOrigen())
                            : "Cirugía registrada directamente por el administrador",
                    null, despues);
            if (cirugia.getEquipo() != null) {
                auditar(id, ACCION_ASIGNAR_EQUIPO, quien, "Equipo asignado: " + cirugia.getEquipo().getNombre(),
                        null, Map.of("idEquipo", valor(cirugia.getEquipo().getIdEquipo())));
            }
            if (cirugia.getBandejaInstrumental() != null) {
                auditar(id, ACCION_ASIGNAR_BANDEJA, quien, "Bandeja asignada: " + cirugia.getBandejaInstrumental().getIdBandeja(),
                        null, Map.of("idBandeja", valor(cirugia.getBandejaInstrumental().getIdBandeja())));
            }

            // RF12: notificacion interna para el instrumentista (en la misma transaccion)
            notificacionBL.notificarTecnicos("Nueva cirugía programada #" + id, mensajeCirugia(cirugia));

            TransactionsManager.commit();
            return id;
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo registrar la cirugía: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public void modificar(Cirugia cirugia, String responsable) throws BLException {
        if (cirugia == null) {
            throw new BLException("La cirugía no puede ser nula");
        }
        Cirugia existente = buscarCirugia(cirugia.getIdCirugia());
        if (!existente.isActivo()) {
            throw new BLException("La cirugía " + cirugia.getIdCirugia() + " está dada de baja");
        }
        if (existente.getEstado() == EstadoCirugia.CANCELADA) {
            throw new BLException("No se puede modificar una cirugía cancelada");
        }
        validarDatos(cirugia, existente);

        // lo que no se edita desde la pantalla se conserva
        cirugia.setIdSolicitudOrigen(existente.getIdSolicitudOrigen());
        cirugia.setConsumoCompleto(existente.isConsumoCompleto());
        cirugia.setActivo(true);
        if (cirugia.getEstado() == EstadoCirugia.CANCELADA) {
            cirugia.setBandejaInstrumental(null); // cancelar libera la bandeja fisica
        }

        Integer idBandejaAntes = idBandeja(existente);
        Integer idBandejaDespues = idBandeja(cirugia);
        if (idBandejaAntes != null && !Objects.equals(idBandejaAntes, idBandejaDespues)) {
            try {
                if (bandejaDAO.tieneConsumoRegistrado(idBandejaAntes)) {
                    throw new BLException("No se puede cambiar la bandeja: ya tiene consumo registrado");
                }
            } catch (SQLException e) {
                throw new BLException("No se pudo verificar el consumo de la bandeja " + idBandejaAntes, e);
            }
        }

        String quien = textoResponsable(responsable);
        TransactionsManager.iniciar();
        try {
            cirugiaDAO.update(cirugia);
            auditarCambios(existente, cirugia, quien);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo actualizar la cirugía: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    // Una entrada de auditoria por tipo de cambio, dentro de la transaccion de la modificacion
    private void auditarCambios(Cirugia antes, Cirugia despues, String quien) throws SQLException {
        int id = despues.getIdCirugia();
        Map<String, String> a = snapshot(antes);
        Map<String, String> d = snapshot(despues);

        // equipo y bandeja se auditan como asignaciones; el estado, como cambio de estado o cancelacion
        Set<String> propios = Set.of("idEquipo", "idBandeja", "estado", "motivoCancelacion", "fechaHoraFin");
        boolean cambioEstado = antes.getEstado() != despues.getEstado();
        Map<String, String> restoAntes = new LinkedHashMap<>();
        Map<String, String> restoDespues = new LinkedHashMap<>();
        diferencias(a, d, cambioEstado ? propios : Set.of("idEquipo", "idBandeja", "estado"), restoAntes, restoDespues);
        if (!restoAntes.isEmpty() || !restoDespues.isEmpty()) {
            auditar(id, ACCION_EDITAR, quien, "Datos de la cirugía modificados: " + String.join(", ", restoDespues.keySet()),
                    restoAntes, restoDespues);
        }

        if (cambioEstado) {
            Map<String, String> estAntes = new LinkedHashMap<>();
            Map<String, String> estDespues = new LinkedHashMap<>();
            diferencias(a, d, Set.of("tipoProcedimiento", "doctorNombre", "diagnostico", "fechaHoraInicio",
                    "tipoCliente", "idCliente", "activo", "idEquipo", "idBandeja"), estAntes, estDespues);
            boolean cancelada = despues.getEstado() == EstadoCirugia.CANCELADA;
            auditar(id, cancelada ? ACCION_CANCELAR : ACCION_CAMBIAR_ESTADO, quien,
                    "Estado: " + antes.getEstado().getTexto() + " -> " + despues.getEstado().getTexto(),
                    estAntes, estDespues);
        }

        if (!Objects.equals(a.get("idEquipo"), d.get("idEquipo"))) {
            boolean asigna = antes.getEquipo() == null;
            auditar(id, asigna ? ACCION_ASIGNAR_EQUIPO : ACCION_CAMBIAR_EQUIPO, quien,
                    asigna ? "Equipo asignado" : "Equipo cambiado",
                    Map.of("idEquipo", a.get("idEquipo")), Map.of("idEquipo", d.get("idEquipo")));
        }
        if (!Objects.equals(a.get("idBandeja"), d.get("idBandeja"))) {
            boolean asigna = antes.getBandejaInstrumental() == null;
            auditar(id, asigna ? ACCION_ASIGNAR_BANDEJA : ACCION_CAMBIAR_BANDEJA, quien,
                    asigna ? "Bandeja asignada" : "Bandeja cambiada",
                    Map.of("idBandeja", a.get("idBandeja")), Map.of("idBandeja", d.get("idBandeja")));
        }
    }

    @Override
    public void cancelar(Integer id, String motivo, String responsable) throws BLException {
        if (motivo == null || motivo.isBlank()) {
            throw new BLException("Debe indicar el motivo de la cancelación");
        }
        if (motivo.trim().length() > MAX_MOTIVO) {
            throw new BLException("El motivo de cancelación no puede superar los " + MAX_MOTIVO + " caracteres");
        }

        Cirugia cirugia = buscarCirugia(id);
        if (!cirugia.isActivo()) {
            throw new BLException("La cirugía " + id + " está dada de baja");
        }
        if (cirugia.getEstado() == EstadoCirugia.CANCELADA
                || cirugia.getEstado() == EstadoCirugia.FINALIZADA) {
            throw new BLException(
                    "No se puede cancelar una cirugía en estado " + cirugia.getEstado().getTexto());
        }

        Map<String, String> antes = snapshot(cirugia);
        String quien = textoResponsable(responsable);
        TransactionsManager.iniciar();
        try {
            cirugiaDAO.cancelar(id, motivo.trim());   // tambien libera la bandeja fisica

            Map<String, String> despues = new LinkedHashMap<>(antes);
            despues.put("estado", EstadoCirugia.CANCELADA.name());
            despues.put("motivoCancelacion", motivo.trim());
            despues.put("idBandeja", "");
            Map<String, String> a = new LinkedHashMap<>();
            Map<String, String> d = new LinkedHashMap<>();
            diferencias(antes, despues, Set.of(), a, d);
            auditar(id, ACCION_CANCELAR, quien, "Cirugía cancelada: " + motivo.trim(), a, d);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo cancelar la cirugía: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public void eliminar(Integer id, String responsable) throws BLException {
        Cirugia cirugia = buscarCirugia(id);
        if (!cirugia.isActivo()) {
            throw new BLException("La cirugía " + id + " ya está dada de baja");
        }

        String quien = textoResponsable(responsable);
        Map<String, String> antes = new LinkedHashMap<>();
        antes.put("activo", "true");
        antes.put("idBandeja", valor(idBandeja(cirugia)));
        Map<String, String> despues = new LinkedHashMap<>();
        despues.put("activo", "false");
        despues.put("idBandeja", "");

        TransactionsManager.iniciar();
        try {
            cirugiaDAO.delete(id);   // baja logica; libera la bandeja fisica
            auditar(id, ACCION_ELIMINAR, quien, "Eliminación lógica de la cirugía", antes, despues);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo eliminar la cirugía: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    @Override
    public void cambiarEstado(Integer id, EstadoCirugia nuevoEstado, String responsable) throws BLException {
        if (nuevoEstado == null) {
            throw new BLException("Debe indicar el nuevo estado");
        }
        if (nuevoEstado == EstadoCirugia.CANCELADA) {
            throw new BLException("Para cancelar una cirugía use cancelar, que exige un motivo");
        }
        Cirugia cirugia = buscarCirugia(id);
        if (!cirugia.isActivo()) {
            throw new BLException("La cirugía " + id + " está dada de baja");
        }

        EstadoCirugia actual = cirugia.getEstado();
        boolean permitido = (actual == EstadoCirugia.PROGRAMADA && nuevoEstado == EstadoCirugia.EN_PROCESO)
                || (actual == EstadoCirugia.EN_PROCESO && nuevoEstado == EstadoCirugia.FINALIZADA);
        if (!permitido) {
            throw new BLException("No se puede pasar de " + actual.getTexto() + " a " + nuevoEstado.getTexto());
        }

        LocalDateTime fin = null;
        if (nuevoEstado == EstadoCirugia.FINALIZADA && cirugia.getFechaHoraFin() == null) {
            fin = LocalDateTime.now();
            if (!fin.isAfter(cirugia.getFechaHoraInicio())) {
                throw new BLException("La cirugía aún no comenzó: indique una fecha de fin posterior al inicio");
            }
        }

        Map<String, String> antes = new LinkedHashMap<>();
        antes.put("estado", actual.name());
        Map<String, String> despues = new LinkedHashMap<>();
        despues.put("estado", nuevoEstado.name());
        if (fin != null) {
            antes.put("fechaHoraFin", valor(cirugia.getFechaHoraFin()));
            despues.put("fechaHoraFin", valor(fin));
        }

        String quien = textoResponsable(responsable);
        final LocalDateTime finFinal = fin;
        TransactionsManager.iniciar();
        try {
            cirugiaDAO.cambiarEstado(id, nuevoEstado, finFinal);
            auditar(id, ACCION_CAMBIAR_ESTADO, quien,
                    "Estado: " + actual.getTexto() + " -> " + nuevoEstado.getTexto(), antes, despues);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo cambiar el estado de la cirugía: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }

    // ------------------------------------------------------------------
    // Validaciones de la cirugia
    // existente = cirugia guardada (en una edicion) o null (en un registro)
    // ------------------------------------------------------------------
    private void validarDatos(Cirugia cirugia, Cirugia existente) throws BLException {
        if (cirugia == null) {
            throw new BLException("La cirugía no puede ser nula");
        }
        Integer idExcluir = existente != null ? existente.getIdCirugia() : null;

        textoObligatorio(cirugia.getTipoProcedimiento(), "El tipo de procedimiento", MAX_PROCEDIMIENTO);
        textoObligatorio(cirugia.getDoctorNombre(), "El nombre del doctor", MAX_DOCTOR);
        textoObligatorio(cirugia.getDiagnostico(), "El diagnóstico", MAX_DIAGNOSTICO);
        if (cirugia.getMotivoCancelacion() != null && cirugia.getMotivoCancelacion().trim().length() > MAX_MOTIVO) {
            throw new BLException("El motivo de cancelación no puede superar los " + MAX_MOTIVO + " caracteres");
        }

        LocalDateTime inicio = cirugia.getFechaHoraInicio();
        LocalDateTime fin = cirugia.getFechaHoraFin();
        if (inicio == null) {
            throw new BLException("La fecha y hora de inicio es obligatoria");
        }
        if (fin != null && !fin.isAfter(inicio)) {
            throw new BLException("La fecha y hora de fin debe ser posterior a la de inicio");
        }

        if (cirugia.getEstado() == EstadoCirugia.FINALIZADA && fin == null) {
            throw new BLException("Una cirugía finalizada debe tener fecha y hora de fin");
        }
        if (cirugia.getEstado() == EstadoCirugia.CANCELADA
                && (cirugia.getMotivoCancelacion() == null || cirugia.getMotivoCancelacion().isBlank())) {
            throw new BLException("Una cirugía cancelada debe tener un motivo de cancelación");
        }

        if (cirugia.getCliente() == null) {
            throw new BLException("La cirugía debe tener un cliente");
        }
        validarClienteExiste(cirugia.getCliente());

        // Los recursos solo deben estar operativos mientras la cirugia este vigente
        boolean vigente = cirugia.getEstado() == EstadoCirugia.PROGRAMADA
                || cirugia.getEstado() == EstadoCirugia.EN_PROCESO;

        if (cirugia.getEquipo() != null) {
            Equipo equipo = buscarEquipo(cirugia.getEquipo().getIdEquipo());
            boolean equipoCambio = existente == null || existente.getEquipo() == null
                    || existente.getEquipo().getIdEquipo() != equipo.getIdEquipo();
            // Si el equipo ya era de esta cirugia, que este "no disponible" (despachado) es normal
            if (vigente && equipoCambio && !equipo.verificar()) {
                throw new BLException("El equipo " + equipo.getNombre() + " no está disponible");
            }
            if (vigente && !equipo.isActivo()) {
                throw new BLException("El equipo " + equipo.getNombre() + " está dado de baja");
            }
            if (vigente) {
                try {
                    if (cirugiaDAO.existeCruceEquipo(equipo.getIdEquipo(), inicio, fin, idExcluir)) {
                        throw new BLException("El equipo " + equipo.getNombre()
                                + " ya está asignado a otra cirugía en ese horario");
                    }
                } catch (SQLException e) {
                    throw new BLException("No se pudo verificar el cruce de horarios del equipo", e);
                }
            }
        }

        if (cirugia.getBandejaInstrumental() != null && cirugia.getEstado() != EstadoCirugia.CANCELADA) {
            BandejaInstrumental bandeja = buscarBandeja(cirugia.getBandejaInstrumental().getIdBandeja());
            boolean bandejaCambio = existente == null || existente.getBandejaInstrumental() == null
                    || existente.getBandejaInstrumental().getIdBandeja() != bandeja.getIdBandeja();
            if (vigente && bandejaCambio) {
                if (!bandeja.isActivo()) {
                    throw new BLException("La bandeja " + bandeja.getIdBandeja() + " está dada de baja");
                }
                if (!bandeja.verificar()) {
                    throw new BLException("La bandeja " + bandeja.getTipo() + " no está esterilizada");
                }
            }
            // Bandeja fisica: no puede pertenecer a otra cirugia (la propia se excluye)
            try {
                if (cirugiaDAO.bandejaAsignadaAOtraCirugia(bandeja.getIdBandeja(), idExcluir)) {
                    throw new BLException("La bandeja " + bandeja.getIdBandeja()
                            + " ya está asignada a otra cirugía");
                }
            } catch (SQLException e) {
                throw new BLException("No se pudo verificar la asignación de la bandeja", e);
            }
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

    private Cirugia buscarCirugia(Integer id) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("El id de la cirugía no es válido");
        }
        try {
            Cirugia cirugia = cirugiaDAO.findById(id);
            if (cirugia == null) {
                throw new BLException("No existe una cirugía con id " + id);
            }
            return cirugia;
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de la cirugía", e);
        }
    }

    private void validarClienteExiste(Cliente cliente) throws BLException {
        try {
            Cliente encontrado;
            if (cliente instanceof ClinicaHospital) {
                encontrado = clinicaHospitalDAO.findById(cliente.getIdCliente());
            } else if (cliente instanceof PacienteParticular) {
                encontrado = pacienteParticularDAO.findById(cliente.getIdCliente());
            } else {
                throw new BLException("Tipo de cliente no soportado");
            }

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

    private Equipo buscarEquipo(int idEquipo) throws BLException {
        try {
            Equipo equipo = equipoDAO.findById(idEquipo);
            if (equipo == null) {
                throw new BLException("No existe un equipo con id " + idEquipo);
            }
            return equipo;
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el equipo de la cirugía", e);
        }
    }

    private BandejaInstrumental buscarBandeja(int idBandeja) throws BLException {
        try {
            BandejaInstrumental bandeja = bandejaDAO.findById(idBandeja);
            if (bandeja == null) {
                throw new BLException("No existe una bandeja instrumental con id " + idBandeja);
            }
            return bandeja;
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la bandeja de la cirugía", e);
        }
    }

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
            resumen = "Solicitud " + codigoSolicitud(idSolicitud)
                    + " · Cliente: " + cliente.getNombre()
                    + " · Procedimiento: " + datos.getTipoProcedimiento().trim()
                    + " · Fecha solicitada: " + datos.getFechaHoraInicio();
            notificacionBL.notificarAdministradores("Nueva solicitud de cirugía " + codigoSolicitud(idSolicitud), resumen);
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo registrar la solicitud de cirugía: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }

        enviarCorreoSolicitud(idSolicitud, resumen);
        return idSolicitud;
    }

    // El correo se envia DESPUES de confirmar la solicitud: si falla, la solicitud sigue registrada
    // y el fallo queda reportado en una notificacion de alerta para los administradores y en el log.
    private void enviarCorreoSolicitud(int idSolicitud, String resumen) {
        try {
            notificacionBL.enviarCorreoAdministradores("Nueva solicitud de cirugía " + codigoSolicitud(idSolicitud), resumen);
        } catch (BLException e) {
            LOG.log(Level.WARNING, "Fallo el correo de la solicitud " + idSolicitud, e);
            try {
                notificacionBL.notificarAdministradores("Error de envío de correo",
                        "No se pudo enviar el correo de la solicitud " + codigoSolicitud(idSolicitud) + ": " + e.getMessage());
            } catch (BLException ex) {
                LOG.log(Level.SEVERE, "No se pudo reportar el fallo de correo de la solicitud " + idSolicitud, ex);
            }
        }
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
            List<Map<String, Object>> filas = cirugiaDAO.buscarSolicitudes(id, estado, tipoCliente, idCliente);
            for (Map<String, Object> fila : filas) {
                fila.put("codigo", codigoSolicitud(((Number) fila.get("idSolicitud")).intValue()));
                fila.put("estadoTexto", textoEstadoSolicitud((String) fila.get("estadoSolicitud")));
            }
            return filas;
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
    public void aprobarSolicitud(int idSolicitud, String responsable) throws BLException {
        Map<String, Object> solicitud = buscarSolicitud(idSolicitud);
        String estado = (String) solicitud.get("estadoSolicitud");
        if (!SOLICITUD_POR_REVISAR.equals(estado)) {
            throw new BLException("Solo se puede aprobar una solicitud POR_REVISAR (estado actual: "
                    + textoEstadoSolicitud(estado) + ")");
        }

        // La comprobacion y el cambio de estado van juntos: no se aprueba con documentos faltantes
        TransactionsManager.iniciar();
        try {
            if (!documentoBL.verificarDocumentosObligatoriosValidados(idSolicitud)) {
                throw new BLException("No se puede aprobar la solicitud " + codigoSolicitud(idSolicitud)
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

    // Lista los tipos obligatorios que aun no tienen version vigente VALIDADA
    private String faltantes(int idSolicitud, String tipoCliente) throws BLException {
        try {
            List<DocumentoIngreso> vigentes = documentoDAO.listarVigentesPorSolicitud(idSolicitud);
            List<String> faltan = new ArrayList<>();
            for (TipoDocumentoIngreso tipo : documentoBL.listarDocumentosObligatorios(tipoCliente)) {
                boolean ok = vigentes.stream().anyMatch(d -> d.getTipoDocumento() == tipo && d.validar());
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
    public void rechazarSolicitud(int idSolicitud, String motivo, String responsable) throws BLException {
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
                    + textoEstadoSolicitud(estado) + ")");
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
                    + "(estado actual: " + textoEstadoSolicitud(estado) + ")");
        }

        // No se reenvia con documentos obligatorios ausentes o todavia rechazados
        try {
            List<DocumentoIngreso> vigentes = documentoDAO.listarVigentesPorSolicitud(idSolicitud);
            for (TipoDocumentoIngreso tipo : documentoBL.listarDocumentosObligatorios((String) solicitud.get("tipoCliente"))) {
                boolean presente = vigentes.stream().anyMatch(d -> d.getTipoDocumento() == tipo
                        && d.getEstadoValidacion() != pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.EstadoValidacionDocumento.RECHAZADO);
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
                    + textoEstadoSolicitud(estado) + ")");
        }
        try {
            Integer existente = cirugiaDAO.buscarCirugiaVigentePorSolicitud(idSolicitud);
            if (existente != null) {
                throw new BLException("La solicitud " + codigoSolicitud(idSolicitud)
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

        return registrarInterno(nueva, responsable);
    }

    private Cliente clienteDeSolicitud(Map<String, Object> solicitud) throws BLException {
        int idCliente = ((Number) solicitud.get("idCliente")).intValue();
        try {
            Cliente cliente = TIPO_CLIENTE_CLINICA.equals(solicitud.get("tipoCliente"))
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

    // Texto que muestra la pantalla Blazor para cada estado interno
    private String textoEstadoSolicitud(String estado) {
        if (SOLICITUD_POR_REVISAR.equals(estado)) {
            return "Por revisar";
        }
        if (SOLICITUD_DOCUMENTACION_INCOMPLETA.equals(estado)) {
            return "Documentación incompleta";
        }
        if (SOLICITUD_RECHAZADA.equals(estado)) {
            return "Rechazada · Resubida habilitada";
        }
        if (SOLICITUD_APROBADA.equals(estado)) {
            return "Aprobada";
        }
        return estado;
    }

    private String codigoSolicitud(int idSolicitud) {
        return String.format("SOL-%04d", idSolicitud);
    }

    // ------------------------------------------------------------------
    // Bitacora de auditoria (RF16)
    // ------------------------------------------------------------------
    @Override
    public List<Map<String, Object>> listarBitacora() throws BLException {
        return buscarBitacora(null, null, null, null, null);
    }

    @Override
    public List<Map<String, Object>> listarBitacoraPorCirugia(int idCirugia) throws BLException {
        if (idCirugia <= 0) {
            throw new BLException("El id de la cirugía no es válido");
        }
        return buscarBitacora(idCirugia, null, null, null, null);
    }

    @Override
    public List<Map<String, Object>> buscarBitacora(Integer idCirugia, LocalDateTime desde, LocalDateTime hasta,
                                                    String accion, String responsable) throws BLException {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BLException("El inicio del rango no puede ser posterior al fin");
        }
        try {
            List<Map<String, Object>> filas = new ArrayList<>();
            for (Map<String, Object> f : cirugiaDAO.buscarAuditoria(null, idCirugia, desde, hasta, accion, responsable)) {
                filas.add(aFilaBitacora(f, false));
            }
            return filas;
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar la bitácora de auditoría", e);
        }
    }

    @Override
    public Map<String, Object> obtenerEntradaBitacora(int idAuditoria) throws BLException {
        if (idAuditoria <= 0) {
            throw new BLException("El id de la entrada de bitácora no es válido");
        }
        try {
            List<Map<String, Object>> filas = cirugiaDAO.buscarAuditoria(idAuditoria, null, null, null, null, null);
            return filas.isEmpty() ? null : aFilaBitacora(filas.get(0), true);
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar la entrada de bitácora " + idAuditoria, e);
        }
    }

    @Override
    public List<Map<String, Object>> obtenerDetalleBitacora(int idAuditoria) throws BLException {
        Map<String, Object> entrada = obtenerEntradaBitacora(idAuditoria);
        if (entrada == null) {
            throw new BLException("No existe la entrada de bitácora " + idAuditoria);
        }
        Map<String, String> antes = parsear((String) entrada.get("valoresAnteriores"));
        Map<String, String> despues = parsear((String) entrada.get("valoresPosteriores"));

        Set<String> campos = new LinkedHashSet<>(despues.keySet());
        campos.addAll(antes.keySet());
        List<Map<String, Object>> detalle = new ArrayList<>();
        for (String campo : campos) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("campo", campo);
            fila.put("valorAnterior", antes.get(campo));
            fila.put("valorPosterior", despues.get(campo));
            detalle.add(fila);
        }
        return detalle;
    }

    // Claves de la vista Blazor: Id, FechaHora, Cirugia, Accion, Responsable, Detalle
    private Map<String, Object> aFilaBitacora(Map<String, Object> f, boolean conValores) {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("id", f.get("idAuditoria"));
        fila.put("fechaHora", f.get("fechaHora"));
        fila.put("cirugia", f.get("idCirugia"));
        fila.put("accion", f.get("accion"));
        fila.put("responsable", f.get("responsable"));
        fila.put("detalle", f.get("detalle"));
        if (conValores) {
            fila.put("valoresAnteriores", f.get("valoresAnteriores"));
            fila.put("valoresPosteriores", f.get("valoresPosteriores"));
        }
        return fila;
    }

    // Debe llamarse dentro de la transaccion de la operacion auditada
    private void auditar(int idCirugia, String accion, String responsable, String detalle,
                         Map<String, String> antes, Map<String, String> despues) throws SQLException {
        cirugiaDAO.insertAuditoria(idCirugia, LocalDateTime.now(), accion, responsable,
                detalle != null && detalle.length() > 500 ? detalle.substring(0, 497) + "..." : detalle,
                serializar(antes), serializar(despues));
    }

    // Valores guardados como lineas "campo=valor"
    private String serializar(Map<String, String> valores) {
        if (valores == null || valores.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : valores.entrySet()) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(e.getKey()).append('=').append(e.getValue() == null ? "" : e.getValue().replaceAll("[\\r\\n]+", " "));
        }
        return sb.toString();
    }

    private Map<String, String> parsear(String texto) {
        Map<String, String> valores = new LinkedHashMap<>();
        if (texto == null || texto.isBlank()) {
            return valores;
        }
        for (String linea : texto.split("\n")) {
            int i = linea.indexOf('=');
            if (i > 0) {
                valores.put(linea.substring(0, i), linea.substring(i + 1));
            }
        }
        return valores;
    }

    // Campos auditables de una cirugia
    private Map<String, String> snapshot(Cirugia c) {
        Map<String, String> s = new LinkedHashMap<>();
        s.put("fechaHoraInicio", valor(c.getFechaHoraInicio()));
        s.put("fechaHoraFin", valor(c.getFechaHoraFin()));
        s.put("tipoProcedimiento", valor(c.getTipoProcedimiento()));
        s.put("doctorNombre", valor(c.getDoctorNombre()));
        s.put("diagnostico", valor(c.getDiagnostico()));
        s.put("motivoCancelacion", valor(c.getMotivoCancelacion()));
        s.put("estado", valor(c.getEstado()));
        s.put("idEquipo", valor(c.getEquipo() != null ? c.getEquipo().getIdEquipo() : null));
        s.put("idBandeja", valor(idBandeja(c)));
        s.put("tipoCliente", c.getCliente() instanceof ClinicaHospital ? TIPO_CLIENTE_CLINICA : TIPO_CLIENTE_PARTICULAR);
        s.put("idCliente", valor(c.getCliente() != null ? c.getCliente().getIdCliente() : null));
        s.put("activo", valor(c.isActivo()));
        return s;
    }

    // Deja en antes/despues solo los campos que cambiaron (sin los excluidos)
    private void diferencias(Map<String, String> a, Map<String, String> d, Set<String> excluir,
                             Map<String, String> antes, Map<String, String> despues) {
        for (String campo : d.keySet()) {
            if (excluir.contains(campo)) {
                continue;
            }
            if (!Objects.equals(a.get(campo), d.get(campo))) {
                antes.put(campo, a.get(campo));
                despues.put(campo, d.get(campo));
            }
        }
    }

    private String valor(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private Integer idBandeja(Cirugia c) {
        return c.getBandejaInstrumental() != null ? c.getBandejaInstrumental().getIdBandeja() : null;
    }

    private String textoResponsable(String responsable) {
        return (responsable == null || responsable.isBlank()) ? RESPONSABLE_SISTEMA : responsable.trim();
    }

    private String mensajeCirugia(Cirugia c) {
        return "Cirugía " + (c.getIdCirugia())
                + " · Fecha: " + c.getFechaHoraInicio()
                + " · Procedimiento: " + c.getTipoProcedimiento()
                + " · Equipo: " + (c.getEquipo() != null ? c.getEquipo().getNombre() : "sin equipo")
                + " · Bandeja: " + (c.getBandejaInstrumental() != null
                    ? c.getBandejaInstrumental().getIdBandeja() + " (" + c.getBandejaInstrumental().getTipo() + ")"
                    : "sin bandeja");
    }
}
