
package pe.edu.pucp.klam.bl;

import java.util.List;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.SolicitudCirugia;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;

public interface SolicitudCirugiaBL {

    int registrarSolicitud(
            SolicitudCirugia solicitud
    ) throws BLException;
    int registrarSolicitud(SolicitudCirugia solicitud, String responsable,
            Integer idUsuario, String rol) throws BLException;

    SolicitudCirugia findSolicitudById(
            int idSolicitud
    ) throws BLException;

    List<SolicitudCirugia> listarSolicitudes(
            String estado
    ) throws BLException;

    List<SolicitudCirugia> listarSolicitudesPorCliente(
            Cliente cliente
    ) throws BLException;

    List<SolicitudCirugia> listarSolicitudesPorVendedor(
            Vendedor vendedor
    ) throws BLException;

    void aprobarSolicitud(
            int idSolicitud
    ) throws BLException;
    void aprobarSolicitud(int idSolicitud, String responsable, int idAdministrador) throws BLException;

    void rechazarSolicitud(
            int idSolicitud,
            String motivo
    ) throws BLException;
    void rechazarSolicitud(int idSolicitud, String motivo,
            String responsable, int idAdministrador) throws BLException;

    void reenviarSolicitud(
            int idSolicitud
    ) throws BLException;

    boolean verificarDocumentacionCompleta(
            int idSolicitud
    ) throws BLException;

    record Elegibilidad(boolean puedeRegistrar, String motivo, boolean requiereCotizacion,
            String estadoCotizacion, String estadoDocumental) { }

    Elegibilidad evaluarElegibilidad(int idSolicitud) throws BLException;
    Elegibilidad evaluarElegibilidadParaRegistro(int idSolicitud) throws BLException;

    int registrarCirugiaDesdeSolicitud(
            int idSolicitud,
            Cirugia cirugia,
            String responsable
    ) throws BLException;
}
