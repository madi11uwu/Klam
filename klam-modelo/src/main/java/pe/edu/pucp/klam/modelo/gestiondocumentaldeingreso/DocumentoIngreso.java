package pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso;

import pe.edu.pucp.klam.modelo.interfaces.Validable;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;

import java.time.LocalDateTime;

public class DocumentoIngreso implements Validable {
    private int id_documento;
    private TipoDocumentoIngreso tipoDocumento;
    private String archivoPath;
    private EstadoValidacionDocumento estadoValidacion;
    private LocalDateTime fechaCarga;
    private OrdenCompra ordenCompra;
    private boolean activo;
    private UsuarioPlataforma usuarioCarga; // quien subio el documento (admin, vendedor o tecnico)

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
        setId_documento(documentoIngreso.getId_documento());
        setTipoDocumento(documentoIngreso.getTipoDocumento());
        setArchivoPath(documentoIngreso.getArchivoPath());
        setEstadoValidacion(documentoIngreso.getEstadoValidacion());
        setFechaCarga(documentoIngreso.getFechaCarga());
        setOrdenCompra(documentoIngreso.getOrdenCompra());
        setActivo(documentoIngreso.isActivo());
        setUsuarioCarga(documentoIngreso.getUsuarioCarga());
    }

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

    public int getId_documento() {
        return id_documento;
    }

    public void setId_documento(int id_documento) {
        if(id_documento<0){
            throw new IllegalArgumentException("id_documento no puede ser nulo o vacío");
        }
        this.id_documento = id_documento;
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
