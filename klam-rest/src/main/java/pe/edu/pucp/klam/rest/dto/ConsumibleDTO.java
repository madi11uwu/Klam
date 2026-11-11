package pe.edu.pucp.klam.rest.dto;

// Consumible del catálogo
public class ConsumibleDTO {
    private int idConsumible;
    private String nombreComercial;
    private String marca;
    private String medida;
    private boolean activo;

    public ConsumibleDTO() {
    }

    public int getIdConsumible() {
        return idConsumible;
    }

    public void setIdConsumible(int idConsumible) {
        this.idConsumible = idConsumible;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getMedida() {
        return medida;
    }

    public void setMedida(String medida) {
        this.medida = medida;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
