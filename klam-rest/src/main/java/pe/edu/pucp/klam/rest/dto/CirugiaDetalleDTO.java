package pe.edu.pucp.klam.rest.dto;

/**
 * Detalle de una cirugía (RF06): hereda los campos del resumen y agrega el equipo completo
 * (con sus especificaciones), su marca y la bandeja con los consumibles despachados y consumidos.
 */
public class CirugiaDetalleDTO extends CirugiaResumenDTO {
    private EquipoDTO equipo;
    private String equipoMarca;
    private BandejaInstrumentalDTO bandejaInstrumental;
    // El detalle solo se entrega para cirugías vigentes (las eliminadas responden 404).
    // Se incluye porque las pantallas de gestión (RF02) lo consultan.
    private boolean activo = true;

    public CirugiaDetalleDTO() {
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public EquipoDTO getEquipo() {
        return equipo;
    }

    public void setEquipo(EquipoDTO equipo) {
        this.equipo = equipo;
    }

    public String getEquipoMarca() {
        return equipoMarca;
    }

    public void setEquipoMarca(String equipoMarca) {
        this.equipoMarca = equipoMarca;
    }

    public BandejaInstrumentalDTO getBandejaInstrumental() {
        return bandejaInstrumental;
    }

    public void setBandejaInstrumental(BandejaInstrumentalDTO bandejaInstrumental) {
        this.bandejaInstrumental = bandejaInstrumental;
    }
}
