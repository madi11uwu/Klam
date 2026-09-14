package pe.edu.pucp.klam.modelo.agendaoperaciones;

import pe.edu.pucp.klam.modelo.Registro;

import java.util.Map;

public class BandejaInstrumental extends Registro {

    private String tipo;
    private boolean esterilizado;
    private Map<Consumible,Integer> consumibles;

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

    public void agregarConsumible(Consumible consumible,int cantidad) {
        consumibles.put(consumible,cantidad);
    }

    public void cargarConsumiblesPorTipoCirugia(String tipoCirugia) {
        consumibles.clear();

        if (tipoCirugia.equalsIgnoreCase("CEREBRO")) {

            Consumible fresaCortante70 =
                    new Consumible("Fresa cortante", "Sin especificar", "70");

            Consumible fresaDiamante70 =
                    new Consumible("Fresa diamante", "Sin especificar", "70");

            Consumible cuchilla70 =
                    new Consumible("Cuchilla quirúrgica", "Sin especificar", "70");

            agregarConsumible(fresaCortante70, 1);
            agregarConsumible(fresaDiamante70, 1);
            agregarConsumible(cuchilla70, 1);
        }

        else if (tipoCirugia.equalsIgnoreCase("COLUMNA")) {

            Consumible fresaCortante125 =
                    new Consumible("Fresa cortante", "Sin especificar", "125");

            Consumible fresaDiamante125 =
                    new Consumible( "Fresa diamante", "Sin especificar", "125");

            Consumible cuchilla125 =
                    new Consumible( "Cuchilla quirúrgica", "Sin especificar", "125");

            Consumible fresaCortante150 =
                    new Consumible( "Fresa cortante", "Sin especificar", "150");

            Consumible fresaDiamante150 =
                    new Consumible( "Fresa diamante", "Sin especificar", "150");

            Consumible cuchilla150 =
                    new Consumible( "Cuchilla quirúrgica", "Sin especificar", "150");

            agregarConsumible(fresaCortante125, 1);
            agregarConsumible(fresaDiamante125, 1);
            agregarConsumible(cuchilla125, 1);

            agregarConsumible(fresaCortante150, 1);
            agregarConsumible(fresaDiamante150, 1);
            agregarConsumible(cuchilla150, 1);
        }
    }

    public void eliminarConsumible(Consumible consumible) {
        consumibles.remove(consumible);
    }

    public int obtenerCantidadConsumibles(Consumible consumible) {
        return consumibles.getOrDefault(consumible,0);
    }

    public void esterilizar() {
        esterilizado = true;
    }

    public void marcarComoNoEsterilizado() {
        esterilizado = false;
    }

    public boolean estaListaParaUso() {
        return esterilizado && !consumibles.isEmpty();
    }

    public boolean verificarCompatibilidad(String tipoProcedimiento) {
        return tipo != null &&
                tipoProcedimiento != null &&
                tipo.equalsIgnoreCase(tipoProcedimiento);
    }
}
