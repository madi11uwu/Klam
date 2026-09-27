package pe.edu.pucp.klam.modelo.agendaoperaciones;

public class Consumible {
    private int id_consumible;
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
        this.id_consumible = id;
        this.nombreComercial = nombreComercial;
        this.marca = marca;
        this.medida = medida;
        this.activo = true;
    }

    public Consumible(final Consumible consumible){
        if (consumible == null){
            throw new IllegalArgumentException("Consumible no puede ser nulo");
        }
        setId_consumible(consumible.getId_consumible());
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

    public int getId_consumible() {
        return id_consumible;
    }

    public void setId_consumible(int id_consumible) {
        this.id_consumible = id_consumible;
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
}
