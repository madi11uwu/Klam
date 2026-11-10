package pe.edu.pucp.klam.rest.mapper;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;
import pe.edu.pucp.klam.rest.dto.ConsumibleDTO;

import java.util.ArrayList;
import java.util.List;

public final class ConsumibleMapper {
    private ConsumibleMapper() {
    }

    public static ConsumibleDTO toDTO(Consumible consumible) {
        ConsumibleDTO dto = new ConsumibleDTO();
        dto.setIdConsumible(consumible.getIdConsumible());
        dto.setActivo(consumible.isActivo());
        dto.setNombreComercial(consumible.getNombreComercial());
        dto.setMarca(consumible.getMarca());
        dto.setMedida(consumible.getMedida());
        return dto;
    }

    public static List<ConsumibleDTO> toDTOs(List<Consumible> consumibles) {
        List<ConsumibleDTO> dtos = new ArrayList<>();
        for (Consumible consumible : consumibles) {
            dtos.add(toDTO(consumible));
        }
        return dtos;
    }

    public static Consumible toModelo(ConsumibleDTO dto) {
        Consumible consumible = new Consumible();
        consumible.setIdConsumible(dto.getIdConsumible());
        consumible.setActivo(dto.isActivo());
        consumible.setNombreComercial(dto.getNombreComercial());
        consumible.setMarca(dto.getMarca());
        consumible.setMedida(dto.getMedida());
        return consumible;
    }
}
