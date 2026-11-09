package pe.edu.pucp.klam.dao;

import java.sql.SQLException;
import java.util.List;

import pe.edu.pucp.klam.modelo.agendaoperaciones.SolicitudCirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoSolicitudCirugia;
import pe.edu.pucp.klam.modelo.clientes.Cliente;

public interface SolicitudCirugiaDAO {
    void insert(SolicitudCirugia solicitud) throws SQLException;
    SolicitudCirugia findById(Integer id) throws SQLException;
    List<SolicitudCirugia> findAll() throws SQLException;
    List<SolicitudCirugia> listarPorEstado(EstadoSolicitudCirugia e) throws SQLException;
    List<SolicitudCirugia> listarPorCliente(Cliente cliente) throws SQLException;
    void cambiarEstado(SolicitudCirugia solicitud) throws SQLException;
}
