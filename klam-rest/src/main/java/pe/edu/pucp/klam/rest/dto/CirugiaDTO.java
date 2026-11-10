package pe.edu.pucp.klam.rest.dto;

/**
 * DTO espejo de Cirugia. Se usa para registrar y modificar (toModelo) y como respuesta con objetos anidados.
 * Para calendario, listas e historial usar CirugiaResumenDTO.
 */
public class CirugiaDTO {
    private int idCirugia;
    private boolean activo;
    private String fechaHoraInicio;
    private String fechaHoraFin;
    private String tipoProcedimiento;
    private String motivoCancelacion;
    private String doctorNombre;
    private String diagnostico;
    private String estado;
    private Integer idSolicitudOrigen;
    private boolean consumoCompleto;
    private EquipoDTO equipo;
    private BandejaInstrumentalDTO bandejaInstrumental;
    private ClienteDTO cliente;

    public CirugiaDTO() {
    }

    public int getIdCirugia() {
        return idCirugia;
    }

    public void setIdCirugia(int idCirugia) {
        this.idCirugia = idCirugia;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
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

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getIdSolicitudOrigen() {
        return idSolicitudOrigen;
    }

    public void setIdSolicitudOrigen(Integer idSolicitudOrigen) {
        this.idSolicitudOrigen = idSolicitudOrigen;
    }

    public boolean isConsumoCompleto() {
        return consumoCompleto;
    }

    public void setConsumoCompleto(boolean consumoCompleto) {
        this.consumoCompleto = consumoCompleto;
    }

    public EquipoDTO getEquipo() {
        return equipo;
    }

    public void setEquipo(EquipoDTO equipo) {
        this.equipo = equipo;
    }

    public BandejaInstrumentalDTO getBandejaInstrumental() {
        return bandejaInstrumental;
    }

    public void setBandejaInstrumental(BandejaInstrumentalDTO bandejaInstrumental) {
        this.bandejaInstrumental = bandejaInstrumental;
    }

    public ClienteDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
    }
}
