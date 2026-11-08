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
        validarPrecioPactado(precioPactado);
        this.cirugia = cirugia;
        this.precioPactado = precioPactado;
        this.fechaEmision = fechaEmision;
    }

    public Cotizacion(final Cotizacion cotizacion) {
        if (cotizacion == null) {
            throw new IllegalArgumentException("cotizacion no puede ser nula");
        }
        this();
        setIdCotizacion(cotizacion.getIdCotizacion());
        setPrecioPactado(cotizacion.getPrecioPactado());
        setEstado(cotizacion.getEstado());
        setFechaEmision(cotizacion.getFechaEmision());
        setCirugia(cotizacion.getCirugia());
        setActivo(cotizacion.isActivo());
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

    public Cirugia getCirugia() { return cirugia; }
    public void setCirugia(Cirugia cirugia) { this.cirugia = cirugia; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}