package pe.edu.pucp.klam.modelo.clientes;

public class PacienteParticular extends Cliente{
    private String dni;
    private boolean pagoConfirmado;

    public PacienteParticular(final PacienteParticular pacienteParticular){
        if(pacienteParticular==null){
            throw new IllegalArgumentException("pacienteParticular no puede ser nulo");
        }
        super(pacienteParticular);
        setDni(pacienteParticular.getDni());
        setPagoConfirmado(pacienteParticular.isPagoConfirmado());
    }
    public boolean isPagoConfirmado() {
        return pagoConfirmado;
    }

    public void setPagoConfirmado(boolean pagoConfirmado) {
        this.pagoConfirmado = pagoConfirmado;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        if(dni==null || dni.isEmpty()){
            throw new IllegalArgumentException("dni no puede ser nulo o vacío");
        }
        this.dni = dni;
    }
}
