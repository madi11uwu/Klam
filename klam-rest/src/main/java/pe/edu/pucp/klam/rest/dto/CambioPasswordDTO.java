package pe.edu.pucp.klam.rest.dto;

/** Cuerpo de PUT /cuenta/password. El usuario sale de la sesión (token), no del cuerpo. */
public class CambioPasswordDTO {
    private String passwordActual;
    private String passwordNueva;

    public CambioPasswordDTO() {
    }

    public String getPasswordActual() { return passwordActual; }
    public void setPasswordActual(String passwordActual) { this.passwordActual = passwordActual; }
    public String getPasswordNueva() { return passwordNueva; }
    public void setPasswordNueva(String passwordNueva) { this.passwordNueva = passwordNueva; }
}
