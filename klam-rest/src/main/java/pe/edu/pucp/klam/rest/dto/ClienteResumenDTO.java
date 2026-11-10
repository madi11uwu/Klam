package pe.edu.pucp.klam.rest.dto;

/** Cliente para selectores de pantalla. Nunca incluye username ni passwordHash. */
public class ClienteResumenDTO {
    private String tipoCliente;   // CLINICA_HOSPITAL | PACIENTE_PARTICULAR
    private int idCliente;
    private String nombre;
    private String documento;     // "RUC ..." o "DNI ..."
    private String dni;           // solo PACIENTE_PARTICULAR
    private String emailContacto;
    private String telefono;
    private boolean activo;

    public ClienteResumenDTO() {
    }

    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getEmailContacto() { return emailContacto; }
    public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
