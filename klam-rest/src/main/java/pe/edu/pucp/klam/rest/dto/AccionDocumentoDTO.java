package pe.edu.pucp.klam.rest.dto;

public class AccionDocumentoDTO {
    private String motivo;
    private String archivoPath;

    public AccionDocumentoDTO() {
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getArchivoPath() {
        return archivoPath;
    }

    public void setArchivoPath(String archivoPath) {
        this.archivoPath = archivoPath;
    }
}
