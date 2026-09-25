package pe.edu.pucp.klam.modelo.clientes;

public class ClinicaHospital extends Cliente {
    private String ruc;
    private boolean tieneConsignacion;
    private String periodoCredito;

    public ClinicaHospital(final ClinicaHospital clinicaHospital) {
        if (clinicaHospital==null) {
            throw new IllegalArgumentException("ClinicaHospital no puede ser nulo.");
        }
        super(clinicaHospital);
        setRuc(clinicaHospital.getRuc());
        setTieneConsignacion(clinicaHospital.isTieneConsignacion());
        setPeriodoCredito(clinicaHospital.getPeriodoCredito());
    }
    public ClinicaHospital () {
        super();
    }
    public ClinicaHospital(int id, String clinicaDelgado, String s1, String mail, String number, String number1, boolean b, String s2) {
        super(id,clinicaDelgado,s1,mail,number);
        this.ruc=number1;
        this.periodoCredito=s2;
        this.tieneConsignacion=b;
    }

    // Getters y Setters
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public boolean isTieneConsignacion() { return tieneConsignacion; }
    public void setTieneConsignacion(boolean tieneConsignacion) { this.tieneConsignacion = tieneConsignacion; }

    public String getPeriodoCredito() { return periodoCredito; }
    public void setPeriodoCredito(String periodoCredito) { this.periodoCredito = periodoCredito; }
}