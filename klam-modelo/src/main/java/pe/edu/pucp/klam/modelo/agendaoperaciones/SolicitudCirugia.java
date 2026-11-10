package pe.edu.pucp.klam.modelo.agendaoperaciones;

import java.time.LocalDateTime;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;

public class SolicitudCirugia {

    private int idSolicitud;
    private Cliente cliente;
    private ClinicaHospital clinicaDestino;
    private Vendedor vendedorRegistrador;
    private String pacienteNombre;
    private String tipoProcedimiento;
    private String doctorNombre;
    private String diagnostico;
    private LocalDateTime fechaHoraSolicitada;
    private String observaciones;
    private EstadoSolicitudCirugia estado;
    private String motivoRechazo;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRespuesta;

    public SolicitudCirugia() {
        this.estado = EstadoSolicitudCirugia.POR_REVISAR;
    }

    public SolicitudCirugia(SolicitudCirugia solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException(
                    "La solicitud no puede ser nula");
        }

        this.idSolicitud = solicitud.idSolicitud;
        this.cliente = solicitud.cliente;
        this.clinicaDestino = solicitud.clinicaDestino;
        this.vendedorRegistrador = solicitud.vendedorRegistrador;
        this.pacienteNombre = solicitud.pacienteNombre;
        this.tipoProcedimiento = solicitud.tipoProcedimiento;
        this.doctorNombre = solicitud.doctorNombre;
        this.diagnostico = solicitud.diagnostico;
        this.fechaHoraSolicitada = solicitud.fechaHoraSolicitada;
        this.observaciones = solicitud.observaciones;
        this.estado = solicitud.estado;
        this.motivoRechazo = solicitud.motivoRechazo;
        this.fechaSolicitud = solicitud.fechaSolicitud;
        this.fechaRespuesta = solicitud.fechaRespuesta;
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public ClinicaHospital getClinicaDestino() {
        return clinicaDestino;
    }

    public void setClinicaDestino(ClinicaHospital clinicaDestino) {
        this.clinicaDestino = clinicaDestino;
    }

    public Vendedor getVendedorRegistrador() {
        return vendedorRegistrador;
    }

    public void setVendedorRegistrador(Vendedor vendedorRegistrador) {
        this.vendedorRegistrador = vendedorRegistrador;
    }

    public String getPacienteNombre() {
        return pacienteNombre;
    }

    public void setPacienteNombre(String pacienteNombre) {
        this.pacienteNombre = pacienteNombre;
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

    public LocalDateTime getFechaHoraSolicitada() {
        return fechaHoraSolicitada;
    }

    public void setFechaHoraSolicitada(
            LocalDateTime fechaHoraSolicitada) {
        this.fechaHoraSolicitada = fechaHoraSolicitada;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public EstadoSolicitudCirugia getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitudCirugia estado) {
        if (estado == null) {
            throw new IllegalArgumentException(
                    "El estado no puede ser nulo");
        }
        this.estado = estado;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }
}
