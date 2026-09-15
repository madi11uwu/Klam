package pe.edu.pucp.klam.modelo.documentacionfinanzas;

import java.time.LocalDateTime;

public class Factura extends DocumentoFacturacion {

    private String rucReceptor;

    public Factura() {
        super();
    }

    public Factura(int idCirugia, LocalDateTime fechaEmision, String rucReceptor) {
        super(idCirugia, fechaEmision);
        this.rucReceptor = rucReceptor;
    }

    public String getRucReceptor() { return rucReceptor; }
    public void setRucReceptor(String rucReceptor) { this.rucReceptor = rucReceptor; }

    @Override
    protected boolean esLineaValida(LineaDocumento linea) {
        return linea instanceof LineaFactura;
    }

    @Override
    public String toString() {
        return "Factura{id=" + getIdDocumento() + ", ruc=" + rucReceptor
                + ", total=" + getMontoTotal() + ", estado=" + getEstadoPago() + "}";
    }
}