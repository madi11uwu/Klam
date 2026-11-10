package pe.edu.pucp.klam.rest.dto;

import java.util.Map;

// Línea de consumible de la bandeja despachada (nombre, medida y cantidad).
public class ConsumibleDespachadoDTO {
    private Integer idConsumible;
    private String nombre;
    private String medida;
    private int cantidadDespachada;

    public ConsumibleDespachadoDTO() {
    }

    public ConsumibleDespachadoDTO(Map<String, Object> fila) {
        if (fila != null) {
            idConsumible = fila.get("idConsumible") instanceof Number n ? n.intValue() : null;
            nombre = fila.get("nombreComercial") == null ? "" : fila.get("nombreComercial").toString();
            medida = fila.get("medida") == null ? "" : fila.get("medida").toString();
            cantidadDespachada = fila.get("cantidadDespachada") instanceof Number n ? n.intValue() :
                                 fila.get("cantidad") instanceof Number c ? c.intValue() : 0;
        }
    }

    public Integer getIdConsumible() { return idConsumible; }
    public void setIdConsumible(Integer idConsumible) { this.idConsumible = idConsumible; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMedida() { return medida; }
    public void setMedida(String medida) { this.medida = medida; }

    public int getCantidadDespachada() { return cantidadDespachada; }
    public void setCantidadDespachada(int cantidadDespachada) { this.cantidadDespachada = cantidadDespachada; }
}
