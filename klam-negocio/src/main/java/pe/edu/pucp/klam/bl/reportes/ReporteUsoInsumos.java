package pe.edu.pucp.klam.bl.reportes;

import java.time.LocalDateTime;
import java.util.List;

/** RF18: resultado del reporte de uso de insumos, ordenado por cantidad utilizada (mayor demanda primero). */
public record ReporteUsoInsumos(LocalDateTime generado,
                                FiltroConsumo filtro,
                                int cirugiasConsideradas,
                                List<ResumenUsoInsumo> insumos) {
}