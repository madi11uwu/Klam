package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.NotificacionBL;
import pe.edu.pucp.klam.dao.NotificacionDAO;
import pe.edu.pucp.klam.dao.impl.NotificacionDAOImpl;
import pe.edu.pucp.klam.modelo.comunicaciones.Notificacion;

import java.sql.SQLException;
import java.util.List;

public class NotificacionBLImpl implements NotificacionBL {

    private final NotificacionDAO notificacionDAO = new NotificacionDAOImpl();

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
    public List listarPorDestinatario(Integer idAdministrador, Integer idVendedor, Integer idTecnico) throws BLException {
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
}