package pe.edu.pucp.klam.rest.dto;

/** Entrada de la bitacora de cirugias (RF16). */
public class AuditoriaCirugiaDTO {

    private int idAuditoria;
    private int idCirugia;
    private String fechaHora;
    private String accion;
    private String responsable;
    private String detalle;

    public AuditoriaCirugiaDTO() {
    }

    public int getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(int idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public int getIdCirugia() {
        return idCirugia;
    }

    public void setIdCirugia(int idCirugia) {
        this.idCirugia = idCirugia;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }
}
