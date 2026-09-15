package pe.edu.pucp.klam.modelo.documentacionfinanzas;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;

public abstract class LineaDocumento {

    private int idLinea;
    private int cantidad;
    private double precioUnitario;
    private String descripcion;
    private Consumible consumible;

    public LineaDocumento() {
    }

    public LineaDocumento(int cantidad, double precioUnitario, String descripcion, Consumible consumible) {
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.descripcion = descripcion;
        this.consumible = consumible;
    }

    public double calcularSubtotal() {
        return this.cantidad * this.precioUnitario;
    }

    public int getIdLinea() { return idLinea; }
    public void setIdLinea(int idLinea) { this.idLinea = idLinea; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Consumible getConsumible() { return consumible; }
    public void setConsumible(Consumible consumible) { this.consumible = consumible; }
}
