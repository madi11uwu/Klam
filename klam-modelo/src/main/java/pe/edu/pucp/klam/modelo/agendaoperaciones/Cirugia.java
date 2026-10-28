package pe.edu.pucp.klam.modelo.agendaoperaciones;

import java.time.LocalDateTime;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.interfaces.Agendable;
import pe.edu.pucp.klam.modelo.interfaces.Cancelable;

public class Cirugia implements Agendable, Cancelable {
    private int idCirugia;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private String tipoProcedimiento;
    private String motivoCancelacion;
    private String doctorNombre;
    private String diagnostico;
    private EstadoCirugia estado;
    private Equipo equipo;
    private BandejaInstrumental bandejaInstrumental;
    private Cliente cliente;

    // Solicitud aprobada de la que nace la cirugia; null si el administrador la registro directamente
    private Integer idSolicitudOrigen;

    // true cuando el administrador cerro el registro de consumos (RF15)
    private boolean consumoCompleto;

    // estado = ciclo de vida de la cirugia; activo = eliminacion logica
    private boolean activo;

    public Cirugia() {
        this.estado = EstadoCirugia.PROGRAMADA;
        this.activo = true;
    }

    public Cirugia(final Cirugia cirugia) {
        if (cirugia == null) {
            throw new IllegalArgumentException("cirugia no puede ser nula");
        }
        setIdCirugia(cirugia.getIdCirugia());
        setFechaHoraInicio(cirugia.getFechaHoraInicio());
        setFechaHoraFin(cirugia.getFechaHoraFin());
        setTipoProcedimiento(cirugia.getTipoProcedimiento());
        setMotivoCancelacion(cirugia.getMotivoCancelacion());
        setDoctorNombre(cirugia.getDoctorNombre());
        setDiagnostico(cirugia.getDiagnostico());
        setEstado(cirugia.getEstado());
        setEquipo(cirugia.getEquipo());
        setBandejaInstrumental(cirugia.getBandejaInstrumental());
        setCliente(cirugia.getCliente());
        setIdSolicitudOrigen(cirugia.getIdSolicitudOrigen());
        setConsumoCompleto(cirugia.isConsumoCompleto());
        setActivo(cirugia.isActivo());
    }

    public int getIdCirugia() {
        return idCirugia;
    }

    public void setIdCirugia(int idCirugia) {
        if (idCirugia < 0) {
            throw new IllegalArgumentException("id no puede ser negativo");
        }
        this.idCirugia = idCirugia;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
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

    public EstadoCirugia getEstado() {
        return estado;
    }

    public void setEstado(EstadoCirugia estado) {
        if (estado == null) {
            throw new IllegalArgumentException("estado no puede ser nulo");
        }
        this.estado = estado;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public BandejaInstrumental getBandejaInstrumental() {
        return bandejaInstrumental;
    }

    public void setBandejaInstrumental(BandejaInstrumental bandejaInstrumental) {
        this.bandejaInstrumental = bandejaInstrumental;
    }

    /** Clinica/hospital o paciente particular al que pertenece la cirugia. */
    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    /** Eliminacion logica: la cirugia se conserva aunque se dé de baja. */
    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    //metodos de la interfaz Agendable, Cancelable
    @Override
    public String getIdentificador() {
        return String.valueOf(idCirugia);
    }

    @Override
    public void cancelar(String motivo) {
        this.estado = EstadoCirugia.CANCELADA;
        this.motivoCancelacion = motivo;
    }
}
