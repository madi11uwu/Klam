package pe.edu.pucp.klam.rest.reportes;

import java.util.List;

/** Datos del encabezado de un reporte exportado: titulo, fecha de generacion, periodo y criterios aplicados. */
public record CabeceraReporte(String titulo, String generado, String periodo, List<String> criterios) {
}