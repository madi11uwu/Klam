package pe.edu.pucp.klam.modelo.clientes;

public class PacienteParticular extends Cliente {
    private String dni;
    private boolean pagoConfirmado;

    public PacienteParticular() {
        super();
    }

    public PacienteParticular(String id_cliente, String nombre, String direccion, String emailContacto,
                              String telefono, String dni, boolean pagoConfirmado) {
        this.id_cliente = id_cliente;
        this.nombre = nombre;
        this.direccion = direccion;
        this.emailContacto = emailContacto;
        this.telefono = telefono;
        this.activo = true;
        this.dni = dni;
        this.pagoConfirmado = pagoConfirmado;
    }

    // Getters y Setters
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public boolean isPagoConfirmado() { return pagoConfirmado; }
    public void setPagoConfirmado(boolean pagoConfirmado) { this.pagoConfirmado = pagoConfirmado; }
}