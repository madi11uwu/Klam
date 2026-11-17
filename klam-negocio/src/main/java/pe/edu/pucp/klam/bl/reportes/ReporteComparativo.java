package pe.edu.pucp.klam.bl.reportes;

import java.time.LocalDateTime;
import java.util.List;

/** RF19: cirugias con consumo completo que cumplen los filtros, en orden cronologico. */
public record ReporteComparativo(LocalDateTime generado,
                                 FiltroConsumo filtro,
                                 List<CirugiaComparativa> cirugias) {
}