package pe.edu.pucp.klam.rest.dto;

/** Cuerpo uniforme de los errores de la API: {"error": "mensaje"}. */
public class ErrorDTO {

    private String error;

    public ErrorDTO() {
    }

    public ErrorDTO(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
