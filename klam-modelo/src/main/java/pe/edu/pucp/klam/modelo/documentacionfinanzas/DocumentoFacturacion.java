package pe.edu.pucp.klam.modelo.documentacionfinanzas;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private int idCirugia;
    private boolean activo;
    private List<LineaDocumento> lineas;

    public DocumentoFacturacion() {
        this.lineas = new ArrayList<>();
        this.tasaIgv = TASA_IGV_POR_DEFECTO;
        this.estadoPago = EstadoPago.PENDIENTE;
        this.activo = true;
    }

    public DocumentoFacturacion(int idCirugia, LocalDateTime fechaEmision) {
        this();
        this.idCirugia = idCirugia;
        this.fechaEmision = fechaEmision;
    }

    /**
     * Cada documento concreto define que tipo de linea acepta.
     * */
    protected abstract boolean esLineaValida(LineaDocumento linea);

    public void agregarLinea(LineaDocumento linea) {
        if (linea == null) {
            throw new IllegalArgumentException("La linea no puede ser nula");
        }
        if (!esLineaValida(linea)) {
            throw new IllegalArgumentException(
                    "Tipo de linea no valido para un " + this.getClass().getSimpleName());
        }
        this.lineas.add(linea);
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

    public int getIdDocumento() { return idDocumento; }
    public void setIdDocumento(int idDocumento) { this.idDocumento = idDocumento; }

    public double getMontoBase() { return montoBase; }
    public void setMontoBase(double montoBase) { this.montoBase = montoBase; }

    public double getIgv() { return igv; }
    public void setIgv(double igv) { this.igv = igv; }

    public double getTasaIgv() { return tasaIgv; }
    public void setTasaIgv(double tasaIgv) { this.tasaIgv = tasaIgv; }

    public double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(double montoTotal) { this.montoTotal = montoTotal; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    public EstadoPago getEstadoPago() { return estadoPago; }
    public void setEstadoPago(EstadoPago estadoPago) { this.estadoPago = estadoPago; }

    public int getIdCirugia() { return idCirugia; }
    public void setIdCirugia(int idCirugia) { this.idCirugia = idCirugia; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public List<LineaDocumento> getLineas() { return new ArrayList<>(lineas); }
    public void setLineas(List<LineaDocumento> lineas) {
        this.lineas = (lineas == null) ? new ArrayList<>() : new ArrayList<>(lineas);
    }
}