package pe.edu.pucp.klam.rest.dto;

import pe.edu.pucp.klam.modelo.agendaoperaciones.SolicitudCirugia;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;

// Datos de presentación: evita serializar el grafo del modelo y sus relaciones.
public class SolicitudCirugiaDTO {
    private final int idSolicitud;
    private final Integer idVendedorRegistrador;
    private final int idCliente;
    private final String cliente;
    private final String tipoCliente;
    private final int idClinicaDestino;
    private final String clinicaDestino;
    private final String pacienteNombre;
    private final String tipoProcedimiento;
    private final String doctorNombre;
    private final String diagnostico;
    private final String fechaHoraSolicitada;
    private final String observaciones;
    private final String estado;
    private final String motivoRechazo;
    private final String fechaSolicitud;
    private final String fechaRespuesta;

    public SolicitudCirugiaDTO(SolicitudCirugia solicitud) {
        idSolicitud = solicitud.getIdSolicitud();
        idVendedorRegistrador = solicitud.getVendedorRegistrador() == null
                ? null : solicitud.getVendedorRegistrador().getIdUsuario();
        idCliente = solicitud.getCliente().getIdCliente();
        cliente = solicitud.getCliente().getNombre();
        tipoCliente = solicitud.getCliente() instanceof ClinicaHospital ? "CLINICA" : "PARTICULAR";
        idClinicaDestino = solicitud.getClinicaDestino().getIdCliente();
        clinicaDestino = solicitud.getClinicaDestino().getNombre();
        pacienteNombre = solicitud.getPacienteNombre();
        tipoProcedimiento = solicitud.getTipoProcedimiento();
        doctorNombre = solicitud.getDoctorNombre();
        diagnostico = solicitud.getDiagnostico();
        fechaHoraSolicitada = solicitud.getFechaHoraSolicitada() == null
                ? null : solicitud.getFechaHoraSolicitada().toString();
        observaciones = solicitud.getObservaciones();
        estado = solicitud.getEstado().name();
        motivoRechazo = solicitud.getMotivoRechazo();
        fechaSolicitud = solicitud.getFechaSolicitud() == null
                ? null : solicitud.getFechaSolicitud().toString();
        fechaRespuesta = solicitud.getFechaRespuesta() == null
                ? null : solicitud.getFechaRespuesta().toString();
    }

    public int getIdSolicitud() { return idSolicitud; }
    public Integer getIdVendedorRegistrador() { return idVendedorRegistrador; }
    public int getIdCliente() { return idCliente; }
    public String getCliente() { return cliente; }
    public String getTipoCliente() { return tipoCliente; }
    public int getIdClinicaDestino() { return idClinicaDestino; }
    public String getClinicaDestino() { return clinicaDestino; }
    public String getPacienteNombre() { return pacienteNombre; }
    public String getTipoProcedimiento() { return tipoProcedimiento; }
    public String getDoctorNombre() { return doctorNombre; }
    public String getDiagnostico() { return diagnostico; }
    public String getFechaHoraSolicitada() { return fechaHoraSolicitada; }
    public String getObservaciones() { return observaciones; }
    public String getEstado() { return estado; }
    public String getMotivoRechazo() { return motivoRechazo; }
    public String getFechaSolicitud() { return fechaSolicitud; }
    public String getFechaRespuesta() { return fechaRespuesta; }
}
