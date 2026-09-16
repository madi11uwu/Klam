package pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso;

import pe.edu.pucp.klam.modelo.interfaces.Validable;

import java.time.LocalDateTime;

public class DocumentoIngreso implements Validable {
    private String id_documento;
    private TipoDocumentoIngreso tipoDocumento;
    private String archivoPath;
    private String estadoValidacion;
    private LocalDateTime fechaCarga;
    private OrdenCompra ordenCompra;

    public DocumentoIngreso(){
        
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

    public String getId_documento() {
        return id_documento;
    }

    public void setId_documento(String id_documento) {
        if(id_documento==null || id_documento.isEmpty()){
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

    public String getEstadoValidacion() {
        return estadoValidacion;
    }

    public void setEstadoValidacion(String estadoValidacion) {
        if(estadoValidacion==null || estadoValidacion.isEmpty()){
            throw new IllegalArgumentException("estadoValidacion no puede ser nulo o vacío");
        }
        this.estadoValidacion = estadoValidacion;
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
        return "VALIDADO".equals(this.estadoValidacion);
    }
}
