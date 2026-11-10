package pe.edu.pucp.klam.rest.dto;

public class RegistrarSolicitudDTO {
    private String tipoCliente;
    private int idCliente;
    private int idClinicaDestino;
    private String pacienteNombre;
    private String diagnostico;
    private String tipoProcedimiento;
    private String doctorNombre;
    private String fechaHoraSolicitada;
    private String observaciones;

    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public int getIdClinicaDestino() { return idClinicaDestino; }
    public void setIdClinicaDestino(int idClinicaDestino) { this.idClinicaDestino = idClinicaDestino; }
    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getTipoProcedimiento() { return tipoProcedimiento; }
    public void setTipoProcedimiento(String tipoProcedimiento) { this.tipoProcedimiento = tipoProcedimiento; }
    public String getDoctorNombre() { return doctorNombre; }
    public void setDoctorNombre(String doctorNombre) { this.doctorNombre = doctorNombre; }
    public String getFechaHoraSolicitada() { return fechaHoraSolicitada; }
    public void setFechaHoraSolicitada(String fechaHoraSolicitada) { this.fechaHoraSolicitada = fechaHoraSolicitada; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
