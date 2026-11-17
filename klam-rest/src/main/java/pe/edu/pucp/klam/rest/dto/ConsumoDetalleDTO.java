package pe.edu.pucp.klam.rest.dto;

import java.util.ArrayList;
import java.util.List;

// Detalle de consumo de una cirugía (RF15). Es la fuente única de consumo para los reportes RF18/RF19.
public class ConsumoDetalleDTO extends ConsumoCirugiaDTO {
    private String doctorNombre;
    private String pacienteNombre;
    private String clinicaNombre;
    private String archivoOrdenCompra;
    private String motivoRechazoOrdenCompra;
    private int totalDespachado;
    private int totalConsumido;
    private int diferencia;
    private List<ConsumoLineaDTO> consumibles = new ArrayList<>();

    public ConsumoDetalleDTO() {
    }

    public String getDoctorNombre() { return doctorNombre; }
    public void setDoctorNombre(String doctorNombre) { this.doctorNombre = doctorNombre; }

    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; }

    public String getClinicaNombre() { return clinicaNombre; }
    public void setClinicaNombre(String clinicaNombre) { this.clinicaNombre = clinicaNombre; }

    public String getArchivoOrdenCompra() { return archivoOrdenCompra; }
    public void setArchivoOrdenCompra(String archivoOrdenCompra) { this.archivoOrdenCompra = archivoOrdenCompra; }

    public String getMotivoRechazoOrdenCompra() { return motivoRechazoOrdenCompra; }
    public void setMotivoRechazoOrdenCompra(String motivoRechazoOrdenCompra) { this.motivoRechazoOrdenCompra = motivoRechazoOrdenCompra; }

    public int getTotalDespachado() { return totalDespachado; }
    public void setTotalDespachado(int totalDespachado) { this.totalDespachado = totalDespachado; }

    public int getTotalConsumido() { return totalConsumido; }
    public void setTotalConsumido(int totalConsumido) { this.totalConsumido = totalConsumido; }

    public int getDiferencia() { return diferencia; }
    public void setDiferencia(int diferencia) { this.diferencia = diferencia; }

    public List<ConsumoLineaDTO> getConsumibles() { return consumibles; }
    public void setConsumibles(List<ConsumoLineaDTO> consumibles) { this.consumibles = consumibles; }
}
