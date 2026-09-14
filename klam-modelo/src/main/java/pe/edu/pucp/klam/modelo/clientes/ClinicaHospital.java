package pe.edu.pucp.klam.modelo.clientes;

public class ClinicaHospital extends Cliente {
    private String ruc;
    private boolean tieneConsignacion;
    private String periodoCredito;

    public ClinicaHospital() {
        super();
    }

    public ClinicaHospital(String id_cliente, String nombre, String direccion, String emailContacto,
                           String telefono, String ruc, boolean tieneConsignacion, String periodoCredito) {
        this.id_cliente = id_cliente;
        this.nombre = nombre;
        this.direccion = direccion;
        this.emailContacto = emailContacto;
        this.telefono = telefono;
        this.estado = true;
        this.ruc = ruc;
        this.tieneConsignacion = tieneConsignacion;
        this.periodoCredito = periodoCredito;
    }

    // Getters y Setters
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public boolean isTieneConsignacion() { return tieneConsignacion; }
    public void setTieneConsignacion(boolean tieneConsignacion) { this.tieneConsignacion = tieneConsignacion; }

    public String getPeriodoCredito() { return periodoCredito; }
    public void setPeriodoCredito(String periodoCredito) { this.periodoCredito = periodoCredito; }
}