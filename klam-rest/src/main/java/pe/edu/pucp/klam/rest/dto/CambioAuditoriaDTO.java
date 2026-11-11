package pe.edu.pucp.klam.rest.dto;

/** Un campo modificado: valor anterior y posterior (null = sin valor). */
public class CambioAuditoriaDTO {

    private String campo;
    private String valorAnterior;
    private String valorPosterior;

    public CambioAuditoriaDTO() {
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior;
    }

    public String getValorPosterior() {
        return valorPosterior;
    }

    public void setValorPosterior(String valorPosterior) {
        this.valorPosterior = valorPosterior;
    }
}
