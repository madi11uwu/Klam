package pe.edu.pucp.klam.modelo.documentacionfinanzas;

import java.time.LocalDateTime;

public class Boleta extends DocumentoFacturacion {

    private String dniReceptor;

    public Boleta() {
        super();
    }

    public Boleta(int idCirugia, LocalDateTime fechaEmision, String dniReceptor) {
        super(idCirugia, fechaEmision);
        this.dniReceptor = dniReceptor;
    }

    public String getDniReceptor() { return dniReceptor; }
    public void setDniReceptor(String dniReceptor) { this.dniReceptor = dniReceptor; }

    @Override
    protected boolean esLineaValida(LineaDocumento linea) {
        return linea instanceof LineaBoleta;
    }

    @Override
    public String toString() {
        return "Boleta{id=" + getIdDocumento() + ", dni=" + dniReceptor
                + ", total=" + getMontoTotal() + ", estado=" + getEstadoPago() + "}";
    }
}