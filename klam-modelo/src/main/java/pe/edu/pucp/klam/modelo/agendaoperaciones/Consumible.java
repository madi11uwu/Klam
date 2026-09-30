package pe.edu.pucp.klam.modelo.agendaoperaciones;

import java.util.Objects;

public class Consumible {
    private int idConsumible;
    private String nombreComercial;
    private String marca;
    private String medida;
    private boolean activo;

    public Consumible(){
        this.activo = true;
    }

    public Consumible(int id,String nombreComercial,
                      String marca,
                      String medida) {
        this.idConsumible = id;
        this.nombreComercial = nombreComercial;
        this.marca = marca;
        this.medida = medida;
        this.activo = true;
    }

    public Consumible(final Consumible consumible){
        if (consumible == null){
            throw new IllegalArgumentException("Consumible no puede ser nulo");
        }
        setIdConsumible(consumible.getIdConsumible());
        setNombreComercial(consumible.getNombreComercial());
        setMarca(consumible.getMarca());
        setMedida(consumible.getMedida());
        setActivo(consumible.isActivo());
    }

    /** Eliminacion logica: el consumible se conserva aunque se dé de baja. */
    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public int getIdConsumible() {
        return idConsumible;
    }

    public void setIdConsumible(int idConsumible) {
        this.idConsumible = idConsumible;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getMedida() {
        return medida;
    }

    public void setMedida(String medida) {
        this.medida = medida;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Consumible that = (Consumible) o;
        return idConsumible == that.idConsumible;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idConsumible);
    }
}
