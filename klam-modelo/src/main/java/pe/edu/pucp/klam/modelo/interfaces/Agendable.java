package pe.edu.pucp.klam.modelo.interfaces;

import java.time.LocalDateTime;

public interface Agendable {
    LocalDateTime getFechaHoraInicio();
    LocalDateTime getFechaHoraFin();
    String getIdentificador();
}