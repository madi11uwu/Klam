package pe.edu.pucp.klam.rest.dto;

// Cuerpo de POST /despachos. El equipo y la bandeja salen de la cirugía, no del cliente.
public class NuevoDespachoDTO {
    private Integer idCirugia;

    public NuevoDespachoDTO() {
    }

    public NuevoDespachoDTO(Integer idCirugia) {
        this.idCirugia = idCirugia;
    }

    public Integer getIdCirugia() {
        return idCirugia;
    }

    public void setIdCirugia(Integer idCirugia) {
        this.idCirugia = idCirugia;
    }
}
