package pe.edu.pucp.klam.interfaces;

import java.time.LocalDateTime;
public interface Agendable {
    LocalDateTime getFechaHoraInicio();
    LocalDateTime getFechaHoraFin();
    String getIdentificador();
}