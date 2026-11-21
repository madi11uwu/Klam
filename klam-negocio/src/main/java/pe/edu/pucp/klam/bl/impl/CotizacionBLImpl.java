package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CotizacionBL;
import pe.edu.pucp.klam.dao.CirugiaDAO;
import pe.edu.pucp.klam.dao.CuentaUsuarioDAO;
import pe.edu.pucp.klam.dao.AuditoriaDocumentalDAO;
import pe.edu.pucp.klam.dao.impl.AuditoriaDocumentalDAOImpl;
import pe.edu.pucp.klam.dao.impl.CuentaUsuarioDAOImpl;
import pe.edu.pucp.klam.bl.DocumentoIngresoBL;
import pe.edu.pucp.klam.dao.SolicitudCirugiaDAO;
import pe.edu.pucp.klam.dao.impl.SolicitudCirugiaDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoSolicitudCirugia;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.dao.CotizacionDAO;
import pe.edu.pucp.klam.dao.impl.CirugiaDAOImpl;
import pe.edu.pucp.klam.dao.impl.CotizacionDAOImpl;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Cotizacion;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.EstadoCotizacion;

import java.sql.SQLException;
import java.util.List;

public class CotizacionBLImpl implements CotizacionBL {
    private static final double PRECIO_MINIMO = 0.0;

    private final CotizacionDAO cotizacionDAO = new CotizacionDAOImpl();
    private final CirugiaDAO cirugiaDAO = new CirugiaDAOImpl();
    private final SolicitudCirugiaDAO solicitudDAO = new SolicitudCirugiaDAOImpl();
    private final CuentaUsuarioDAO cuentaDAO = new CuentaUsuarioDAOImpl();
    private final DocumentoIngresoBL documentoBL = new DocumentoIngresoBLImpl();
    private final AuditoriaDocumentalDAO auditoriaDAO = new AuditoriaDocumentalDAOImpl();

    @Override
    public List<Cotizacion> findAll() throws BLException {
        try {
            return cotizacionDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las cotizaciones", e);
        }
    }

    @Override
    public Cotizacion findById(Integer id) throws BLException {
        try {
            return cotizacionDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la cotización", e);
        }
    }

    @Override
    public List<Cotizacion> findByCirugia(Integer idCirugia) throws BLException {
        if (idCirugia == null) {
            throw new BLException("Debe indicar la cirugía de las cotizaciones a buscar");
        }

        try {
            return cotizacionDAO.findByCirugia(idCirugia);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las cotizaciones de la cirugía", e);
        }
    }

    @Override
    public List<Cotizacion> listarPorSolicitud(Integer idSolicitud) throws BLException {
        try {
            return cotizacionDAO.listarPorSolicitud(idSolicitud);
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar el historial de cotizaciones", e);
        }
    }

    @Override
    public Cotizacion buscarVigentePorSolicitud(Integer idSolicitud) throws BLException {
        try {
            return cotizacionDAO.buscarVigentePorSolicitud(idSolicitud);
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar la cotización vigente", e);
        }
    }
    @Override
    public void insert(Cotizacion cotizacion) throws BLException {
        if (cotizacion != null && cotizacion.getSolicitud() != null) {
            throw new BLException("Use registrarVersion con identidad autenticada para una solicitud");
        }
        validarDatos(cotizacion);
        try {
            cotizacionDAO.insert(cotizacion);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la cotización", e);
        }
    }

