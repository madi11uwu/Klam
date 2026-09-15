package pe.edu.pucp.klam.modelo.documentacionfinanzas;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import pe.edu.pucp.klam.modelo.interfaces.Facturable;

public class NotaCredito implements Facturable {

    private int idNotaCredito;
    private String motivo;
    private LocalDateTime fechaEmision;
    private double montoTotal;
    private boolean activo;
    private DocumentoFacturacion documentoOriginal;
    private List<LineaNotaCredito> lineas;

    public NotaCredito() {
        this.lineas = new ArrayList<>();
        this.activo = true;
    }

    public NotaCredito(DocumentoFacturacion documentoOriginal, String motivo, LocalDateTime fechaEmision) {
        this();
        this.documentoOriginal = documentoOriginal;
        this.motivo = motivo;
        this.fechaEmision = fechaEmision;
    }

    public void agregarLinea(LineaNotaCredito linea) {
        if (linea == null) {
            throw new IllegalArgumentException("La linea no puede ser nula");
        }
        this.lineas.add(linea);
        this.calcularMontoTotal();
    }

    @Override
    public double calcularMontoTotal() {
        double total = 0.0;
        for (LineaNotaCredito linea : this.lineas) {
            total += linea.calcularSubtotal();
        }
        this.montoTotal = total;
        return this.montoTotal;
    }

    public boolean excedeAlDocumentoOriginal() {
        if (this.documentoOriginal == null) {
            return false;
        }
        // Se comparan montos sin IGV: las lineas de la nota suman base,
        // igual que el monto base del documento original.
        return this.calcularMontoTotal() > this.documentoOriginal.getMontoBase();
    }

    public int getIdNotaCredito() { return idNotaCredito; }
    public void setIdNotaCredito(int idNotaCredito) { this.idNotaCredito = idNotaCredito; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    /**
     * El total se recalcula al leerlo: si alguien cambia la cantidad o el
     * precio de una linea ya agregada, el valor nunca queda viejo.
     */
    public double getMontoTotal() {
        return this.calcularMontoTotal();
    }
    public void setMontoTotal(double montoTotal) { this.montoTotal = montoTotal; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public DocumentoFacturacion getDocumentoOriginal() { return documentoOriginal; }
    public void setDocumentoOriginal(DocumentoFacturacion documentoOriginal) {
        this.documentoOriginal = documentoOriginal;
    }

    public List<LineaNotaCredito> getLineas() { return new ArrayList<>(lineas); }
    /**
     * Reemplaza el detalle completo. Cada linea pasa por la misma
     * validacion que agregarLinea, y el total se recalcula.
     */
    public void setLineas(List<LineaNotaCredito> lineas) {
        List<LineaNotaCredito> nuevas = new ArrayList<>();
        if (lineas != null) {
            for (LineaNotaCredito linea : lineas) {
                if (linea == null) {
                    throw new IllegalArgumentException("La linea no puede ser nula");
                }
                nuevas.add(linea);
            }
        }
        // Solo se reemplaza el detalle si todas las lineas son validas:
        // un rechazo no debe dejar la nota a medias.
        this.lineas = nuevas;
        this.calcularMontoTotal();
    }

    @Override
    public String toString() {
        String ref = (documentoOriginal == null) ? "sin documento" : documentoOriginal.toString();
        return "NotaCredito{id=" + idNotaCredito + ", motivo=" + motivo
                + ", monto=" + montoTotal + ", corrige=" + ref + "}";
    }
}