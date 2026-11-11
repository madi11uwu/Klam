package pe.edu.pucp.klam.rest.mapper;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.BandejaInstrumentalBL;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.ClinicaHospitalBL;
import pe.edu.pucp.klam.bl.EquipoBL;
import pe.edu.pucp.klam.bl.PacienteParticularBL;
import pe.edu.pucp.klam.bl.impl.BandejaInstrumentalBLImpl;
import pe.edu.pucp.klam.bl.impl.ClinicaHospitalBLImpl;
import pe.edu.pucp.klam.bl.impl.EquipoBLImpl;
import pe.edu.pucp.klam.bl.impl.PacienteParticularBLImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.rest.dto.CirugiaDTO;
import pe.edu.pucp.klam.rest.dto.CirugiaFormDTO;
import pe.edu.pucp.klam.rest.dto.CirugiaResumenDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Convierte filas de CirugiaBL en DTOs de cirugia y modelos para persistencia. */
public final class CirugiaMapper {
    private static final ClinicaHospitalBL clinicaBL = new ClinicaHospitalBLImpl();
    private static final PacienteParticularBL pacienteBL = new PacienteParticularBLImpl();
    private static final EquipoBL equipoBL = new EquipoBLImpl();
    private static final BandejaInstrumentalBL bandejaBL = new BandejaInstrumentalBLImpl();

    private CirugiaMapper() {
    }

    // ── Métodos para el historial de cliente (RF10) ──────────────────────────

    public static CirugiaResumenDTO aResumen(Map<String, Object> fila) {
        CirugiaResumenDTO dto = new CirugiaResumenDTO();
        dto.setIdCirugia(MapperUtil.aEntero(fila.get("idCirugia")));
        dto.setFechaHoraInicio(MapperUtil.aIso(fila.get("fechaHoraInicio")));
        dto.setFechaHoraFin(MapperUtil.aIso(fila.get("fechaHoraFin")));
        dto.setTipoProcedimiento(MapperUtil.aTexto(fila.get("tipoProcedimiento")));
        dto.setDoctorNombre(MapperUtil.aTexto(fila.get("doctorNombre")));
        dto.setMotivoCancelacion(MapperUtil.aTexto(fila.get("motivoCancelacion")));
        dto.setEstado(MapperUtil.aTexto(fila.get("estado")));
        dto.setTipoCliente(MapperUtil.aTexto(fila.get("tipoCliente")));
        dto.setIdCliente(MapperUtil.aEntero(fila.get("idCliente")));
        dto.setClienteNombre(MapperUtil.aTexto(fila.get("clienteNombre")));
        return dto;
    }

    public static List<CirugiaResumenDTO> aResumenes(List<Map<String, Object>> filas) {
        List<CirugiaResumenDTO> salida = new ArrayList<>(filas.size());
        for (Map<String, Object> fila : filas) {
            salida.add(aResumen(fila));
        }
        return salida;
    }

    // ── Métodos de gestión de cirugías (RF02) ───────────────────────────────

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
        dto.setPrecioPactado(fila.get("precioPactado") == null ? null : ((Number) fila.get("precioPactado")).doubleValue());
        dto.setEstadoCotizacion(texto(fila.get("estadoCotizacion")));
        Object activo = fila.get("activo");
        dto.setActivo(activo == null || activo.toString().equals("true") || activo.toString().equals("1"));
        return dto;
    }

    public static List<CirugiaDTO> toDTOs(List<Map<String, Object>> filas) {
        List<CirugiaDTO> dtos = new ArrayList<>();
        for (Map<String, Object> fila : filas) {
            dtos.add(toDTO(fila));
        }
        return dtos;
    }

    // Formulario -> modelo; el cliente, el equipo y la bandeja se buscan por su id
    public static Cirugia toModelo(CirugiaFormDTO dto) throws BLException {
        Cliente cliente = null;
        if (CirugiaBL.TIPO_CLIENTE_CLINICA.equals(dto.getTipoCliente())) {
            cliente = clinicaBL.findById(dto.getIdCliente());
            if (cliente == null) {
                throw new BLException("No existe la clínica u hospital con id " + dto.getIdCliente());
            }
        } else if (CirugiaBL.TIPO_CLIENTE_PARTICULAR.equals(dto.getTipoCliente())) {
            cliente = pacienteBL.findById(dto.getIdCliente());
            if (cliente == null) {
                throw new BLException("No existe el paciente particular con id " + dto.getIdCliente());
            }
        }
        Equipo equipo = null;
        if (dto.getIdEquipo() != null && dto.getIdEquipo() > 0) {
            equipo = equipoBL.findById(dto.getIdEquipo());
            if (equipo == null) {
                throw new BLException("No existe el equipo con id " + dto.getIdEquipo());
            }
        }
        BandejaInstrumental bandeja = null;
        if (dto.getIdBandeja() != null && dto.getIdBandeja() > 0) {
            bandeja = bandejaBL.findById(dto.getIdBandeja());
            if (bandeja == null) {
                throw new BLException("No existe la bandeja instrumental con id " + dto.getIdBandeja());
            }
        }

        Cirugia cirugia = new Cirugia();
        cirugia.setFechaHoraInicio(fecha(dto.getFechaHoraInicio(), "inicio"));
        cirugia.setFechaHoraFin(fecha(dto.getFechaHoraFin(), "fin"));
        cirugia.setTipoProcedimiento(dto.getTipoProcedimiento());
        cirugia.setDoctorNombre(dto.getDoctorNombre());
        cirugia.setDiagnostico(dto.getDiagnostico());
        cirugia.setCliente(cliente);
        cirugia.setEquipo(equipo);
        cirugia.setBandejaInstrumental(bandeja);
        return cirugia;
    }

    private static LocalDateTime fecha(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw new BLException("La fecha y hora de " + campo + " no tiene un formato válido");
        }
    }

    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private static int entero(Object valor) {
        return valor == null ? 0 : ((Number) valor).intValue();
    }

    private static Integer enteroONulo(Object valor) {
        return valor == null ? null : ((Number) valor).intValue();
    }
}
