package pe.edu.pucp.klam.rest.dto;

public class MetricasUsuariosDTO {
    private int totalRegistrados;
    private int cuentasActivas;
    private int cuentasInactivas;
    private int rolesPersonal;

    public MetricasUsuariosDTO() {
    }

    public MetricasUsuariosDTO(int totalRegistrados, int cuentasActivas, int cuentasInactivas, int rolesPersonal) {
        this.totalRegistrados = totalRegistrados;
        this.cuentasActivas = cuentasActivas;
        this.cuentasInactivas = cuentasInactivas;
        this.rolesPersonal = rolesPersonal;
    }

    public int getTotalRegistrados() {
        return totalRegistrados;
    }

    public void setTotalRegistrados(int totalRegistrados) {
        this.totalRegistrados = totalRegistrados;
    }

    public int getCuentasActivas() {
        return cuentasActivas;
    }

    public void setCuentasActivas(int cuentasActivas) {
        this.cuentasActivas = cuentasActivas;
    }

    public int getCuentasInactivas() {
        return cuentasInactivas;
    }

    public void setCuentasInactivas(int cuentasInactivas) {
        this.cuentasInactivas = cuentasInactivas;
    }

    public int getRolesPersonal() {
        return rolesPersonal;
    }

    public void setRolesPersonal(int rolesPersonal) {
        this.rolesPersonal = rolesPersonal;
    }
}
