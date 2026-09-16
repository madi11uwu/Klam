package pe.edu.pucp.klam.modelo.documentacionfinanzas;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;

public class LineaFactura extends LineaDocumento {

    private String itemReferencia;

    public LineaFactura() {
        super();
    }

    public LineaFactura(int cantidad, double precioUnitario, String descripcion,
                        Consumible consumible, String itemReferencia) {
        super(cantidad, precioUnitario, descripcion, consumible);
        this.itemReferencia = itemReferencia;
    }

    public String getItemReferencia() { return itemReferencia; }
    public void setItemReferencia(String itemReferencia) { this.itemReferencia = itemReferencia; }

    @Override
    public String toString() {
        return "LineaFactura{" + getDescripcion() + " x" + getCantidad()
                + " = " + calcularSubtotal() + "}";
    }
}
