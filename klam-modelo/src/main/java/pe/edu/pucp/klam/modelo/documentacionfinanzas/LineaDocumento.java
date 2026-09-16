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
        validarCantidad(cantidad);
        validarPrecioUnitario(precioUnitario);
        validarDescripcion(descripcion);
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.descripcion = descripcion;
        this.consumible = consumible;
    }

    public LineaDocumento(final LineaDocumento lineaDocumento){
        if(lineaDocumento==null){
            throw new IllegalArgumentException("La linea de documento no puede ser nula");
        }
        setIdLinea(lineaDocumento.getIdLinea());
        setCantidad(lineaDocumento.getCantidad());
        setPrecioUnitario(lineaDocumento.getPrecioUnitario());
        setDescripcion(lineaDocumento.getDescripcion());
        setConsumible(lineaDocumento.getConsumible());
    }

    public double calcularSubtotal() {
        return this.cantidad * this.precioUnitario;
    }

    /**
     * Mismas reglas que los CHECK del script SQL:
     * cantidad > 0 y precio_unitario >= 0.
     */
    private static void validarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
    }

    private static void validarPrecioUnitario(double precioUnitario) {
        if (precioUnitario < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        }
    }

    /** descripcion es NOT NULL en el script SQL. */
    private static void validarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripcion no puede ser nula ni vacia");
        }
    }

    public int getIdLinea() { return idLinea; }
    public void setIdLinea(int idLinea) { this.idLinea = idLinea; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        validarCantidad(cantidad);
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) {
        validarPrecioUnitario(precioUnitario);
        this.precioUnitario = precioUnitario;
    }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) {
        validarDescripcion(descripcion);
        this.descripcion = descripcion;
    }

    public Consumible getConsumible() { return consumible; }
    public void setConsumible(Consumible consumible) { this.consumible = consumible; }
}
