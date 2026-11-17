package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.bl.reportes.FiltroConsumo;
import pe.edu.pucp.klam.bl.reportes.ReporteComparativo;
import pe.edu.pucp.klam.bl.reportes.ReporteUsoInsumos;

/**
 * Reportes de consumo (RF18 y RF19). Ambos se construyen solo con consumo real confirmado
 * (cirugias FINALIZADAS con consumo completo); nunca con cantidades previstas.
 */
public interface ReporteBL {

    /** RF18: uso de insumos agrupado por insumo y tipo de cirugia. */
    ReporteUsoInsumos usoInsumos(FiltroConsumo filtro) throws BLException;

    /** RF19: comparativo despachado / consumido / diferencia por cirugia. */
    ReporteComparativo comparativo(FiltroConsumo filtro) throws BLException;
}