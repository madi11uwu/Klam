package pe.edu.pucp.klam.rest.mapper;

import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.rest.dto.CirugiaDTO;
import pe.edu.pucp.klam.rest.dto.CirugiaFormDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CirugiaMapper {
    private CirugiaMapper() {
    }

    // Fila de buscarDetalle / obtenerDetalle (claves en camelCase) -> DTO
    public static CirugiaDTO toDTO(Map<String, Object> fila) {
        CirugiaDTO dto = new CirugiaDTO();
        dto.setIdCirugia(entero(fila.get("idCirugia")));
        dto.setEstado(texto(fila.get("estado")));
        dto.setFechaHoraInicio(texto(fila.get("fechaHoraInicio")));
        dto.setFechaHoraFin(texto(fila.get("fechaHoraFin")));
        dto.setTipoProcedimiento(texto(fila.get("tipoProcedimiento")));
        dto.setDoctorNombre(texto(fila.get("doctorNombre")));
        dto.setDiagnostico(texto(fila.get("diagnostico")));
        dto.setMotivoCancelacion(texto(fila.get("motivoCancelacion")));
        dto.setTipoCliente(texto(fila.get("tipoCliente")));
        dto.setIdCliente(enteroONulo(fila.get("idCliente")));
        dto.setClienteNombre(texto(fila.get("clienteNombre")));
        dto.setPacienteNombre(texto(fila.get("pacienteNombre")));
        dto.setClinicaNombre(texto(fila.get("clinicaNombre")));
        dto.setIdEquipo(enteroONulo(fila.get("idEquipo")));
        dto.setEquipoNombre(texto(fila.get("equipoNombre")));
        dto.setIdBandeja(enteroONulo(fila.get("idBandeja")));
        dto.setBandejaTipo(texto(fila.get("bandejaTipo")));
        dto.setIdSolicitudOrigen(enteroONulo(fila.get("idSolicitudOrigen")));
        dto.setPrecioPactado(fila.get("precioPactado") instanceof Number n ? n.doubleValue() : null);
        dto.setEstadoCotizacion(texto(fila.get("estadoCotizacion")));
        // las filas de la búsqueda solo traen cirugías activas
        Object activo = fila.get("activo");
        dto.setActivo(activo == null || Boolean.TRUE.equals(activo)
                || (activo instanceof Number n && n.intValue() == 1));
        return dto;
    }

    public static List<CirugiaDTO> toDTOs(List<Map<String, Object>> filas) {
        List<CirugiaDTO> dtos = new ArrayList<>();
        for (Map<String, Object> fila : filas) {
            dtos.add(toDTO(fila));
        }
        return dtos;
    }

    // Formulario -> modelo; el equipo, la bandeja y el cliente solo llevan su id (la BL valida que existan)
    public static Cirugia toModelo(CirugiaFormDTO dto) {
        Cirugia cirugia = new Cirugia();
        cirugia.setFechaHoraInicio(fecha(dto.getFechaHoraInicio(), "inicio"));
        cirugia.setFechaHoraFin(fecha(dto.getFechaHoraFin(), "fin"));
        cirugia.setTipoProcedimiento(dto.getTipoProcedimiento());
        cirugia.setDoctorNombre(dto.getDoctorNombre());
        cirugia.setDiagnostico(dto.getDiagnostico());
        cirugia.setCliente(cliente(dto.getTipoCliente(), dto.getIdCliente()));

        if (dto.getIdEquipo() != null && dto.getIdEquipo() > 0) {
            Equipo equipo = new Equipo();
            equipo.setIdEquipo(dto.getIdEquipo());
            cirugia.setEquipo(equipo);
        }
        if (dto.getIdBandeja() != null && dto.getIdBandeja() > 0) {
            BandejaInstrumental bandeja = new BandejaInstrumental();
            bandeja.setIdBandeja(dto.getIdBandeja());
            cirugia.setBandejaInstrumental(bandeja);
        }
        return cirugia;
    }

    private static Cliente cliente(String tipoCliente, int idCliente) {
        Cliente cliente;
        if (CirugiaBL.TIPO_CLIENTE_CLINICA.equals(tipoCliente)) {
            cliente = new ClinicaHospital();
        } else if (CirugiaBL.TIPO_CLIENTE_PARTICULAR.equals(tipoCliente)) {
            cliente = new PacienteParticular();
        } else {
            return null;   // la BL responde "La cirugía debe tener un cliente"
        }
        cliente.setIdCliente(idCliente);
        return cliente;
    }

    private static LocalDateTime fecha(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha y hora de " + campo + " no tiene un formato válido");
        }
    }

    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private static int entero(Object valor) {
        return valor instanceof Number n ? n.intValue() : 0;
    }

    private static Integer enteroONulo(Object valor) {
        return valor instanceof Number n ? n.intValue() : null;
    }
}
