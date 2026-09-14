package pe.edu.pucp.klam.modelo.agendaoperaciones;

import java.util.ArrayList;
import java.util.List;

public class BandejaInstrumental {

    private int id_bandeja;
    private String tipo;
    private boolean esterilizado;
    private List<Consumible> consumbiles;
    public BandejaInstrumental() {
        consumbiles=new ArrayList<>();
    }
    public int getId_banceja() {
        return id_bandeja;
    }

    public void setId_banceja(int id_banceja) {
        this.id_bandeja = id_banceja;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isEsterilizado() {
        return esterilizado;
    }

    public void setEsterilizado(boolean esterilizado) {
        this.esterilizado = esterilizado;
    }

    public List<Consumible> getConsumbiles() {
        return new ArrayList<>(consumbiles);
    }

    public void setConsumbiles(List<Consumible> consumbiles) {
        this.consumbiles = consumbiles;
    }


}
