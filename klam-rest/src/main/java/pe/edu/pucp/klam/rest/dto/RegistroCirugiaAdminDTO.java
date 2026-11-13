package pe.edu.pucp.klam.rest.dto;

/**
 * RF09: cuerpo de POST /admin/cirugias. Se registra siempre en estado PROGRAMADA.
 * Con tipoCliente = CLINICA_HOSPITAL, ese cliente es también la clínica destino.
 * El responsable de la auditoría NO viaja aquí: es el administrador de la sesión.
 */
public class RegistroCirugiaAdminDTO {
    private String fechaHoraInicio;   // ISO-8601, ej. 2026-11-20T08:00
    private String fechaHoraFin;      // opcional
    private String tipoProcedimiento;
    private String doctorNombre;
    private String diagnostico;
    private String tipoCliente;       // CLINICA_HOSPITAL | PACIENTE_PARTICULAR
    private int idCliente;
    private Integer idEquipo;         // opcional
    private Integer idBandeja;
    private java.util.Map<Integer, Integer> consumiblesPrevistos = new java.util.HashMap<>();
    public Integer getIdBandeja() { return idBandeja; }
    public void setIdBandeja(Integer idBandeja) { this.idBandeja = idBandeja; }
    public java.util.Map<Integer, Integer> getConsumiblesPrevistos() { return consumiblesPrevistos; }
    public void setConsumiblesPrevistos(java.util.Map<Integer, Integer> valor) {
        consumiblesPrevistos = valor == null ? new java.util.HashMap<>() : valor;
    }

    public RegistroCirugiaAdminDTO() {
    }

    public String getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(String fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }
    public String getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(String fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }
    public String getTipoProcedimiento() { return tipoProcedimiento; }
    public void setTipoProcedimiento(String tipoProcedimiento) { this.tipoProcedimiento = tipoProcedimiento; }
    public String getDoctorNombre() { return doctorNombre; }
    public void setDoctorNombre(String doctorNombre) { this.doctorNombre = doctorNombre; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public Integer getIdEquipo() { return idEquipo; }
    public void setIdEquipo(Integer idEquipo) { this.idEquipo = idEquipo; }
}
