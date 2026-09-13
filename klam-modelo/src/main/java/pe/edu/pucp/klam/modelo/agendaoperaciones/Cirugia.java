package pe.edu.pucp.klam.modelo.agendaoperaciones;
 
import java.time.LocalDateTime;
import pe.edu.pucp.klam.interfaces.Agendable;
import pe.edu.pucp.klam.interfaces.Cancelable;
 
public class Cirugia implements Agendable, Cancelable {
    private int id_cirugia;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private String tipoProcedimiento;

    //estos no se si estan bien?
    private String motivo_cancelacion;
    private String doctor_nombre;
    //----

    private EstadoCirugia estado;
    private Equipo equipo;
    private BandejaInstrumental bandejaInstrumental;

     public int getId_cirugia() {
        return id_cirugia;
    }
 
    public void setId_cirugia(int id_cirugia) {
        this.id_cirugia = id_cirugia;
    }
 
    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }
    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }
    
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public String getTipoProcedimiento() {
        return tipoProcedimiento;
    }
    public void setTipoProcedimiento(String tipoProcedimiento) {
        this.tipoProcedimiento = tipoProcedimiento;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }
    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public String getDoctornombre() {
        return doctornombre;
    }

    public void setDoctornombre(String doctornombre) {
        this.doctornombre = doctornombre;
    }
    public EstadoCirugia getEstado() {
        return estado;
    }

    public void setEstado(EstadoCirugia estado) {
       if (estado==null){
            throw new IllegalArgumentException("estado nulo");
        }
        this.estado = estado;
    }

    public Equipo getEquipo() {
        return equipo;
    }
    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public BandejaInstrumental getBandejaInstrumental() {
        return bandejaInstrumental;
    }

    public void setBandejaInstrumental(BandejaInstrumental bandejaInstrumental) {
        this.bandejaInstrumental = bandejaInstrumental;
    }
    
    //metodos de la interfaz Agendable
    @Override
    public String getIdentificador() {
        return String.valueOf(id_cirugia);
    }
 
    @Override
    public void cancelar(String motivo) {
        this.estado = EstadoCirugia.CANCELADA;
        this.motivoCancelacion = motivo;
    }
 }