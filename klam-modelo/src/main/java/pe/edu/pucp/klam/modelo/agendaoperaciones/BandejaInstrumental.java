package pe.edu.pucp.klam.modelo.agendaoperaciones;

import pe.edu.pucp.klam.modelo.interfaces.Verificable;

import java.util.HashMap;
import java.util.Map;

public class BandejaInstrumental implements Verificable {
    private int id_bandeja;
    private String tipo;
    private boolean esterilizado;
    private Map<Consumible,Integer> consumibles;

    public BandejaInstrumental(final BandejaInstrumental bandejaInstrumental) {
        if (bandejaInstrumental == null){
            throw new IllegalArgumentException("bandejaInstrumental nula");
        }
        setId_bandeja(bandejaInstrumental.getId_bandeja());
        setTipo(bandejaInstrumental.getTipo());
        setEsterilizado(bandejaInstrumental.isEsterilizado());
        setConsumibles(bandejaInstrumental.getConsumibles());
    }

    public Map<Consumible,Integer> getConsumibles() {
        return new HashMap<>(consumibles);
    }

    public void setConsumibles(Map<Consumible,Integer> consumibles) {
        this.consumibles = new HashMap<>(consumibles);
    }

    public boolean isEsterilizado() {
        return esterilizado;
    }

    public void setEsterilizado(boolean esterilizado) {
        this.esterilizado = esterilizado;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getId_bandeja() {
        return id_bandeja;
    }

    public void setId_bandeja(int id_bandeja) {
        this.id_bandeja = id_bandeja;
    }

    @Override
    public boolean verificar() {
        if (esterilizado) return true;
        return false;
    }
}
