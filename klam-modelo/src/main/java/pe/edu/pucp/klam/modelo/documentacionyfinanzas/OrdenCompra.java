package pe.edu.pucp.klam.modelo.documentacionyfinanzas;

import pe.edu.pucp.klam.modelo.interfaces.Verificable;

import java.time.LocalDateTime;


public class OrdenCompra implements Verificable {

    private String idOrdenCompra;
    private String archivoRespaldoPath;
    private LocalDateTime fechaRecepcion;

    public OrdenCompra(){

    }

    public OrdenCompra(final OrdenCompra ordenCompra) {
        if(ordenCompra==null){
            throw new IllegalArgumentException("La orden de compra no puede ser nula");
        }
        setIdOrdenCompra(ordenCompra.getIdOrdenCompra());
        setArchivoRespaldoPath(ordenCompra.getArchivoRespaldoPath());
        setFechaRecepcion(ordenCompra.getFechaRecepcion());
    }

    public String getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(String idOrdenCompra) {

        if(idOrdenCompra==null || idOrdenCompra.isEmpty()){
            throw new IllegalArgumentException("El identificador de la orden de compra no puede ser nulo o vacío ");
        }
        this.idOrdenCompra = idOrdenCompra;
    }

    public String getArchivoRespaldoPath() {
        return archivoRespaldoPath;
    }

    public void setArchivoRespaldoPath(String archivoRespaldoPath) {
        if(archivoRespaldoPath==null || archivoRespaldoPath.isEmpty()){
            throw new IllegalArgumentException("El archivo de respaldo path no puede ser nulo o vacío ");
        }
        this.archivoRespaldoPath = archivoRespaldoPath;
    }

    public LocalDateTime getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(LocalDateTime fechaRecepcion) {
        if(fechaRecepcion==null ){
            throw new IllegalArgumentException("La fecha de recepción no puede ser nula");
        }
        this.fechaRecepcion = fechaRecepcion;
    }

    @Override
    public boolean verificar() {
        return this.archivoRespaldoPath != null && !this.archivoRespaldoPath.isEmpty();
    }

}
