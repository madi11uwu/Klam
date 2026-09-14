package pe.edu.pucp.klam.modelo.agendaoperaciones;

import java.util.Map;

public class BandejaInstrumental {

    private int id_bandeja;
    private String tipo;
    private boolean esterilizado;
    private Map<Consumible,Integer> consumibles;

    public int getId_banceja() {
        return id_bandeja;
    }

    public void setId_banceja(int id_banceja) {
        this.id_bandeja = id_banceja;
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

}
