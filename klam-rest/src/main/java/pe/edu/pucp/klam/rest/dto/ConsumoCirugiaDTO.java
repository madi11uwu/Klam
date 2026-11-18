package pe.edu.pucp.klam.rest.dto;

// Fila del listado de consumos (RF15): una cirugía finalizada con el estado de su cierre.
// estadoOrdenCompra: NO_APLICA | POR_ADJUNTAR | PENDIENTE | VALIDADO | RECHAZADO
// estadoConsumo:     NO_DISPONIBLE | POR_REGISTRAR | COMPLETO
public class ConsumoCirugiaDTO {
    private int idCirugia;
    private String fechaHoraInicio;
    private String tipoProcedimiento;
    private String tipoCliente;
    private String clienteNombre;
    private Integer idBandeja;
    private boolean requiereOrdenCompra;
    private String estadoOrdenCompra;
    private boolean documentacionCierreCompleta;
    private String estadoConsumo;

    public ConsumoCirugiaDTO() {
    }

    public int getIdCirugia() { return idCirugia; }
    public void setIdCirugia(int idCirugia) { this.idCirugia = idCirugia; }

    public String getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(String fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }

    public String getTipoProcedimiento() { return tipoProcedimiento; }
    public void setTipoProcedimiento(String tipoProcedimiento) { this.tipoProcedimiento = tipoProcedimiento; }

    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public Integer getIdBandeja() { return idBandeja; }
    public void setIdBandeja(Integer idBandeja) { this.idBandeja = idBandeja; }

    public boolean isRequiereOrdenCompra() { return requiereOrdenCompra; }
    public void setRequiereOrdenCompra(boolean requiereOrdenCompra) { this.requiereOrdenCompra = requiereOrdenCompra; }

    public String getEstadoOrdenCompra() { return estadoOrdenCompra; }
    public void setEstadoOrdenCompra(String estadoOrdenCompra) { this.estadoOrdenCompra = estadoOrdenCompra; }

    public boolean isDocumentacionCierreCompleta() { return documentacionCierreCompleta; }
    public void setDocumentacionCierreCompleta(boolean documentacionCierreCompleta) { this.documentacionCierreCompleta = documentacionCierreCompleta; }

    public String getEstadoConsumo() { return estadoConsumo; }
    public void setEstadoConsumo(String estadoConsumo) { this.estadoConsumo = estadoConsumo; }
}
