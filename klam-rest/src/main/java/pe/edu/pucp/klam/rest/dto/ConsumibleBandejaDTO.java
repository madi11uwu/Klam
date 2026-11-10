package pe.edu.pucp.klam.rest.dto;

// Consumible dentro de una bandeja; también es el cuerpo para asociar o cambiar la cantidad
public class ConsumibleBandejaDTO {
    private int idConsumible;
    private String nombreComercial;
    private String medida;
    private int cantidad;

    public ConsumibleBandejaDTO() {
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

    public String getMedida() {
        return medida;
    }

    public void setMedida(String medida) {
        this.medida = medida;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
