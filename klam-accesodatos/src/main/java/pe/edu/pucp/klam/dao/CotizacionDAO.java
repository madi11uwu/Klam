package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.documentacionfinanzas.Cotizacion;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.EstadoCotizacion;

import java.sql.SQLException;
import java.util.List;

public interface CotizacionDAO extends DAO<Cotizacion, Integer> {
    List<Cotizacion> findByCirugia(Integer idCirugia) throws SQLException;
    List<Cotizacion> listarPorSolicitud(Integer idSolicitud) throws SQLException;
    Cotizacion buscarVigentePorSolicitud(Integer idSolicitud) throws SQLException;
    void insertarVersionSolicitud(Cotizacion cotizacion) throws SQLException;
    void actualizarEstado(Integer id, EstadoCotizacion estado) throws SQLException;
    boolean resolverVersionVigente(int id, EstadoCotizacion estado, int idCuentaUsuario,
            String motivo) throws SQLException;
}
