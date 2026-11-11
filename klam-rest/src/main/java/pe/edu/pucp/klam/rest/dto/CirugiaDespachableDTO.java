package pe.edu.pucp.klam.rest.dto;

import java.util.Map;

// Cirugía programada o en curso con el equipo y el kit que se despacharían. Si no se puede
// despachar, "motivo" explica por qué (equipo fuera, despacho pendiente, sin recursos...).
public class CirugiaDespachableDTO {
    private int idCirugia;
    private String procedimiento;
    private String cliente;
    private String clinica;
    private String fechaHoraInicio;
    private String estado;
    private Integer idEquipo;
    private String equipo;
    private Boolean equipoDisponible;
    private Integer idBandeja;
    private String kit;
    private boolean despachable;
    private String motivo;

    public CirugiaDespachableDTO() {
    }

    public CirugiaDespachableDTO(Map<String, Object> cirugia, Boolean equipoDisponible, String motivo) {
        if (cirugia != null) {
            idCirugia = DespachoDTO.entero(cirugia.get("idCirugia"), 0);
            procedimiento = DespachoDTO.texto(cirugia.get("tipoProcedimiento"));
            cliente = DespachoDTO.texto(cirugia.get("clienteNombre"));
            clinica = DespachoDTO.texto(cirugia.get("clinicaNombre"));
            fechaHoraInicio = DespachoDTO.fecha(cirugia.get("fechaHoraInicio"));
            estado = DespachoDTO.texto(cirugia.get("estado"));
            idEquipo = DespachoDTO.enteroONulo(cirugia.get("idEquipo"));
            equipo = DespachoDTO.texto(cirugia.get("equipoNombre"));
            idBandeja = DespachoDTO.enteroONulo(cirugia.get("idBandeja"));
            kit = DespachoDTO.texto(cirugia.get("bandejaTipo"));
        }
        this.equipoDisponible = equipoDisponible;
        this.motivo = motivo;
        this.despachable = motivo == null;
    }

    public int getIdCirugia() { return idCirugia; }
    public void setIdCirugia(int idCirugia) { this.idCirugia = idCirugia; }

    public String getProcedimiento() { return procedimiento; }
    public void setProcedimiento(String procedimiento) { this.procedimiento = procedimiento; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getClinica() { return clinica; }
    public void setClinica(String clinica) { this.clinica = clinica; }

    public String getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(String fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getIdEquipo() { return idEquipo; }
    public void setIdEquipo(Integer idEquipo) { this.idEquipo = idEquipo; }

    public String getEquipo() { return equipo; }
    public void setEquipo(String equipo) { this.equipo = equipo; }

    public Boolean getEquipoDisponible() { return equipoDisponible; }
    public void setEquipoDisponible(Boolean equipoDisponible) { this.equipoDisponible = equipoDisponible; }

    public Integer getIdBandeja() { return idBandeja; }
    public void setIdBandeja(Integer idBandeja) { this.idBandeja = idBandeja; }

    public String getKit() { return kit; }
    public void setKit(String kit) { this.kit = kit; }

    public boolean isDespachable() { return despachable; }
    public void setDespachable(boolean despachable) { this.despachable = despachable; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
