package pe.edu.pucp.klam.rest.dto;

/**
 * RF09: cuerpo de POST /cirugias (el administrador registra una cirugía directamente).
 * Se registra siempre en estado PROGRAMADA. La clínica destino es el cliente cuando
 * tipoCliente = CLINICA_HOSPITAL (el modelo Cirugia no guarda otra clínica).
 * El responsable de la auditoría NO viaja aquí: el servidor lo toma de la sesión (token).
 */
public class CirugiaRequestDTO {
    private String tipoCliente;         // CLINICA_HOSPITAL | PACIENTE_PARTICULAR
    private Integer idCliente;
    private String dni;                 // solo PACIENTE_PARTICULAR
    private String diagnostico;
    private String tipoProcedimiento;
    private String doctorNombre;
    private String fechaHoraInicio;     // ISO-8601
    private String fechaHoraFin;        // ISO-8601, opcional
    private Integer idEquipo;           // opcional

    public CirugiaRequestDTO() {
    }

    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }
    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getTipoProcedimiento() { return tipoProcedimiento; }
    public void setTipoProcedimiento(String tipoProcedimiento) { this.tipoProcedimiento = tipoProcedimiento; }
    public String getDoctorNombre() { return doctorNombre; }
    public void setDoctorNombre(String doctorNombre) { this.doctorNombre = doctorNombre; }
    public String getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(String fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }
    public String getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(String fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }
    public Integer getIdEquipo() { return idEquipo; }
    public void setIdEquipo(Integer idEquipo) { this.idEquipo = idEquipo; }
}
