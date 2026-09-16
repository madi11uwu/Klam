package pe.edu.pucp.klam.modelo.documentacionfinanzas;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.interfaces.Facturable;

public abstract class DocumentoFacturacion implements Facturable {

    public static final double TASA_IGV_POR_DEFECTO = 0.18;

    private int idDocumento;
    private double montoBase;
    private double igv;
    private double tasaIgv;
    private double montoTotal;
    private LocalDateTime fechaEmision;
    private EstadoPago estadoPago;
    private Cirugia cirugia;
    private boolean activo;
    private List<LineaDocumento> lineas;

    public DocumentoFacturacion() {
        this.lineas = new ArrayList<>();
        this.tasaIgv = TASA_IGV_POR_DEFECTO;
        this.estadoPago = EstadoPago.PENDIENTE;
        this.activo = true;
    }

    public DocumentoFacturacion(Cirugia cirugia, LocalDateTime fechaEmision) {
        this();
        this.cirugia = cirugia;
        this.fechaEmision = fechaEmision;
    }

    /**
     * Cada documento concreto define que tipo de linea acepta.
     */
    protected abstract boolean esLineaValida(LineaDocumento linea);

    /**
     * Validacion comun de agregarLinea y setLineas. Revisa cantidad y precio
     * porque una linea creada con el constructor vacio queda con cantidad 0.
     */
    private void validarLinea(LineaDocumento linea) {
        if (linea == null) {
            throw new IllegalArgumentException("La linea no puede ser nula");
        }
        if (!esLineaValida(linea)) {
            throw new IllegalArgumentException(
                    "Tipo de linea no valido para un " + this.getClass().getSimpleName());
        }
        if (linea.getCantidad() <= 0) {
            throw new IllegalArgumentException("La linea debe tener cantidad mayor que cero");
        }
        if (linea.getPrecioUnitario() < 0) {
            throw new IllegalArgumentException("La linea no puede tener precio unitario negativo");
        }
    }

    public void agregarLinea(LineaDocumento linea) {
        validarLinea(linea);
        this.lineas.add(linea);
        this.calcularMontoTotal();
    }

    @Override
    public double calcularMontoTotal() {
        double base = 0.0;
        for (LineaDocumento linea : this.lineas) {
            base += linea.calcularSubtotal();
        }
        this.montoBase = base;
        this.igv = base * this.tasaIgv;
        this.montoTotal = this.montoBase + this.igv;
        return this.montoTotal;
    }

    public void anular() {
        this.estadoPago = EstadoPago.ANULADO;
    }

    public boolean estaAnulado() {
        return this.estadoPago == EstadoPago.ANULADO;
    }

    public int getIdDocumento() { return idDocumento; }
    public void setIdDocumento(int idDocumento) { this.idDocumento = idDocumento; }

    /**
     * Los montos se recalculan al leerlos: si alguien cambia la cantidad
     * o el precio de una linea ya agregada, el valor nunca queda viejo.
     */
    public double getMontoBase() {
        this.calcularMontoTotal();
        return montoBase;
    }
    // Valor derivado de las lineas: getMontoBase() lo recalcula,
    // asi que asignarlo directamente no tiene efecto.
    public void setMontoBase(double montoBase) { this.montoBase = montoBase; }

    public double getIgv() {
        this.calcularMontoTotal();
        return igv;
    }
    // Valor derivado de las lineas: getIgv() lo recalcula,
    // asi que asignarlo directamente no tiene efecto.
    public void setIgv(double igv) { this.igv = igv; }

    public double getTasaIgv() { return tasaIgv; }

    public void setTasaIgv(double tasaIgv) {
        if (tasaIgv < 0) {
            throw new IllegalArgumentException("La tasa de IGV no puede ser negativa");
        }
        this.tasaIgv = tasaIgv;
        this.calcularMontoTotal();
    }

    public double getMontoTotal() {
        return this.calcularMontoTotal();
    }
    // Valor derivado de las lineas: getMontoTotal() lo recalcula,
    // asi que asignarlo directamente no tiene efecto.
    public void setMontoTotal(double montoTotal) { this.montoTotal = montoTotal; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    public EstadoPago getEstadoPago() { return estadoPago; }

    /**
     * Un documento anulado no vuelve a otro estado: se corrige emitiendo
     * una nota de credito, no reabriendo el comprobante.
     */
    public void setEstadoPago(EstadoPago estadoPago) {
        if (estadoPago == null) {
            throw new IllegalArgumentException("El estado de pago no puede ser nulo");
        }
        if (this.estadoPago == EstadoPago.ANULADO && estadoPago != EstadoPago.ANULADO) {
            throw new IllegalStateException(
                    "Un documento anulado no puede cambiar de estado de pago");
        }
        if (this.estadoPago == EstadoPago.PAGADO && estadoPago == EstadoPago.PENDIENTE) {
            throw new IllegalStateException(
                    "Un documento pagado no vuelve a estado pendiente");
        }
        this.estadoPago = estadoPago;
    }

    /** La relacion con la cirugia se modela con la referencia, no con su id. */
    public Cirugia getCirugia() { return cirugia; }
    public void setCirugia(Cirugia cirugia) { this.cirugia = cirugia; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public List<LineaDocumento> getLineas() { return new ArrayList<>(lineas); }
    /**
     * Reemplaza el detalle completo. Cada linea pasa por la misma
     * validacion que agregarLinea, y el total se recalcula.
     */
    public void setLineas(List<LineaDocumento> lineas) {
        List<LineaDocumento> nuevas = new ArrayList<>();
        if (lineas != null) {
            for (LineaDocumento linea : lineas) {
                validarLinea(linea);
                nuevas.add(linea);
            }
        }
        // Solo se reemplaza el detalle si todas las lineas son validas:
        // un rechazo no debe dejar el documento a medias.
        this.lineas = nuevas;
        this.calcularMontoTotal();
    }
}