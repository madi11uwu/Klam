package pe.edu.pucp.klam.rest.dto;

// Datos para crear una plantilla de bandeja
public class BandejaFormDTO {
    private String tipo;
    private String procedimiento;
    private boolean esterilizado;

    public BandejaFormDTO() {
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
}
