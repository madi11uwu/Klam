package pe.edu.pucp.klam.rest.dto;

/**
 * Alta y modificación de datos comerciales en /gestion-clientes.
 * Clínica: ruc, tieneConsignacion, periodoCredito. Paciente particular: dni, pagoConfirmado.
 * También es la forma del detalle (GET) y de la modificación (PUT).
 * La cuenta de acceso se habilita por separado.
 */
public class ClienteRegistroDTO {
    private int idCliente;          // solo en respuestas de detalle
    private boolean activo = true;
    private String tipoCliente;
    private String nombre;
    private String direccion;
    private String emailContacto;
    private String telefono;
    private String ruc;
    private boolean tieneConsignacion;
    private String periodoCredito;
    private String dni;
    private boolean pagoConfirmado;
    private String username;     // solo en el alta (obligatorio)
    private String password;     // solo en el alta (obligatorio); nunca se devuelve

    public ClienteRegistroDTO() {
    }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getEmailContacto() { return emailContacto; }
    public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public boolean isTieneConsignacion() { return tieneConsignacion; }
    public void setTieneConsignacion(boolean tieneConsignacion) { this.tieneConsignacion = tieneConsignacion; }
    public String getPeriodoCredito() { return periodoCredito; }
    public void setPeriodoCredito(String periodoCredito) { this.periodoCredito = periodoCredito; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public boolean isPagoConfirmado() { return pagoConfirmado; }
    public void setPagoConfirmado(boolean pagoConfirmado) { this.pagoConfirmado = pagoConfirmado; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
