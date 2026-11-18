package pe.edu.pucp.klam.bl.reportes;

import java.time.LocalDateTime;
import java.util.List;

/** RF19: cabecera de la cirugia (fecha, procedimiento, paciente, medico, cliente) y sus insumos. */
public record CirugiaComparativa(int idCirugia,
                                 LocalDateTime fecha,
                                 String procedimiento,
                                 String paciente,
                                 String documentoPaciente,
                                 String medico,
                                 String tipoCliente,
                                 String cliente,
                                 List<LineaComparativa> lineas) {
}