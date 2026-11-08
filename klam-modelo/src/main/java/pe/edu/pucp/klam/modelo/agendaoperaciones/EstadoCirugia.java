package pe.edu.pucp.klam.modelo.agendaoperaciones;

/**
 * Los valores internos (Java y SQL) no cambian. La columna "texto" es la
 * etiqueta que muestra el frontend Blazor:
 * PROGRAMADA -> "Programada", EN_PROCESO -> "En curso",
 * FINALIZADA -> "Culminada", CANCELADA -> "Cancelada".
 */
public enum EstadoCirugia {
    PROGRAMADA,
    EN_PROCESO,
    FINALIZADA,
    CANCELADA;
}
