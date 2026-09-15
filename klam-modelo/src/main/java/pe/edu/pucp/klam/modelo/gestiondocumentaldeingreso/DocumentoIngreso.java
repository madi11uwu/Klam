package pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso;

import java.time.LocalDateTime;

public class DocumentoIngreso {
    private String id_documento;
    private TipoDocumentoIngreso tipoDocumento;
    private String archivoPath;
    private String estadoValidacion;
    private LocalDateTime fechaCarga;

    public DocumentoIngreso(final DocumentoIngreso documentoIngreso){
        if(documentoIngreso==null){
            throw new IllegalArgumentException("documentoIngreso no puede ser nulo");
        }
        setId_documento(documentoIngreso.getId_documento());
        setTipoDocumento(documentoIngreso.getTipoDocumento());
        setArchivoPath(documentoIngreso.getArchivoPath());
        setEstadoValidacion(documentoIngreso.getEstadoValidacion());
        setFechaCarga(documentoIngreso.getFechaCarga());
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
}
