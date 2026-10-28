package pe.edu.pucp.klam.modelo.agendaoperaciones;

/**
 * Los valores internos (Java y SQL) no cambian. La columna "texto" es la
 * etiqueta que muestra el frontend Blazor:
 * PROGRAMADA -> "Programada", EN_PROCESO -> "En curso",
 * FINALIZADA -> "Culminada", CANCELADA -> "Cancelada".
 */
public enum EstadoCirugia {
    PROGRAMADA("Programada"),
    EN_PROCESO("En curso"),
    FINALIZADA("Culminada"),
    CANCELADA("Cancelada");

    private final String texto;

    EstadoCirugia(String texto) {
        this.texto = texto;
    }

    /** Texto que se muestra en las pantallas Blazor. */
    public String getTexto() {
        return texto;
    }

    /** Convierte el texto de Blazor ("En curso", "Culminada"...) al estado interno. */
    public static EstadoCirugia desdeTexto(String texto) {
        for (EstadoCirugia e : values()) {
            if (e.texto.equalsIgnoreCase(texto)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado de cirugia desconocido: " + texto);
    }
}
