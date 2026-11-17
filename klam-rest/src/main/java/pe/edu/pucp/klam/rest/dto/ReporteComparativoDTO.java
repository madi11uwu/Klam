package pe.edu.pucp.klam.rest.dto;

import java.util.ArrayList;
import java.util.List;

/** RF19: respuesta JSON del comparativo de consumo por cirugia. */
public class ReporteComparativoDTO {
    private String generado;
    private String desde;
    private String hasta;
    private Integer idCirugia;
    private String tipoCliente;
    private Integer idCliente;
    private List<CirugiaComparativaDTO> cirugias = new ArrayList<>();

    public ReporteComparativoDTO() {
    }

    public ReporteComparativoDTO(String generado, String desde, String hasta, Integer idCirugia,
                                 String tipoCliente, Integer idCliente, List<CirugiaComparativaDTO> cirugias) {
        this.generado = generado;
        this.desde = desde;
        this.hasta = hasta;
        this.idCirugia = idCirugia;
        this.tipoCliente = tipoCliente;
        this.idCliente = idCliente;
        this.cirugias = cirugias;
    }

    public String getGenerado() { return generado; }
    public String getDesde() { return desde; }
    public String getHasta() { return hasta; }
    public Integer getIdCirugia() { return idCirugia; }
    public String getTipoCliente() { return tipoCliente; }
    public Integer getIdCliente() { return idCliente; }
    public List<CirugiaComparativaDTO> getCirugias() { return cirugias; }
}