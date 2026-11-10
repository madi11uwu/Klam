package pe.edu.pucp.klam.rest.mapper;

import pe.edu.pucp.klam.modelo.agendaoperaciones.CategoriaEquipo;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
import pe.edu.pucp.klam.rest.dto.EquipoDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EquipoMapper {
    private EquipoMapper() {
    }

    public static EquipoDTO toDTO(Equipo equipo) {
        EquipoDTO dto = new EquipoDTO();
        dto.setIdEquipo(equipo.getIdEquipo());
        dto.setActivo(equipo.isActivo());
        dto.setNombre(equipo.getNombre());
        dto.setCategoria(equipo.getCategoria() != null ? equipo.getCategoria().name() : null);
        dto.setDisponible(equipo.isDisponible());

        // Las especificaciones son clave/valor libres (marca, voltaje, version_sw...): viajan como texto.
        Map<String, String> especificaciones = new LinkedHashMap<>();
        if (equipo.getEspecificaciones() != null) {
            for (Map.Entry<String, Object> e : equipo.getEspecificaciones().entrySet()) {
                especificaciones.put(e.getKey(), e.getValue() == null ? "" : e.getValue().toString());
            }
        }
        dto.setEspecificaciones(especificaciones);
        return dto;
    }

    public static List<EquipoDTO> toDTOs(List<Equipo> equipos) {
        List<EquipoDTO> dtos = new ArrayList<>();
        for (Equipo equipo : equipos) {
            dtos.add(toDTO(equipo));
        }
        return dtos;
    }

    public static Equipo toModelo(EquipoDTO dto) {
        Equipo equipo = new Equipo();
        equipo.setIdEquipo(dto.getIdEquipo());
        equipo.setActivo(dto.isActivo());
        equipo.setNombre(dto.getNombre());
        if (dto.getCategoria() != null && !dto.getCategoria().isBlank()) {
            equipo.setCategoria(CategoriaEquipo.valueOf(dto.getCategoria()));
        }
        equipo.setDisponible(dto.isDisponible());
        Map<String, Object> especificaciones = new LinkedHashMap<>();
        if (dto.getEspecificaciones() != null) {
            especificaciones.putAll(dto.getEspecificaciones());
        }
        equipo.setEspecificaciones(especificaciones);
        return equipo;
    }
}
