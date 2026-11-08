package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.NotificacionBL;
import pe.edu.pucp.klam.dao.AdministradorDAO;
import pe.edu.pucp.klam.dao.NotificacionDAO;
import pe.edu.pucp.klam.dao.TecnicoInstrumentistaDAO;
import pe.edu.pucp.klam.dao.impl.AdministradorDAOImpl;
import pe.edu.pucp.klam.dao.impl.NotificacionDAOImpl;
import pe.edu.pucp.klam.dao.impl.TecnicoInstrumentistaDAOImpl;
import pe.edu.pucp.klam.modelo.comunicaciones.Notificacion;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Administrador;
import pe.edu.pucp.klam.modelo.usuariosPermisos.TecnicoInstrumentista;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificacionBLImpl implements NotificacionBL {

    private final NotificacionDAO notificacionDAO = new NotificacionDAOImpl();
    private final AdministradorDAO administradorDAO = new AdministradorDAOImpl();
    private final TecnicoInstrumentistaDAO tecnicoDAO = new TecnicoInstrumentistaDAOImpl();

    @Override
    public List findAll() throws BLException {
        try {
            return notificacionDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las notificaciones", e);
        }
    }

    @Override
    public Notificacion findById(Integer id) throws BLException {
        if (id == null) throw new BLException("El ID no puede ser nulo");
        try {
            return notificacionDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la notificación con ID: " + id, e);
        }
    }

    @Override
    public void insert(Notificacion notificacion) throws BLException {
        validarDatos(notificacion);
        try {
            notificacionDAO.insert(notificacion);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la notificación", e);
        }
    }

    @Override
    public void update(Notificacion notificacion) throws BLException {
        validarDatos(notificacion);
        validarExiste(notificacion.getIdNotificacion());
        try {
            notificacionDAO.update(notificacion);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la notificación", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarExiste(id);
        try {
            notificacionDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar la notificación", e);
        }
    }

    @Override
    public void marcarLeida(Integer id, boolean leida) throws BLException {
        validarExiste(id);
        try {
            notificacionDAO.marcarLeida(id, leida);
        } catch (SQLException e) {
            throw new BLException("No se pudo cambiar el estado de lectura", e);
        }
    }

    @Override
    public List <Notificacion> listarPorDestinatario(Integer idAdministrador, Integer idVendedor, Integer idTecnico) throws BLException {
        if (idAdministrador == null && idVendedor == null && idTecnico == null) {
            throw new BLException("Debe especificar al menos un destinatario");
        }
        try {
            return notificacionDAO.listarPorDestinatario(idAdministrador, idVendedor, idTecnico);
        } catch (SQLException e) {
            throw new BLException("No se pudieron listar las notificaciones", e);
        }
    }

    @Override
    public List listarNoLeidas() throws BLException {
        try {
            return notificacionDAO.listarNoLeidas();
        } catch (SQLException e) {
            throw new BLException("No se pudieron listar las notificaciones no leídas", e);
        }
    }

    private void validarDatos(Notificacion notificacion) throws BLException {
        if (notificacion == null) {
            throw new BLException("La notificación no puede ser nula");
        }
        if (notificacion.getTitulo() == null || notificacion.getTitulo().isBlank()) {
            throw new BLException("El título de la notificación es obligatorio");
        }
        if (notificacion.getMensaje() == null || notificacion.getMensaje().isBlank()) {
            throw new BLException("El mensaje de la notificación es obligatorio");
        }
        if (notificacion.getFechaHora() == null) {
            throw new BLException("La fecha y hora de la notificación son obligatorias");
        }
        if (notificacion.getDestinatario() == null || notificacion.getDestinatario().getIdUsuario() <= 0) {
            throw new BLException("El destinatario de la notificación es inválido u obligatorio");
        }
    }

    private void validarExiste(Integer id) throws BLException {
        if (id == null) throw new BLException("El ID no puede ser nulo");
        try {
            if (notificacionDAO.findById(id) == null) {
                throw new BLException("No existe una notificación con el ID " + id);
            }
        } catch (SQLException e) {
            throw new BLException("Error al verificar la notificación", e);
        }
    }

    // ------------------------------------------------------------------
    // Notificaciones automaticas internas
    // ------------------------------------------------------------------
    @Override
    public void notificarAdministradores(String titulo, String mensaje) throws BLException {
        try {
            List<Administrador> administradores = administradorDAO.findAll();
            notificarA(new ArrayList<UsuarioPlataforma>(administradores), titulo, mensaje);
        } catch (SQLException e) {
            throw new BLException("No se pudo notificar a los administradores", e);
        }
    }

    @Override
    public void notificarTecnicos(String titulo, String mensaje) throws BLException {
        try {
            List<TecnicoInstrumentista> tecnicos = tecnicoDAO.findAll();
            notificarA(new ArrayList<UsuarioPlataforma>(tecnicos), titulo, mensaje);
        } catch (SQLException e) {
            throw new BLException("No se pudo notificar a los técnicos instrumentistas", e);
        }
    }

    // Una notificacion por destinatario. Si hay una transaccion activa, se une a ella.
    private void notificarA(List<UsuarioPlataforma> destinatarios, String titulo, String mensaje)
            throws BLException, SQLException {
        for (UsuarioPlataforma destinatario : destinatarios) {
            if (!destinatario.isActivo()) {
                continue;
            }
            Notificacion n = new Notificacion();
            n.setFechaHora(LocalDateTime.now());
            n.setTitulo(recortar(titulo, 150));
            n.setMensaje(recortar(mensaje, 500));
            n.setEstadoLeida(false);
            n.setDestinatario(destinatario);
            validarDatos(n);
            notificacionDAO.insert(n);
        }
    }

    private String recortar(String texto, int maximo) {
        if (texto == null || texto.length() <= maximo) {
            return texto;
        }
        return texto.substring(0, maximo - 3) + "...";
    }
}
