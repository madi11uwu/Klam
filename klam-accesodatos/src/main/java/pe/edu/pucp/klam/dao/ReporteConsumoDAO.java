package pe.edu.pucp.klam.dao;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Lectura de consumos reales para los reportes (RF18 y RF19). Solo consulta: no modifica datos.
 * Cada fila es una cirugia FINALIZADA con consumo completo y un consumible de su bandeja.
 * Los filtros nulos no restringen la busqueda.
 */
public interface ReporteConsumoDAO {

    List<Map<String, Object>> buscarFilasConsumo(Integer idCirugia,
                                                 String tipoCliente,
                                                 Integer idCliente,
                                                 LocalDateTime desde,
                                                 LocalDateTime hasta,
                                                 String tipoProcedimiento,
                                                 Integer idConsumible,
                                                 String medida) throws SQLException;
}