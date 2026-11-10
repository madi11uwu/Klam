package pe.edu.pucp.klam.rest.mapper;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;
import pe.edu.pucp.klam.rest.dto.CirugiaDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Mapper espejo: CirugiaDTO <-> Cirugia. Para lecturas de calendario/lista usar CirugiaConsultaMapper. */
public final class CirugiaMapper {
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private CirugiaMapper() {
    }

    public static CirugiaDTO toDTO(Cirugia cirugia) {
        CirugiaDTO dto = new CirugiaDTO();
        dto.setIdCirugia(cirugia.getIdCirugia());
        dto.setActivo(cirugia.isActivo());
        dto.setFechaHoraInicio(aTexto(cirugia.getFechaHoraInicio()));
        dto.setFechaHoraFin(aTexto(cirugia.getFechaHoraFin()));
        dto.setTipoProcedimiento(cirugia.getTipoProcedimiento());
        dto.setMotivoCancelacion(cirugia.getMotivoCancelacion());
        dto.setDoctorNombre(cirugia.getDoctorNombre());
        dto.setDiagnostico(cirugia.getDiagnostico());
        dto.setEstado(cirugia.getEstado() != null ? cirugia.getEstado().name() : null);
        dto.setIdSolicitudOrigen(cirugia.getIdSolicitudOrigen());
        dto.setConsumoCompleto(cirugia.isConsumoCompleto());
        dto.setEquipo(cirugia.getEquipo() != null ? EquipoMapper.toDTO(cirugia.getEquipo()) : null);
        dto.setBandejaInstrumental(cirugia.getBandejaInstrumental() != null
                ? BandejaInstrumentalMapper.toDTO(cirugia.getBandejaInstrumental()) : null);
        dto.setCliente(cirugia.getCliente() != null ? ClienteMapper.toDTO(cirugia.getCliente()) : null);
        return dto;
    }

    public static List<CirugiaDTO> toDTOs(List<Cirugia> cirugias) {
        List<CirugiaDTO> dtos = new ArrayList<>();
        for (Cirugia cirugia : cirugias) {
            dtos.add(toDTO(cirugia));
        }
        return dtos;
    }

    /** @throws IllegalArgumentException si el estado no existe o la fecha no es ISO-8601. */
    public static Cirugia toModelo(CirugiaDTO dto) {
        Cirugia cirugia = new Cirugia();
        cirugia.setIdCirugia(dto.getIdCirugia());
        cirugia.setActivo(dto.isActivo());
        cirugia.setFechaHoraInicio(aFecha(dto.getFechaHoraInicio()));
        cirugia.setFechaHoraFin(aFecha(dto.getFechaHoraFin()));
        cirugia.setTipoProcedimiento(dto.getTipoProcedimiento());
        cirugia.setMotivoCancelacion(dto.getMotivoCancelacion());
        cirugia.setDoctorNombre(dto.getDoctorNombre());
        cirugia.setDiagnostico(dto.getDiagnostico());
        if (dto.getEstado() != null && !dto.getEstado().isBlank()) {
            cirugia.setEstado(EstadoCirugia.valueOf(dto.getEstado()));
        }
        cirugia.setIdSolicitudOrigen(dto.getIdSolicitudOrigen());
        cirugia.setConsumoCompleto(dto.isConsumoCompleto());
        cirugia.setEquipo(dto.getEquipo() != null ? EquipoMapper.toModelo(dto.getEquipo()) : null);
        cirugia.setBandejaInstrumental(dto.getBandejaInstrumental() != null
                ? BandejaInstrumentalMapper.toModelo(dto.getBandejaInstrumental()) : null);
        cirugia.setCliente(dto.getCliente() != null ? ClienteMapper.toModelo(dto.getCliente()) : null);
        return cirugia;
    }

    private static String aTexto(LocalDateTime fecha) {
        return fecha == null ? null : ISO.format(fecha);
    }

    private static LocalDateTime aFecha(String texto) {
        return texto == null || texto.isBlank() ? null : LocalDateTime.parse(texto.trim());
    }
}
