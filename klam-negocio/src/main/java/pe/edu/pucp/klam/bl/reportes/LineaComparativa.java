package pe.edu.pucp.klam.bl.reportes;

/** RF19: un consumible de una cirugia. diferencia = despachada - consumida. */
public record LineaComparativa(String nombre,
                               String medida,
                               int despachada,
                               int consumida,
                               int diferencia) {
}