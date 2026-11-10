package pe.edu.pucp.klam.rest.dto;

import java.util.ArrayList;
import java.util.List;

public class BandejaInstrumentalDTO {
    private int idBandeja;
    private boolean activo;
    private String tipo;
    private String procedimiento;
    private boolean esterilizado;
    private List<LineaConsumibleDTO> consumibles = new ArrayList<>();
    private List<LineaConsumibleDTO> consumiblesConsumidos = new ArrayList<>();

    public BandejaInstrumentalDTO() {
    }

    public int getIdBandeja() {
        return idBandeja;
    }

    public void setIdBandeja(int idBandeja) {
        this.idBandeja = idBandeja;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getProcedimiento() {
        return procedimiento;
    }

    public void setProcedimiento(String procedimiento) {
        this.procedimiento = procedimiento;
    }

    public boolean isEsterilizado() {
        return esterilizado;
    }

    public void setEsterilizado(boolean esterilizado) {
        this.esterilizado = esterilizado;
    }

    public List<LineaConsumibleDTO> getConsumibles() {
        return consumibles;
    }

    public void setConsumibles(List<LineaConsumibleDTO> consumibles) {
        this.consumibles = consumibles;
    }

    public List<LineaConsumibleDTO> getConsumiblesConsumidos() {
        return consumiblesConsumidos;
    }

    public void setConsumiblesConsumidos(List<LineaConsumibleDTO> consumiblesConsumidos) {
        this.consumiblesConsumidos = consumiblesConsumidos;
    }
}
