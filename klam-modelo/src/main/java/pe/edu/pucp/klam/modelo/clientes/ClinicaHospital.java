package pe.edu.pucp.klam.modelo.clientes;

public class ClinicaHospital extends Cliente{
    private String ruc;
    private boolean tieneConsignacion;
    private String periodoCredito;

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        if(ruc==null || ruc.isEmpty()){
            throw new IllegalArgumentException("ruc no puede quedar nulo o vacío");
        }
        this.ruc = ruc;
    }

    public boolean isTieneConsignacion() {
        return tieneConsignacion;
    }

    public void setTieneConsignacion(boolean tieneConsignacion) {
        this.tieneConsignacion = tieneConsignacion;
    }

    public String getPeriodoCredito() {
        return periodoCredito;
    }

    public void setPeriodoCredito(String periodoCredito) {
        if(periodoCredito==null || periodoCredito.isEmpty()){
            throw new IllegalArgumentException("periodoCredito no puede quedar nulo o vacío");
        }
        this.periodoCredito = periodoCredito;
    }
    public ClinicaHospital(final ClinicaHospital clinicaHospital){
        if(clinicaHospital==null){
            throw new IllegalArgumentException("clinicaHospital no puede quedar nulo");
        }
        super(clinicaHospital);
        setRuc(clinicaHospital.getRuc());
        setTieneConsignacion(clinicaHospital.isTieneConsignacion());
        setPeriodoCredito(clinicaHospital.getPeriodoCredito());
    }
}
