package pe.edu.pucp.klam.modelo.clientes;

public class PacienteParticular extends Cliente {
    private String dni;
    private boolean pagoConfirmado;

    public PacienteParticular(final PacienteParticular paciente) {
        super(paciente);
        setDni(paciente.getDni());
        setPagoConfirmado(paciente.isPagoConfirmado());
    }

    public PacienteParticular(int i, String juanPerez, String s, String mail, String number, String number1, boolean b) {
        super(i,juanPerez,s,mail,number);
        this.dni=number1;
        this.pagoConfirmado=b;
    }

    // Getters y Setters
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public boolean isPagoConfirmado() { return pagoConfirmado; }
    public void setPagoConfirmado(boolean pagoConfirmado) { this.pagoConfirmado = pagoConfirmado; }
}