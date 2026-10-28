package pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso;

import pe.edu.pucp.klam.modelo.interfaces.Validable;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;

import java.time.LocalDateTime;

public class DocumentoIngreso implements Validable {
    private int idDocumento;
    private TipoDocumentoIngreso tipoDocumento;
    private String archivoPath;
    private EstadoValidacionDocumento estadoValidacion;
    private LocalDateTime fechaCarga;
    private OrdenCompra ordenCompra;
    private boolean activo;
    private UsuarioPlataforma usuarioCarga; // quien subio el documento (admin, vendedor o tecnico)

    // Trazabilidad y subsanacion (RF04, RF07, RF08)
    private Integer idSolicitud;          // solicitud a la que pertenece (puede ser null)
    private Integer idCirugia;            // cirugia a la que pertenece (puede ser null)
    private int version = 1;              // la resubida crea una version nueva, no sobrescribe
    private LocalDateTime fechaValidacion;
    private String motivoRechazo;
    private String responsable;           // texto: los clientes no tienen usuario en la plataforma

    public DocumentoIngreso(){
        this.estadoValidacion = EstadoValidacionDocumento.PENDIENTE;
        this.activo = true;
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
        this.idSolicitud = documentoIngreso.getIdSolicitud();
        this.idCirugia = documentoIngreso.getIdCirugia();
        this.version = documentoIngreso.getVersion();
        this.fechaValidacion = documentoIngreso.getFechaValidacion();
        this.motivoRechazo = documentoIngreso.getMotivoRechazo();
        this.responsable = documentoIngreso.getResponsable();
    }

    public Integer getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Integer idSolicitud) { this.idSolicitud = idSolicitud; }

    public Integer getIdCirugia() { return idCirugia; }
    public void setIdCirugia(Integer idCirugia) { this.idCirugia = idCirugia; }

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

    /** Eliminacion logica: el documento se conserva aunque se dé de baja. */
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

    @Override
    public boolean validar() {
        return this.estadoValidacion == EstadoValidacionDocumento.VALIDADO;
    }
}
