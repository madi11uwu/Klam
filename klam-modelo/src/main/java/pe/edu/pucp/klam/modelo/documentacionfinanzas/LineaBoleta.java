package pe.edu.pucp.klam.modelo.documentacionfinanzas;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;

public class LineaBoleta extends LineaDocumento {

    private String itemReferencia;

    public LineaBoleta() {
        super();
    }

    public LineaBoleta(int cantidad, double precioUnitario, String descripcion,
                       Consumible consumible, String itemReferencia) {
        super(cantidad, precioUnitario, descripcion, consumible);
        this.itemReferencia = itemReferencia;
    }

    public String getItemReferencia() { return itemReferencia; }
    public void setItemReferencia(String itemReferencia) { this.itemReferencia = itemReferencia; }

    @Override
    public String toString() {
        return "LineaBoleta{" + getDescripcion() + " x" + getCantidad()
                + " = " + calcularSubtotal() + "}";
    }
}
