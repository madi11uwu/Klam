package pe.edu.pucp.klam.modelo.agendaoperaciones;

import pe.edu.pucp.klam.modelo.Registro;

public class Consumible extends Registro {
    private String nombreComercial;
    private String marca;
    private String medida;

    public Consumible(String nombreComercial,
                      String marca,
                      String medida) {
        super();
        this.nombreComercial = nombreComercial;
        this.marca = marca;
        this.medida = medida;
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
