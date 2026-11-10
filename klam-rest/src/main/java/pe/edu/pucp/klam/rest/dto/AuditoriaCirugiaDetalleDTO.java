package pe.edu.pucp.klam.rest.dto;

import java.util.ArrayList;
import java.util.List;

/** Entrada de la bitacora con sus diferencias campo por campo (RF16). */
public class AuditoriaCirugiaDetalleDTO extends AuditoriaCirugiaDTO {

    private List<CambioAuditoriaDTO> cambios = new ArrayList<>();

    public AuditoriaCirugiaDetalleDTO() {
    }

    public List<CambioAuditoriaDTO> getCambios() {
        return cambios;
    }

    public void setCambios(List<CambioAuditoriaDTO> cambios) {
        this.cambios = cambios;
    }
}
