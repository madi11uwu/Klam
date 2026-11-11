package pe.edu.pucp.klam.rest.dto;

import java.util.ArrayList;
import java.util.List;

// Bandeja (plantilla) con los consumibles que se despachan con ella
public class BandejaDTO {
    private int idBandeja;
    private String tipo;
    private String procedimiento;
    private boolean esterilizado;
    private boolean activo;
    private Integer idCirugia;   // cirugía a la que está asignada, o null
    private List<ConsumibleBandejaDTO> consumibles = new ArrayList<>();

    public BandejaDTO() {
    }

    public int getIdBandeja() {
        return idBandeja;
    }

    public void setIdBandeja(int idBandeja) {
        this.idBandeja = idBandeja;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Integer getIdCirugia() {
        return idCirugia;
    }

    public void setIdCirugia(Integer idCirugia) {
        this.idCirugia = idCirugia;
    }

    public List<ConsumibleBandejaDTO> getConsumibles() {
        return consumibles;
    }

    public void setConsumibles(List<ConsumibleBandejaDTO> consumibles) {
        this.consumibles = consumibles;
    }
}
