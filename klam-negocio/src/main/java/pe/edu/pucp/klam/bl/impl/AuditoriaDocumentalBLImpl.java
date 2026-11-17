package pe.edu.pucp.klam.bl.impl;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import pe.edu.pucp.klam.bl.AuditoriaDocumentalBL;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.dao.AuditoriaDocumentalDAO;
import pe.edu.pucp.klam.dao.impl.AuditoriaDocumentalDAOImpl;

public class AuditoriaDocumentalBLImpl implements AuditoriaDocumentalBL {
    private final AuditoriaDocumentalDAO dao = new AuditoriaDocumentalDAOImpl();

    @Override
    public List<Map<String, Object>> buscar(String cliente, String paciente, LocalDateTime desde,
            LocalDateTime hasta, String tipoDocumento, String accion, String responsable,
            Integer idSolicitud, Integer idCirugia, int pagina, int tamano) throws BLException {
        if (desde != null && hasta != null && desde.isAfter(hasta))
            throw new BLException("La fecha inicial no puede superar la final");
        if (pagina < 1 || tamano < 1 || tamano > 100)
            throw new BLException("Paginación no válida");
        if (idSolicitud != null && idSolicitud <= 0 || idCirugia != null && idCirugia <= 0)
            throw new BLException("Identificador no válido");
        try {
            return dao.buscar(cliente, paciente, desde, hasta, tipoDocumento, accion,
                    responsable, idSolicitud, idCirugia, tamano, Math.multiplyExact(pagina - 1, tamano));
        } catch (SQLException | ArithmeticException ex) {
            throw new BLException("No se pudo consultar la bitácora documental", ex);
        }
    }

    @Override
    public Map<String, Object> obtener(int idEvento) throws BLException {
        if (idEvento <= 0) throw new BLException("Identificador no válido");
        try {
            return dao.obtener(idEvento);
        } catch (SQLException ex) {
            throw new BLException("No se pudo consultar el evento documental", ex);
        }
    }
}
