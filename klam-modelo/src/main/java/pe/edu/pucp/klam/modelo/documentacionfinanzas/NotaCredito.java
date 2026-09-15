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

    public double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(double montoTotal) { this.montoTotal = montoTotal; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public DocumentoFacturacion getDocumentoOriginal() { return documentoOriginal; }
    public void setDocumentoOriginal(DocumentoFacturacion documentoOriginal) {
        this.documentoOriginal = documentoOriginal;
    }

    public List<LineaNotaCredito> getLineas() { return new ArrayList<>(lineas); }
    public void setLineas(List<LineaNotaCredito> lineas) {
        this.lineas = (lineas == null) ? new ArrayList<>() : new ArrayList<>(lineas);
    }

    @Override
    public String toString() {
        String ref = (documentoOriginal == null) ? "sin documento" : documentoOriginal.toString();
        return "NotaCredito{id=" + idNotaCredito + ", motivo=" + motivo
                + ", monto=" + montoTotal + ", corrige=" + ref + "}";
    }
}