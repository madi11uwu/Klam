package pe.edu.pucp.klam.modelo.agendaoperaciones;

import java.time.LocalDateTime;
import pe.edu.pucp.klam.interfaces.Agendable;

public class Alerta implements Agendable {
    private int id_alerta;
    private int id_cirugia;
    private String tipo_alerta;
    private String mensaje;
    private boolean activo;//este esta bien?, es para saber si la alerta esta activa o ya fue leida
    private LocalDateTime fecha_generacion;
    
    public int getId_alerta() {
        return id_alerta;
    }

    public void setId_alerta(int id_alerta) {
        this.id_alerta = id_alerta;
    }
    public int getId_cirugia() {
        return id_cirugia;
    }
    public void setId_cirugia(int id_cirugia) {
        this.id_cirugia = id_cirugia;
    }
    public String getTipoAlerta() {
        return tipoAlerta;
    }
    public void setTipoAlerta(String tipoAlerta) {
        this.tipoAlerta = tipoAlerta;
    }
    public String getMensaje() {
        return mensaje;
    }
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }
      public boolean isActivo() {
        return activo;
    }
 
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