    @Override
    public void registrarVersion(Cotizacion cotizacion, int idAdministrador, String responsable)
            throws BLException {
        if (idAdministrador <= 0) throw new SecurityException("Se requiere un administrador autenticado");
        validarDatos(cotizacion);
        if (!Double.isFinite(cotizacion.getPrecioPactado()))
            throw new BLException("El precio del ERP debe ser finito");
        if (cotizacion.getSolicitud() == null)
            throw new BLException("La cotización nueva debe pertenecer a una solicitud");
        TransactionsManager.iniciar();
        try {
            int idSolicitud = cotizacion.getSolicitud().getIdSolicitud();
            solicitudDAO.bloquearParaRegistro(idSolicitud);
            var solicitud = solicitudDAO.findById(idSolicitud);
            if (solicitud == null || !(solicitud.getCliente() instanceof ClinicaHospital)
                    || solicitud.getEstado() != EstadoSolicitudCirugia.APROBADA)
                throw new BLException("La cotización requiere una solicitud de clínica aprobada");
            if (!documentoBL.verificarDocumentosObligatoriosValidados(idSolicitud))
                throw new BLException("Faltan documentos obligatorios vigentes y validados");
            if (cirugiaDAO.buscarCirugiaVigentePorSolicitud(idSolicitud) != null)
                throw new BLException("La solicitud ya tiene una cirugía vigente");
            cotizacionDAO.insertarVersionSolicitud(cotizacion);
            auditoriaDAO.registrar(null, idSolicitud, null, "COTIZACION", "EMISION",
                    responsable, "ADMINISTRADOR", idAdministrador, null, "EMITIDA",
                    "Cotización " + cotizacion.getIdCotizacion() + " versión " + cotizacion.getVersion());
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo registrar la cotización", e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }
    @Override
    public void update(Cotizacion cotizacion) throws BLException {
        validarDatos(cotizacion);

        Cotizacion existente = buscarCotizacion(cotizacion.getIdCotizacion());
        if (existente.estaResuelta() && existente.getEstado() != cotizacion.getEstado()) {
            throw new BLException("La cotización ya está " + existente.getEstado()
                    + " y no puede pasar a " + cotizacion.getEstado());
        }

        try {
            cotizacionDAO.update(cotizacion);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la cotización", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        Cotizacion existente = buscarCotizacion(id);
        if (existente.getSolicitud() != null)
            throw new BLException("El historial de cotizaciones de solicitud no se elimina");
        try {
            cotizacionDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar la cotización", e);
        }
    }

    @Override
    public void decidirVersion(int idCotizacion, int idCuentaUsuario, boolean aceptar, String motivo, String responsable)
            throws BLException {
        if (idCuentaUsuario <= 0) throw new SecurityException("Se requiere una cuenta clínica autenticada");
        if (!aceptar && motivo != null && motivo.trim().length() > 150)
            throw new BLException("El motivo de rechazo no puede superar 150 caracteres");
        TransactionsManager.iniciar();
        try {
            Cotizacion previa = buscarCotizacion(idCotizacion);
            if (previa.getSolicitud() == null)
                throw new BLException("La cotización no pertenece a una solicitud clínica");
            int idSolicitud = previa.getSolicitud().getIdSolicitud();
            solicitudDAO.bloquearParaRegistro(idSolicitud);
            var solicitud = solicitudDAO.findById(idSolicitud);
            if (solicitud == null || !(solicitud.getCliente() instanceof ClinicaHospital)
                    || solicitud.getEstado() != EstadoSolicitudCirugia.APROBADA)
                throw new BLException("La solicitud clínica no está aprobada");
            if (!cuentaDAO.esCuentaDeClinica(idCuentaUsuario, solicitud.getCliente().getIdCliente()))
                throw new SecurityException("La cuenta no representa a la clínica solicitante");
            if (!documentoBL.verificarDocumentosObligatoriosValidados(idSolicitud))
                throw new BLException("Faltan documentos obligatorios vigentes y validados");
            Cotizacion vigente = cotizacionDAO.buscarVigentePorSolicitud(idSolicitud);
            if (vigente == null || vigente.getIdCotizacion() != idCotizacion
                    || vigente.getEstado() != EstadoCotizacion.EMITIDA || !vigente.isActivo())
                throw new BLException("Solo puede decidirse la cotización vigente EMITIDA");
            boolean actualizado = cotizacionDAO.resolverVersionVigente(idCotizacion,
                    aceptar ? EstadoCotizacion.ACEPTADA : EstadoCotizacion.RECHAZADA,
                    idCuentaUsuario, aceptar ? null : motivo);
            if (!actualizado) throw new BLException("La cotización ya fue decidida o reemplazada");
            auditoriaDAO.registrar(null, idSolicitud, null, "COTIZACION",
                    aceptar ? "ACEPTACION" : "RECHAZO", responsable,
                    "CLINICA_HOSPITAL", idCuentaUsuario, "EMITIDA",
                    aceptar ? "ACEPTADA" : "RECHAZADA",
                    "Cotización " + idCotizacion + " versión " + vigente.getVersion()
                            + (motivo == null || motivo.isBlank() ? "" : ". Motivo: " + motivo));
            TransactionsManager.commit();
        } catch (SQLException e) {
            TransactionsManager.rollbackSiActiva();
            throw new BLException("No se pudo registrar la decisión de cotización", e);
        } catch (RuntimeException e) {
            TransactionsManager.rollbackSiActiva();
            throw e;
        }
    }
    @Override
    public void aceptar(Integer id) throws BLException {
        resolver(id, EstadoCotizacion.ACEPTADA);
    }

    @Override
    public void rechazar(Integer id) throws BLException {
        resolver(id, EstadoCotizacion.RECHAZADA);
    }

    @Override
    public void vencer(Integer id) throws BLException {
        resolver(id, EstadoCotizacion.VENCIDA);
    }

    // La transición de EMITIDA a un estado resuelto se valida en BL y en el setter del Modelo.
    private void resolver(Integer id, EstadoCotizacion nuevo) throws BLException {
        Cotizacion cotizacion = buscarCotizacion(id);
        if (cotizacion.getSolicitud() != null)
            throw new BLException("La cotización de solicitud requiere la decisión autenticada de su clínica");
        if (nuevo != EstadoCotizacion.ACEPTADA && nuevo != EstadoCotizacion.RECHAZADA
                && nuevo != EstadoCotizacion.VENCIDA) {
            throw new BLException("Estado de cotización no válido: " + nuevo);
        }
        if (cotizacion.getSolicitud() != null && !cotizacion.isVigente()) {
            throw new BLException("No se puede resolver una versión anterior de cotización");
        }
        if (cotizacion.estaResuelta()) {
            throw new BLException("La cotización ya está " + cotizacion.getEstado()
                    + " y no puede pasar a " + nuevo);
        }
        try {
            cotizacion.setEstado(nuevo);
        } catch (IllegalStateException e) {
            throw new BLException(e.getMessage(), e);
        }
        try {
            cotizacionDAO.actualizarEstado(id, cotizacion.getEstado());
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar el estado de la cotización", e);
        }
    }

    private void validarDatos(Cotizacion cotizacion) throws BLException {
        if (cotizacion == null) {
            throw new BLException("La cotización no puede ser nula");
        }

        if (cotizacion.getPrecioPactado() < PRECIO_MINIMO) {
            throw new BLException(String.format(
                    "El precio pactado no puede ser menor a S/ %.2f", PRECIO_MINIMO));
        }

        if (cotizacion.getFechaEmision() == null) {
            throw new BLException("La fecha de emisión es obligatoria");
        }

        if (cotizacion.getSolicitud() != null) {
            if (cotizacion.getCirugia() != null || cotizacion.getSolicitud().getIdSolicitud() <= 0
                    || cotizacion.getArchivoPath() == null || cotizacion.getArchivoPath().isBlank()) {
                throw new BLException("La cotización de solicitud requiere solicitud y archivo, sin cirugía previa");
            }
            return;
        }
        if (cotizacion.getCirugia() == null) {
            throw new BLException("La cotización debe estar asociada a una cirugía");
        }
        validarCirugiaExiste(cotizacion.getCirugia().getIdCirugia());
    }

    private void validarCirugiaExiste(int idCirugia) throws BLException {
        try {
            if (cirugiaDAO.findById(idCirugia) == null) {
                throw new BLException("No existe una cirugía con id " + idCirugia);
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de la cirugía", e);
        }
    }

    private Cotizacion buscarCotizacion(int id) throws BLException {
        try {
            Cotizacion cotizacion = cotizacionDAO.findById(id);
            if (cotizacion == null) {
                throw new BLException("No existe una cotización con id " + id);
            }
            return cotizacion;
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de la cotización", e);
        }
    }
}
