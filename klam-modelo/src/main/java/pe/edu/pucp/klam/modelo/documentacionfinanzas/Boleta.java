package pe.edu.pucp.klam.modelo.documentacionfinanzas;

import java.time.LocalDateTime;

public class Boleta extends DocumentoFacturacion {

    private String dniReceptor;

    public Boleta() {
        super();
    }

    public Boleta(int idCirugia, LocalDateTime fechaEmision, String dniReceptor) {
        super(idCirugia, fechaEmision);
        validarDni(dniReceptor);
        this.dniReceptor = dniReceptor;
    }

    /** dni_receptor es CHAR(8) NOT NULL en el script SQL. */
    private static void validarDni(String dni) {
        if (dni == null || !dni.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 digitos");
        }
    }

    public String getDniReceptor() { return dniReceptor; }
    public void setDniReceptor(String dniReceptor) {
        validarDni(dniReceptor);
        this.dniReceptor = dniReceptor;
    }

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