package pe.edu.pucp.klam.rest.dto;

public class ClinicaNuevaDTO {
    private String nombre;
    private String ruc;
    private String direccion;
    private String emailContacto;
    private String telefono;
    private String periodoCredito;
    private boolean tieneConsignacion;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getEmailContacto() { return emailContacto; }
    public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getPeriodoCredito() { return periodoCredito; }
    public void setPeriodoCredito(String periodoCredito) { this.periodoCredito = periodoCredito; }
    public boolean isTieneConsignacion() { return tieneConsignacion; }
    public void setTieneConsignacion(boolean tieneConsignacion) { this.tieneConsignacion = tieneConsignacion; }
}
