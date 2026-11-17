package pe.edu.pucp.klam.rest.dto;

import java.util.ArrayList;
import java.util.List;

// Cuerpo de PUT /consumos/{idCirugia}. Con confirmar=true el consumo queda completo y ya no se puede editar.
public class RegistroConsumoDTO {
    private int idCirugia;
    private boolean confirmar;
    private List<LineaRegistroConsumoDTO> lineas = new ArrayList<>();

    public RegistroConsumoDTO() {
    }

    public int getIdCirugia() { return idCirugia; }
    public void setIdCirugia(int idCirugia) { this.idCirugia = idCirugia; }

    public boolean isConfirmar() { return confirmar; }
    public void setConfirmar(boolean confirmar) { this.confirmar = confirmar; }

    public List<LineaRegistroConsumoDTO> getLineas() { return lineas; }
    public void setLineas(List<LineaRegistroConsumoDTO> lineas) { this.lineas = lineas; }
}
