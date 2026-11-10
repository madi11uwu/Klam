package pe.edu.pucp.klam.rest.mapper;

import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;
import pe.edu.pucp.klam.rest.dto.BandejaDTO;
import pe.edu.pucp.klam.rest.dto.BandejaFormDTO;
import pe.edu.pucp.klam.rest.dto.ConsumibleBandejaDTO;
import pe.edu.pucp.klam.rest.dto.ConsumibleDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BandejaMapper {
    private BandejaMapper() {
    }

    // Modelo -> DTO (con los consumibles que tenga cargados)
    public static BandejaDTO toDTO(BandejaInstrumental bandeja) {
        BandejaDTO dto = new BandejaDTO();
        dto.setIdBandeja(bandeja.getIdBandeja());
        dto.setTipo(bandeja.getTipo());
        dto.setProcedimiento(bandeja.getProcedimiento());
        dto.setEsterilizado(bandeja.isEsterilizado());
        dto.setActivo(bandeja.isActivo());
        for (Map.Entry<Consumible, Integer> e : bandeja.getConsumibles().entrySet()) {
            ConsumibleBandejaDTO c = new ConsumibleBandejaDTO();
            c.setIdConsumible(e.getKey().getIdConsumible());
            c.setNombreComercial(e.getKey().getNombreComercial());
            c.setMedida(e.getKey().getMedida());
            c.setCantidad(e.getValue());
            dto.getConsumibles().add(c);
        }
        return dto;
    }

    public static List<BandejaDTO> toDTOs(List<BandejaInstrumental> bandejas) {
        List<BandejaDTO> dtos = new ArrayList<>();
        for (BandejaInstrumental bandeja : bandejas) {
            dtos.add(toDTO(bandeja));
        }
        return dtos;
    }

    // Detalle de BandejaInstrumentalBL.consultarDetalle -> DTO
    @SuppressWarnings("unchecked")
    public static BandejaDTO toDTO(Map<String, Object> detalle) {
        BandejaDTO dto = new BandejaDTO();
        dto.setIdBandeja(((Number) detalle.get("idBandeja")).intValue());
        dto.setTipo((String) detalle.get("tipo"));
        dto.setProcedimiento((String) detalle.get("procedimiento"));
        dto.setEsterilizado(Boolean.TRUE.equals(detalle.get("esterilizado")));
        dto.setActivo(Boolean.TRUE.equals(detalle.get("activo")));
        Object idCirugia = detalle.get("idCirugia");
        dto.setIdCirugia(idCirugia instanceof Number n ? n.intValue() : null);
        for (Map<String, Object> fila : (List<Map<String, Object>>) detalle.get("consumibles")) {
            ConsumibleBandejaDTO c = new ConsumibleBandejaDTO();
            c.setIdConsumible(((Number) fila.get("idConsumible")).intValue());
            c.setNombreComercial((String) fila.get("nombreComercial"));
            c.setMedida((String) fila.get("medida"));
            c.setCantidad(((Number) fila.get("cantidadDespachada")).intValue());
            dto.getConsumibles().add(c);
        }
        return dto;
    }

    public static BandejaInstrumental toModelo(BandejaFormDTO dto) {
        BandejaInstrumental bandeja = new BandejaInstrumental();
        bandeja.setTipo(dto.getTipo());
        bandeja.setProcedimiento(dto.getProcedimiento());
        bandeja.setEsterilizado(dto.isEsterilizado());
        return bandeja;
    }

    public static ConsumibleDTO toDTO(Consumible consumible) {
        ConsumibleDTO dto = new ConsumibleDTO();
        dto.setIdConsumible(consumible.getIdConsumible());
        dto.setNombreComercial(consumible.getNombreComercial());
        dto.setMarca(consumible.getMarca());
        dto.setMedida(consumible.getMedida());
        dto.setActivo(consumible.isActivo());
        return dto;
    }
}
