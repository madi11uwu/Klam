package pe.edu.pucp.klam.rest.dto;

// Datos de una cirugía que se envían a Blazor (las fechas viajan como texto ISO)
public class CirugiaDTO {
    private int idCirugia;
    private String estado;
    private String fechaHoraInicio;
    private String fechaHoraFin;
    private String tipoProcedimiento;
    private String doctorNombre;
    private String diagnostico;
    private String motivoCancelacion;
    private String tipoCliente;
    private Integer idCliente;
    private String clienteNombre;
    private String pacienteNombre;
    private String clinicaNombre;
    private Integer idEquipo;
    private String equipoNombre;
    private Integer idBandeja;
    private String bandejaTipo;
    private Integer idSolicitudOrigen;
    private Double precioPactado;
    private String estadoCotizacion;
    private boolean activo;

    public CirugiaDTO() {
    }

    public int getIdCirugia() {
        return idCirugia;
    }

    public void setIdCirugia(int idCirugia) {
        this.idCirugia = idCirugia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(String fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public String getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(String fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public String getTipoProcedimiento() {
        return tipoProcedimiento;
    }

    public void setTipoProcedimiento(String tipoProcedimiento) {
        this.tipoProcedimiento = tipoProcedimiento;
    }

    public String getDoctorNombre() {
        return doctorNombre;
    }

    public void setDoctorNombre(String doctorNombre) {
        this.doctorNombre = doctorNombre;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getPacienteNombre() {
        return pacienteNombre;
    }

    public void setPacienteNombre(String pacienteNombre) {
        this.pacienteNombre = pacienteNombre;
    }

    public String getClinicaNombre() {
        return clinicaNombre;
    }

    public void setClinicaNombre(String clinicaNombre) {
        this.clinicaNombre = clinicaNombre;
    }

    public Integer getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Integer idEquipo) {
        this.idEquipo = idEquipo;
    }

    public String getEquipoNombre() {
        return equipoNombre;
    }

    public void setEquipoNombre(String equipoNombre) {
        this.equipoNombre = equipoNombre;
    }

    public Integer getIdBandeja() {
        return idBandeja;
    }

    public void setIdBandeja(Integer idBandeja) {
        this.idBandeja = idBandeja;
    }

    public String getBandejaTipo() {
        return bandejaTipo;
    }

    public void setBandejaTipo(String bandejaTipo) {
        this.bandejaTipo = bandejaTipo;
    }

    public Integer getIdSolicitudOrigen() {
        return idSolicitudOrigen;
    }

    public void setIdSolicitudOrigen(Integer idSolicitudOrigen) {
        this.idSolicitudOrigen = idSolicitudOrigen;
    }

    public Double getPrecioPactado() {
        return precioPactado;
    }

    public void setPrecioPactado(Double precioPactado) {
        this.precioPactado = precioPactado;
    }

    public String getEstadoCotizacion() {
        return estadoCotizacion;
    }

    public void setEstadoCotizacion(String estadoCotizacion) {
        this.estadoCotizacion = estadoCotizacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
