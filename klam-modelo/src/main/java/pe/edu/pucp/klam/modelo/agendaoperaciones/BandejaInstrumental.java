package pe.edu.pucp.klam.modelo.agendaoperaciones;

import java.util.ArrayList;
import java.util.List;

public class BandejaInstrumental {
    private int id_banceja;
    private String tipo;
    private boolean esterilizado;
    private List<Consumible> consumbiles;
    public BandejaInstrumental() {
        consumbiles=new ArrayList<>();
    }
}
