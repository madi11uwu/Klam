package pe.edu.pucp.klam.modelo.agendaoperaciones;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import pe.edu.pucp.klam.modelo.clientes.Cliente;

public class Cirugia {
    private int idCirugia;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private String tipoProcedimiento;
    private String doctorNombre;
    private String diagnostico;
    private EstadoCirugia estado;
    private String motivoCancelacion;
    private Equipo equipo;
    private BandejaInstrumental bandejaInstrumental;
    private Map<Integer, Integer> consumiblesPrevistos = new HashMap<>();
    private Cliente cliente;

    // Solicitud aprobada de la que nace la cirugia; null si el administrador la registro directamente
    private SolicitudCirugia solicitudOrigen;

    // true cuando el administrador cerro el registro de consumos (RF15)
    private boolean consumoCompleto;

    // estado = ciclo de vida de la cirugia; activo = eliminacion logica
    private boolean activo;

    public Cirugia() {
        this.estado = EstadoCirugia.PROGRAMADA;
        this.activo = true;
    }
    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }
    public Cirugia(final Cirugia cirugia) {
        if (cirugia == null) {
            throw new IllegalArgumentException("cirugia no puede ser nula");
        }
        setIdCirugia(cirugia.getIdCirugia());
        setFechaHoraInicio(cirugia.getFechaHoraInicio());
        setFechaHoraFin(cirugia.getFechaHoraFin());
        setTipoProcedimiento(cirugia.getTipoProcedimiento());
        setDoctorNombre(cirugia.getDoctorNombre());
        setDiagnostico(cirugia.getDiagnostico());
        setEstado(cirugia.getEstado());
        setEquipo(cirugia.getEquipo());
        setBandejaInstrumental(cirugia.getBandejaInstrumental());
        setConsumiblesPrevistos(cirugia.getConsumiblesPrevistos());
        setCliente(cirugia.getCliente());
        setSolicitudOrigen(cirugia.getSolicitudOrigen());
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

    public SolicitudCirugia getSolicitudOrigen() {
        return solicitudOrigen;
    }

    public void setSolicitudOrigen(SolicitudCirugia solicitudOrigen) {
        this.solicitudOrigen = solicitudOrigen;
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

    public Map<Integer, Integer> getConsumiblesPrevistos() {
        return new HashMap<>(consumiblesPrevistos);
    }

    public void setConsumiblesPrevistos(Map<Integer, Integer> consumiblesPrevistos) {
        this.consumiblesPrevistos = consumiblesPrevistos == null
                ? new HashMap<>() : new HashMap<>(consumiblesPrevistos);
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
}
