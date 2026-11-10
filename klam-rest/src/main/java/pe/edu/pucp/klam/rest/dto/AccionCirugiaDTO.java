package pe.edu.pucp.klam.rest.dto;

// Cuerpo de cambiar estado (estado) y de cancelar (motivo)
public class AccionCirugiaDTO {
    private String estado;
    private String motivo;

    public AccionCirugiaDTO() {
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
