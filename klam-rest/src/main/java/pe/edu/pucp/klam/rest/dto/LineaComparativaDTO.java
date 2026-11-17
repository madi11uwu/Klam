package pe.edu.pucp.klam.rest.dto;

public class LineaComparativaDTO {
    private String nombre;
    private String medida;
    private int despachada;
    private int consumida;
    private int diferencia;

    public LineaComparativaDTO() {
    }

    public LineaComparativaDTO(String nombre, String medida, int despachada, int consumida, int diferencia) {
        this.nombre = nombre;
        this.medida = medida;
        this.despachada = despachada;
        this.consumida = consumida;
        this.diferencia = diferencia;
    }

    public String getNombre() { return nombre; }
    public String getMedida() { return medida; }
    public int getDespachada() { return despachada; }
    public int getConsumida() { return consumida; }
    public int getDiferencia() { return diferencia; }
}