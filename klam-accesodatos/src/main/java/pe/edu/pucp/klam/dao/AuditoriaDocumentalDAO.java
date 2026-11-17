package pe.edu.pucp.klam.dao;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditoriaDocumentalDAO {
    void registrar(Integer idDocumento, Integer idSolicitud, Integer idCirugia,
            String tipoDocumento, String accion, String responsable, String rolResponsable,
            Integer idUsuarioResponsable, String estadoAnterior, String estadoNuevo,
            String descripcion) throws SQLException;

    List<Map<String, Object>> buscar(String cliente, String paciente, LocalDateTime desde,
            LocalDateTime hasta, String tipoDocumento, String accion, String responsable,
            Integer idSolicitud, Integer idCirugia, int limite, int desplazamiento) throws SQLException;

    Map<String, Object> obtener(int idEvento) throws SQLException;
}
