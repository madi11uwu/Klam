package pe.edu.pucp.klam.rest.dto;

// Una cantidad consumida enviada por la pantalla de registro de consumo.
public class LineaRegistroConsumoDTO {
    private Integer idConsumible;
    private Integer cantidadConsumida;

    public LineaRegistroConsumoDTO() {
    }

    public Integer getIdConsumible() { return idConsumible; }
    public void setIdConsumible(Integer idConsumible) { this.idConsumible = idConsumible; }

    public Integer getCantidadConsumida() { return cantidadConsumida; }
    public void setCantidadConsumida(Integer cantidadConsumida) { this.cantidadConsumida = cantidadConsumida; }
}
