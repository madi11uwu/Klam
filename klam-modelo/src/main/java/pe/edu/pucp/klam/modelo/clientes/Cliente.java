package pe.edu.pucp.klam.modelo.clientes;

public abstract class Cliente {
    protected String id_cliente;
    protected String nombre;
    protected String direccion;
    protected String emailContacto;
    protected String telefono;
    protected boolean estado; // true = Activo, false = Inactivo (Eliminación lógica)

    public Cliente() {
        this.estado = true;
    }

    // Getters y Setters
    public String getId_cliente() { return id_cliente; }
    public void setId_cliente(String id_cliente) { this.id_cliente = id_cliente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEmailContacto() { return emailContacto; }
    public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }
}