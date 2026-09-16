package pe.edu.pucp.klam.modelo.documentacionfinanzas;

import java.time.LocalDateTime;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;

public class Cotizacion {

    private int idCotizacion;
    private double precioPactado;
    private EstadoCotizacion estado;
    private LocalDateTime fechaEmision;
    private Cirugia cirugia;
    private boolean activo;

    public Cotizacion() {
        this.estado = EstadoCotizacion.EMITIDA;
        this.activo = true;
    }

    public Cotizacion(Cirugia cirugia, double precioPactado, LocalDateTime fechaEmision) {
        this();
        validarPrecioPactado(precioPactado);
        this.cirugia = cirugia;
        this.precioPactado = precioPactado;
        this.fechaEmision = fechaEmision;
    }

    public void aceptar()  { this.transitar(EstadoCotizacion.ACEPTADA); }
    public void rechazar() { this.transitar(EstadoCotizacion.RECHAZADA); }
    public void vencer()   { this.transitar(EstadoCotizacion.VENCIDA); }

    /**
     * Una cotizacion solo se resuelve una vez: desde EMITIDA pasa a
     * ACEPTADA, RECHAZADA o VENCIDA, y de ahi no se mueve.
     */
    private void transitar(EstadoCotizacion nuevo) {
        if (this.estado != EstadoCotizacion.EMITIDA) {
            throw new IllegalStateException(
                    "La cotizacion ya esta " + this.estado + " y no puede pasar a " + nuevo);
        }
        this.estado = nuevo;
    }

    public boolean estaResuelta() {
        return this.estado != EstadoCotizacion.EMITIDA;
    }

    public int getIdCotizacion() { return idCotizacion; }
    public void setIdCotizacion(int idCotizacion) { this.idCotizacion = idCotizacion; }

    public double getPrecioPactado() { return precioPactado; }
    public void setPrecioPactado(double precioPactado) {
        validarPrecioPactado(precioPactado);
        this.precioPactado = precioPactado;
    }

    /** Misma regla que el CHECK del script SQL: precio_pactado >= 0. */
    private static void validarPrecioPactado(double precioPactado) {
        if (precioPactado < 0) {
            throw new IllegalArgumentException("El precio pactado no puede ser negativo");
        }
    }

    public EstadoCotizacion getEstado() { return estado; }
    public void setEstado(EstadoCotizacion estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado de la cotizacion no puede ser nulo");
        }
        if (this.estaResuelta() && estado != this.estado) {
            throw new IllegalStateException(
                    "La cotizacion ya esta " + this.estado + " y no puede cambiar de estado");
        }
        this.estado = estado;
    }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    /** La relacion con la cirugia se modela con la referencia, no con su id. */
    public Cirugia getCirugia() { return cirugia; }
    public void setCirugia(Cirugia cirugia) { this.cirugia = cirugia; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "Cotizacion{id=" + idCotizacion + ", precio=" + precioPactado
                + ", estado=" + estado
                + ", cirugia=" + (cirugia == null ? "sin cirugia" : cirugia.getId_cirugia()) + "}";
    }
}