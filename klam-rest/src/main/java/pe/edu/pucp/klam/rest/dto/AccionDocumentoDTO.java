package pe.edu.pucp.klam.rest.dto;

public class AccionDocumentoDTO {
    private String motivo;
    private String archivoPath;
    private boolean habilitarResubida;

    public AccionDocumentoDTO() {
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public boolean isHabilitarResubida() {
        return habilitarResubida;
    }

    public void setHabilitarResubida(boolean habilitarResubida) {
        this.habilitarResubida = habilitarResubida;
    }

    public String getArchivoPath() {
        return archivoPath;
    }

    public void setArchivoPath(String archivoPath) {
        this.archivoPath = archivoPath;
    }
}
