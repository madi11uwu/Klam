package pe.edu.pucp.klam.rest.dto;

// Un consumible de la bandeja despachada: lo despachado, lo realmente consumido y la diferencia.
public class ConsumoLineaDTO {
    private int idConsumible;
    private String nombre;
    private String medida;
    private int cantidadDespachada;
    private int cantidadConsumida;
    private int diferencia;

    public ConsumoLineaDTO() {
    }

    public int getIdConsumible() { return idConsumible; }
    public void setIdConsumible(int idConsumible) { this.idConsumible = idConsumible; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMedida() { return medida; }
    public void setMedida(String medida) { this.medida = medida; }

    public int getCantidadDespachada() { return cantidadDespachada; }
    public void setCantidadDespachada(int cantidadDespachada) { this.cantidadDespachada = cantidadDespachada; }

    public int getCantidadConsumida() { return cantidadConsumida; }
    public void setCantidadConsumida(int cantidadConsumida) { this.cantidadConsumida = cantidadConsumida; }

    public int getDiferencia() { return diferencia; }
    public void setDiferencia(int diferencia) { this.diferencia = diferencia; }
}
