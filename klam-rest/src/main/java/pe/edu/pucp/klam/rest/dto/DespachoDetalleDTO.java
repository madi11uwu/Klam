package pe.edu.pucp.klam.rest.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Detalle de un despacho: datos de la lista + paciente, médico, cirugía y consumibles del kit.
public class DespachoDetalleDTO extends DespachoDTO {
    private String paciente;
    private String doctor;
    private String fechaCirugia;
    private String estadoCirugia;
    private List<ConsumibleDespachadoDTO> consumibles = new ArrayList<>();

    public DespachoDetalleDTO() {
        super();
    }

    public DespachoDetalleDTO(Map<String, Object> despacho, Map<String, Object> cirugia,
                              List<ConsumibleDespachadoDTO> consumibles) {
        super(despacho, cirugia);
        paciente = cirugia == null ? null : texto(cirugia.get("pacienteNombre"));
        doctor = cirugia == null ? null : texto(cirugia.get("doctorNombre"));
        fechaCirugia = cirugia == null ? null : fecha(cirugia.get("fechaHoraInicio"));
        estadoCirugia = cirugia == null ? null : texto(cirugia.get("estado"));
        this.consumibles = consumibles != null ? consumibles : new ArrayList<>();
    }

    public String getPaciente() { return paciente; }
    public void setPaciente(String paciente) { this.paciente = paciente; }

    public String getDoctor() { return doctor; }
    public void setDoctor(String doctor) { this.doctor = doctor; }

    public String getFechaCirugia() { return fechaCirugia; }
    public void setFechaCirugia(String fechaCirugia) { this.fechaCirugia = fechaCirugia; }

    public String getEstadoCirugia() { return estadoCirugia; }
    public void setEstadoCirugia(String estadoCirugia) { this.estadoCirugia = estadoCirugia; }

    public List<ConsumibleDespachadoDTO> getConsumibles() { return consumibles; }
    public void setConsumibles(List<ConsumibleDespachadoDTO> consumibles) { this.consumibles = consumibles; }
}
