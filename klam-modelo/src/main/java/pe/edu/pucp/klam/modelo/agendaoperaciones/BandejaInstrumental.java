package pe.edu.pucp.klam.modelo.agendaoperaciones;

import pe.edu.pucp.klam.modelo.interfaces.Verificable;

import java.util.HashMap;
import java.util.Map;

public class BandejaInstrumental implements Verificable {
    private int idBandeja;
    private String tipo;
    private boolean esterilizado;
    private Map<Consumible,Integer> consumibles;            // despachados a la cirugia
    private Map<Consumible,Integer> consumiblesConsumidos;  // usados realmente
    private boolean activo;

    public BandejaInstrumental() {
        this.consumibles = new HashMap<>();
        this.consumiblesConsumidos = new HashMap<>();
        this.activo = true;
    }

    public BandejaInstrumental(int i, String s) {
        this.idBandeja = i;
        this.tipo = s;
        this.consumibles = new HashMap<>();
        this.consumiblesConsumidos = new HashMap<>();
        this.activo = true;
    }

    public BandejaInstrumental(final BandejaInstrumental bandejaInstrumental) {
        if (bandejaInstrumental == null) {
            throw new IllegalArgumentException("bandejaInstrumental nula");
        }
        setIdBandeja(bandejaInstrumental.getIdBandeja());
        setTipo(bandejaInstrumental.getTipo());
        setEsterilizado(bandejaInstrumental.isEsterilizado());
        setConsumibles(bandejaInstrumental.getConsumibles());
        setConsumiblesConsumidos(bandejaInstrumental.getConsumiblesConsumidos());
        setActivo(bandejaInstrumental.isActivo());
    }

    public Map<Consumible,Integer> getConsumibles() {
        return new HashMap<>(consumibles);
    }

    public void setConsumibles(Map<Consumible,Integer> consumibles) {
        this.consumibles = new HashMap<>(consumibles);
    }

    public Map<Consumible,Integer> getConsumiblesConsumidos() {
        return new HashMap<>(consumiblesConsumidos);
    }

    public void setConsumiblesConsumidos(Map<Consumible,Integer> consumiblesConsumidos) {
        this.consumiblesConsumidos = new HashMap<>(consumiblesConsumidos);
    }

    /** Eliminacion logica: la bandeja se conserva aunque se de de baja. */
    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
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

    public int getIdBandeja() {
        return idBandeja;
    }

    public void setIdBandeja(int idBandeja) {
        this.idBandeja = idBandeja;
    }

    public void agregarConsumible(Consumible consumible, int cantidad) {
        if (consumible == null) {
            throw new IllegalArgumentException("Consumible no puede ser nulo");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        consumibles.put(consumible, cantidad);
    }

    public void eliminarConsumible(Consumible consumible) {
        consumibles.remove(consumible);
    }

    public int obtenerCantidadConsumible(Consumible consumible) {
        return consumibles.getOrDefault(consumible, 0);
    }

    /** Registra cuanto se uso realmente de un consumible despues de la cirugia. */
    public void registrarConsumo(Consumible consumible, int cantidad) {
        if (consumible == null) {
            throw new IllegalArgumentException("Consumible no puede ser nulo");
        }
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        consumiblesConsumidos.put(consumible, cantidad);
    }

    public int obtenerCantidadConsumida(Consumible consumible) {
        return consumiblesConsumidos.getOrDefault(consumible, 0);
    }

    /** Lo despachado menos lo consumido: lo que deberia regresar a inventario. */
    public int obtenerDiferencia(Consumible consumible) {
        return obtenerCantidadConsumible(consumible) - obtenerCantidadConsumida(consumible);
    }

    @Override
    public boolean verificar() {
        return esterilizado;
    }
}