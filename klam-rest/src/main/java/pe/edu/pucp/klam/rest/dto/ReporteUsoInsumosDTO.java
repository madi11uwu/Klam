package pe.edu.pucp.klam.rest.dto;

import java.util.ArrayList;
import java.util.List;

/** RF18: respuesta JSON del reporte de uso de insumos (misma informacion que el archivo exportado). */
public class ReporteUsoInsumosDTO {
    private String generado;
    private String desde;
    private String hasta;
    private String tipoCirugia;
    private String medida;
    private String consumible;
    private int cirugiasConsideradas;
    private List<ResumenUsoInsumoDTO> insumos = new ArrayList<>();

    public ReporteUsoInsumosDTO() {
    }

    public ReporteUsoInsumosDTO(String generado, String desde, String hasta, String tipoCirugia, String medida,
                                String consumible, int cirugiasConsideradas, List<ResumenUsoInsumoDTO> insumos) {
        this.generado = generado;
        this.desde = desde;
        this.hasta = hasta;
        this.tipoCirugia = tipoCirugia;
        this.medida = medida;
        this.consumible = consumible;
        this.cirugiasConsideradas = cirugiasConsideradas;
        this.insumos = insumos;
    }

    public String getGenerado() { return generado; }
    public String getDesde() { return desde; }
    public String getHasta() { return hasta; }
    public String getTipoCirugia() { return tipoCirugia; }
    public String getMedida() { return medida; }
    public String getConsumible() { return consumible; }
    public int getCirugiasConsideradas() { return cirugiasConsideradas; }
    public List<ResumenUsoInsumoDTO> getInsumos() { return insumos; }
}