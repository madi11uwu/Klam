package pe.edu.pucp.klam.modelo.documentacionfinanzas;

import java.time.LocalDateTime;

public class Factura extends DocumentoFacturacion {

    private String rucReceptor;

    public Factura() {
        super();
    }

    public Factura(int idCirugia, LocalDateTime fechaEmision, String rucReceptor) {
        super(idCirugia, fechaEmision);
        validarRuc(rucReceptor);
        this.rucReceptor = rucReceptor;
    }

    /** ruc_receptor es CHAR(11) NOT NULL en el script SQL. */
    private static void validarRuc(String ruc) {
        if (ruc == null || !ruc.matches("\\d{11}")) {
            throw new IllegalArgumentException("El RUC debe tener exactamente 11 digitos");
        }
    }

    public String getRucReceptor() { return rucReceptor; }
    public void setRucReceptor(String rucReceptor) {
        validarRuc(rucReceptor);
        this.rucReceptor = rucReceptor;
    }

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