package pe.edu.pucp.klam.rest.dto;

public class LineaConsumibleDTO {
    private ConsumibleDTO consumible;
    private int cantidad;

    public LineaConsumibleDTO() {
    }

    public ConsumibleDTO getConsumible() {
        return consumible;
    }

    public void setConsumible(ConsumibleDTO consumible) {
        this.consumible = consumible;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
