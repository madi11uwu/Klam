package pe.edu.pucp.klam.rest.mapper;

import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;
import pe.edu.pucp.klam.rest.dto.BandejaInstrumentalDTO;
import pe.edu.pucp.klam.rest.dto.LineaConsumibleDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * La bandeja guarda sus consumibles en un Map<Consumible, Integer>. Un Map con objetos como clave
 * no se puede expresar en JSON, así que cada entrada viaja como una LineaConsumibleDTO
 * (consumible + cantidad), igual que las líneas de una orden de venta en la sesión 21.
 */
public final class BandejaInstrumentalMapper {
    private BandejaInstrumentalMapper() {
    }

    public static BandejaInstrumentalDTO toDTO(BandejaInstrumental bandeja) {
        BandejaInstrumentalDTO dto = new BandejaInstrumentalDTO();
        dto.setIdBandeja(bandeja.getIdBandeja());
        dto.setActivo(bandeja.isActivo());
        dto.setTipo(bandeja.getTipo());
        dto.setProcedimiento(bandeja.getProcedimiento());
        dto.setEsterilizado(bandeja.isEsterilizado());
        dto.setConsumibles(toLineas(bandeja.getConsumibles()));
        dto.setConsumiblesConsumidos(toLineas(bandeja.getConsumiblesConsumidos()));
        return dto;
    }

    public static List<BandejaInstrumentalDTO> toDTOs(List<BandejaInstrumental> bandejas) {
        List<BandejaInstrumentalDTO> dtos = new ArrayList<>();
        for (BandejaInstrumental bandeja : bandejas) {
            dtos.add(toDTO(bandeja));
        }
        return dtos;
    }

    public static BandejaInstrumental toModelo(BandejaInstrumentalDTO dto) {
        BandejaInstrumental bandeja = new BandejaInstrumental();
        bandeja.setIdBandeja(dto.getIdBandeja());
        bandeja.setActivo(dto.isActivo());
        bandeja.setTipo(dto.getTipo());
        bandeja.setProcedimiento(dto.getProcedimiento());
        bandeja.setEsterilizado(dto.isEsterilizado());
        bandeja.setConsumibles(toMapa(dto.getConsumibles()));
        bandeja.setConsumiblesConsumidos(toMapa(dto.getConsumiblesConsumidos()));
        return bandeja;
    }

    private static List<LineaConsumibleDTO> toLineas(Map<Consumible, Integer> consumibles) {
        List<LineaConsumibleDTO> lineas = new ArrayList<>();
        if (consumibles != null) {
            for (Map.Entry<Consumible, Integer> e : consumibles.entrySet()) {
                LineaConsumibleDTO linea = new LineaConsumibleDTO();
                linea.setConsumible(ConsumibleMapper.toDTO(e.getKey()));
                linea.setCantidad(e.getValue() == null ? 0 : e.getValue());
                lineas.add(linea);
            }
        }
        return lineas;
    }

    private static Map<Consumible, Integer> toMapa(List<LineaConsumibleDTO> lineas) {
        Map<Consumible, Integer> consumibles = new LinkedHashMap<>();
        if (lineas != null) {
            for (LineaConsumibleDTO linea : lineas) {
                consumibles.put(ConsumibleMapper.toModelo(linea.getConsumible()), linea.getCantidad());
            }
        }
        return consumibles;
    }
}
