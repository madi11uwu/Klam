package pe.edu.pucp.klam.bl.reportes;

import java.time.LocalDate;

/**
 * Criterios de los reportes de consumo. Todos son opcionales (null = sin filtro).
 * RF18 usa: desde, hasta, tipoProcedimiento, idConsumible, medida.
 * RF19 usa: idCirugia, tipoCliente, idCliente, desde, hasta.
 */
public record FiltroConsumo(LocalDate desde,
                            LocalDate hasta,
                            String tipoProcedimiento,
                            Integer idConsumible,
                            String medida,
                            Integer idCirugia,
                            String tipoCliente,
                            Integer idCliente) {
}