package pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.SolicitudCirugia;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;

import java.time.LocalDateTime;

public class DocumentoIngreso {
    private int idDocumento;
    private TipoDocumentoIngreso tipoDocumento;
    private String archivoPath;
    private EstadoValidacionDocumento estadoValidacion;
    private LocalDateTime fechaCarga;
    private OrdenCompra ordenCompra;
    private boolean activo;
    private UsuarioPlataforma usuarioCarga;
   // Trazabilidad y subsanacion (RF04, RF07, RF08)
    private SolicitudCirugia solicitud;          // solicitud a la que pertenece (puede ser null)
    private Cirugia cirugia;            // cirugia a la que pertenece (puede ser null)
    private int version = 1;              // la resubida crea una version nueva, no sobrescribe
    private LocalDateTime fechaValidacion;
    private String motivoRechazo;
    private boolean habilitarResubida;
    private String responsable;           // texto: los clientes no tienen usuario en la plataforma

    public DocumentoIngreso(){
        this.estadoValidacion = EstadoValidacionDocumento.PENDIENTE;
        this.activo = true;
        this.habilitarResubida = false;
    }
    public OrdenCompra getOrdenCompra() {
        return ordenCompra!=null? new OrdenCompra(ordenCompra):null;
    }

    public void setOrdenCompra(OrdenCompra ordenCompra) {
        this.ordenCompra = (ordenCompra!=null)?new OrdenCompra(ordenCompra):null;
    }

    public DocumentoIngreso(final DocumentoIngreso documentoIngreso){
        if(documentoIngreso==null){
            throw new IllegalArgumentException("documentoIngreso no puede ser nulo");
        }
        setIdDocumento(documentoIngreso.getIdDocumento());
        setTipoDocumento(documentoIngreso.getTipoDocumento());
        setArchivoPath(documentoIngreso.getArchivoPath());
        setEstadoValidacion(documentoIngreso.getEstadoValidacion());
        setFechaCarga(documentoIngreso.getFechaCarga());
        setOrdenCompra(documentoIngreso.getOrdenCompra());
        setActivo(documentoIngreso.isActivo());
        setUsuarioCarga(documentoIngreso.getUsuarioCarga());
        setSolicitud(documentoIngreso.getSolicitud());
        setCirugia(documentoIngreso.getCirugia());
        this.version = documentoIngreso.getVersion();
        this.fechaValidacion = documentoIngreso.getFechaValidacion();
        this.motivoRechazo = documentoIngreso.getMotivoRechazo();
        this.habilitarResubida = documentoIngreso.isHabilitarResubida();
        this.responsable = documentoIngreso.getResponsable();
    }
    public SolicitudCirugia getSolicitud() {
        return solicitud;
    }
    public void setSolicitud(SolicitudCirugia solicitud) {
        this.solicitud = solicitud;
    }
    public Cirugia getCirugia() {
        return cirugia;
    }
    public void setCirugia(Cirugia cirugia) {
        this.cirugia = cirugia;
    }
    public int getVersion() { return version; }
    public void setVersion(int version) {
        if (version < 1) {
            throw new IllegalArgumentException("la version debe ser mayor o igual a 1");
        }
        this.version = version;
    }
    public LocalDateTime getFechaValidacion() { return fechaValidacion; }
    public void setFechaValidacion(LocalDateTime fechaValidacion) { this.fechaValidacion = fechaValidacion; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public boolean isHabilitarResubida() { return habilitarResubida; }
    public void setHabilitarResubida(boolean habilitarResubida) {
        this.habilitarResubida = habilitarResubida;
    }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public UsuarioPlataforma getUsuarioCarga() {
        return usuarioCarga;
    }

    public void setUsuarioCarga(UsuarioPlataforma usuarioCarga) {
        this.usuarioCarga = usuarioCarga;
    }
    public TipoDocumentoIngreso getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumentoIngreso tipoDocumento) {
        if(tipoDocumento==null){
            throw new IllegalArgumentException("El tipo de documento de ingreso no puede ser nula");
        }
        this.tipoDocumento = tipoDocumento;
    }

    public int getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(int idDocumento) {
        if(idDocumento <0){
            throw new IllegalArgumentException("id_documento no puede ser nulo o vacío");
        }
        this.idDocumento = idDocumento;
    }

    public String getArchivoPath() {
        return archivoPath;
    }

    public void setArchivoPath(String archivoPath) {
        if(archivoPath==null || archivoPath.isEmpty()){
            throw new IllegalArgumentException("archivoPath no puede ser nulo o vacío");
        }
        this.archivoPath = archivoPath;
    }

    public EstadoValidacionDocumento getEstadoValidacion() {
        return estadoValidacion;
    }

    public void setEstadoValidacion(EstadoValidacionDocumento estadoValidacion) {
        if(estadoValidacion==null){
            throw new IllegalArgumentException("estadoValidacion no puede ser nulo");
        }
        this.estadoValidacion = estadoValidacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCarga() {

        return fechaCarga;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
        if(fechaCarga==null){
            throw new IllegalArgumentException("fechaCarga no puede ser nulo");
        }
        this.fechaCarga = fechaCarga;
    }

}
