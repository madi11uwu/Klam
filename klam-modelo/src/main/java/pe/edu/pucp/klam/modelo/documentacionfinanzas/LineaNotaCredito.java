package pe.edu.pucp.klam.modelo.documentacionfinanzas;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;

public class LineaNotaCredito extends LineaDocumento {

    private String itemReferencia;
    private String motivoDevolucion;

    public LineaNotaCredito() {
        super();
    }

    public LineaNotaCredito(int cantidad, double precioUnitario, String descripcion,
                            Consumible consumible, String itemReferencia, String motivoDevolucion) {
        super(cantidad, precioUnitario, descripcion, consumible);
        this.itemReferencia = itemReferencia;
        this.motivoDevolucion = motivoDevolucion;
    }

    public String getItemReferencia() { return itemReferencia; }
    public void setItemReferencia(String itemReferencia) { this.itemReferencia = itemReferencia; }

    public String getMotivoDevolucion() { return motivoDevolucion; }
    public void setMotivoDevolucion(String motivoDevolucion) { this.motivoDevolucion = motivoDevolucion; }

    @Override
    public String toString() {
        return "LineaNotaCredito{" + getDescripcion() + " x" + getCantidad()
                + " = " + calcularSubtotal() + ", motivo=" + motivoDevolucion + "}";
    }
}
