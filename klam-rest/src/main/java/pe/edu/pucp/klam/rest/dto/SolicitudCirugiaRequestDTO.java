package pe.edu.pucp.klam.rest.dto;

/**
 * RF05: cuerpo de POST /clientes/{tipo}/{id}/solicitudes.
 * El solicitante NO viaja aquí: sale de la ruta ({tipo} = CLINICA_HOSPITAL | PACIENTE_PARTICULAR).
 * La fecha y hora viajan juntas en ISO-8601 (ej. "2026-11-12T09:30"); Blazor une Fecha + Hora.
 * La validación de negocio (obligatorios, longitudes, DNI, cliente existente) la hace la BL.
 */
public class SolicitudCirugiaRequestDTO {
    private Integer idClinicaDestino;   // opcional si el solicitante es una clínica (usa su propia sede)
    private String pacienteNombre;      // opcional para particular (usa su nombre)
    private String dni;                 // obligatorio solo para PACIENTE_PARTICULAR
    private String diagnostico;
    private String tipoProcedimiento;
    private String doctorNombre;
    private String fechaHoraSolicitada;
    private String observaciones;

    public SolicitudCirugiaRequestDTO() {
    }

    public Integer getIdClinicaDestino() { return idClinicaDestino; }
    public void setIdClinicaDestino(Integer idClinicaDestino) { this.idClinicaDestino = idClinicaDestino; }
    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
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
