package pe.edu.pucp.klam.modelo.agendaoperaciones;

public class Consumible {
    private int id_consumible;
    private String nombreComercial;
    private String marca;
    private String medida;

    public Consumible(String nombreComercial,
                      String marca,
                      String medida) {
        this.nombreComercial = nombreComercial;
        this.marca = marca;
        this.medida = medida;
    }

    public Consumible(int id_consumible,
                      String nombreComercial,
                      String marca,
                      String medida) {
        this.id_consumible = id_consumible;
        this.nombreComercial = nombreComercial;
        this.marca = marca;
        this.medida = medida;
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
