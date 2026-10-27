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
        setCantidad(cantidad);
        setPrecioUnitario(precioUnitario);
        setDescripcion(descripcion);
        setConsumible(consumible);
    }

    public LineaDocumento(final LineaDocumento lineaDocumento) {
        if (lineaDocumento == null) {
            throw new IllegalArgumentException("La linea de documento no puede ser nula");
        }
        setIdLinea(lineaDocumento.getIdLinea());
        setCantidad(lineaDocumento.getCantidad());
        setPrecioUnitario(lineaDocumento.getPrecioUnitario());
        setDescripcion(lineaDocumento.getDescripcion());
        setConsumible(lineaDocumento.getConsumible());
    }

    public int getIdLinea() { return idLinea; }
    public void setIdLinea(int idLinea) { this.idLinea = idLinea; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) {
        if (!Double.isFinite(precioUnitario) || precioUnitario < 0) {
            throw new IllegalArgumentException("El precio unitario debe ser finito y no negativo");
        }
        this.precioUnitario = precioUnitario;
    }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripcion no puede ser nula ni vacia");
        }
        this.descripcion = descripcion;
    }

    public Consumible getConsumible() { return consumible; }
    public void setConsumible(Consumible consumible) { this.consumible = consumible; }
}
