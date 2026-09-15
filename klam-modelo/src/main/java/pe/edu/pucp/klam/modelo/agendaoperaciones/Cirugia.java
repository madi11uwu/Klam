package pe.edu.pucp.klam.modelo.agendaoperaciones;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Cirugia {
    private String id_cirugia;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private String estado;
    private String motivoCancelacion;
    private String tipoProcedimiento;
    private String doctorNombre;
    private Equipo equipo;
    private BandejaInstrumental bandeja;

    public String getTipoProcedimiento() {
        return tipoProcedimiento;
    }

    public void setTipoProcedimiento(String tipoProcedimiento) {
        if(tipoProcedimiento==null || tipoProcedimiento.isEmpty()){
            throw new IllegalArgumentException("tipoProcedimiento no puede ser nulo o vacío");
        }
        this.tipoProcedimiento = tipoProcedimiento;
    }

    public String getId_cirugia() {
        return id_cirugia;
    }

    public void setId_cirugia(String id_cirugia) {
        if(id_cirugia==null || id_cirugia.isEmpty()){
            throw new IllegalArgumentException("id_cirugia no puede ser nulo o vacío");
        }
        this.id_cirugia = id_cirugia;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) {
        if(fechaHoraInicio==null ){
            throw new IllegalArgumentException("fechaHoraInicio no puede ser nulo ");
        }
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        if(fechaHoraFin==null ){
            throw new IllegalArgumentException("fechaHoraFin no puede ser nulo");
        }
        this.fechaHoraFin = fechaHoraFin;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        if(motivoCancelacion==null || motivoCancelacion.isEmpty()){
            throw new IllegalArgumentException("motivoCancelacion no puede ser nulo o vacío");
        }
        this.motivoCancelacion = motivoCancelacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if(estado==null || estado.isEmpty()){
            throw new IllegalArgumentException("estado no puede ser nulo o vacío");
        }
        this.estado = estado;
    }

    public String getDoctorNombre() {
        return doctorNombre;
    }

    public void setDoctorNombre(String doctorNombre) {
        if(doctorNombre==null || doctorNombre.isEmpty()){
            throw new IllegalArgumentException("doctorNombre no puede ser nulo o vacío");
        }
        this.doctorNombre = doctorNombre;
    }

    public Equipo getEquipo() {
        return new Equipo(equipo);
    }

    public void setEquipo(Equipo equipo) {
        if(equipo==null){
            throw new IllegalArgumentException("equipo no puede ser nulo");
        }
        this.equipo = new Equipo(equipo);
    }

    public BandejaInstrumental getBandeja() {
        return new BandejaInstrumental(bandeja) ;
    }

    public void setBandeja(BandejaInstrumental bandeja) {
        if(bandeja==null){
            throw new IllegalArgumentException("bandeja no puede ser nulo");
        }
        this.bandeja =new BandejaInstrumental(bandeja);
    }

    public Cirugia(final Cirugia cirugia){
        if(cirugia==null){
            throw new IllegalArgumentException(("cirugia no puede ser nulo"));
        }
        setId_cirugia(cirugia.getId_cirugia());
        setFechaHoraInicio(cirugia.getFechaHoraInicio());
        setFechaHoraFin(cirugia.getFechaHoraFin());
        setEstado(cirugia.getEstado());
        setMotivoCancelacion(cirugia.getMotivoCancelacion());
        setTipoProcedimiento(cirugia.getTipoProcedimiento());
    }
}
