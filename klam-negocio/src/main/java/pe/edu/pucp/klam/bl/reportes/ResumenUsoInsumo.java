package pe.edu.pucp.klam.bl.reportes;

/**
 * RF18: un insumo dentro de un tipo de cirugia. cantidadUtilizada es la suma de lo consumido;
 * frecuencia es el numero de cirugias en que se utilizo (consumido &gt; 0).
 */
public record ResumenUsoInsumo(String nombre,
                               String medida,
                               String tipoProcedimiento,
                               int cantidadUtilizada,
                               int frecuencia) {
}