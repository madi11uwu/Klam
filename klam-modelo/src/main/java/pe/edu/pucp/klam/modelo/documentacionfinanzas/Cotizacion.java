package pe.edu.pucp.klam.modelo.documentacionfinanzas;

import java.time.LocalDateTime;

public class Cotizacion {

    private int idCotizacion;
    private double precioPactado;
    private EstadoCotizacion estado;
    private LocalDateTime fechaEmision;
    private int idCirugia;
    private boolean activo;

    public Cotizacion() {
        this.estado = EstadoCotizacion.EMITIDA;
        this.activo = true;
    }

    public Cotizacion(int idCirugia, double precioPactado, LocalDateTime fechaEmision) {
        this();
        this.idCirugia = idCirugia;
        this.precioPactado = precioPactado;
        this.fechaEmision = fechaEmision;
    }

    public void aceptar() { this.estado = EstadoCotizacion.ACEPTADA; }
    public void rechazar() { this.estado = EstadoCotizacion.RECHAZADA; }

    public int getIdCotizacion() { return idCotizacion; }
    public void setIdCotizacion(int idCotizacion) { this.idCotizacion = idCotizacion; }

    public double getPrecioPactado() { return precioPactado; }
    public void setPrecioPactado(double precioPactado) { this.precioPactado = precioPactado; }

    public EstadoCotizacion getEstado() { return estado; }
    public void setEstado(EstadoCotizacion estado) { this.estado = estado; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    public int getIdCirugia() { return idCirugia; }
    public void setIdCirugia(int idCirugia) { this.idCirugia = idCirugia; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "Cotizacion{id=" + idCotizacion + ", precio=" + precioPactado
                + ", estado=" + estado + ", cirugia=" + idCirugia + "}";
    }
}
