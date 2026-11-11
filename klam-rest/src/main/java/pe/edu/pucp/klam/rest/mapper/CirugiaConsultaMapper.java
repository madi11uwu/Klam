package pe.edu.pucp.klam.rest.mapper;

import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;
import pe.edu.pucp.klam.rest.dto.CirugiaDetalleDTO;
import pe.edu.pucp.klam.rest.dto.CirugiaResumenDTO;

import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Mapper de proyección (solo lectura). Convierte lo que devuelve la capa de negocio en consultas
 * (listas de Map con claves en camelCase y fechas LocalDateTime) en CirugiaResumenDTO y
 * CirugiaDetalleDTO. No consulta la base de datos: el Resource le entrega los datos ya obtenidos.
 */
public final class CirugiaConsultaMapper {
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private CirugiaConsultaMapper() {
    }

    // ------------------------------------------------------------------
    // Estados
    // ------------------------------------------------------------------

    /** PROGRAMADA -> "Programada", EN_PROCESO -> "En curso", FINALIZADA -> "Culminada", CANCELADA -> "Cancelada". */
    public static String estadoAEtiqueta(EstadoCirugia estado) {
        if (estado == null) {
            return null;
        }
        switch (estado) {
            case PROGRAMADA:
                return "Programada";
            case EN_PROCESO:
                return "En curso";
            case FINALIZADA:
                return "Culminada";
            case CANCELADA:
                return "Cancelada";
            default:
                return estado.name();
        }
    }

    /**
     * Acepta la etiqueta de pantalla ("En curso", "Culminada") o el código ("EN_PROCESO", "FINALIZADA"),
     * sin distinguir mayúsculas ni tildes. Devuelve null si el texto es nulo o vacío (filtro no aplicado).
     *
     * @throws IllegalArgumentException si no corresponde a ningún estado (el Resource responde 400).
     */
    public static EstadoCirugia parsearEstado(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String t = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_');
        switch (t) {
            case "PROGRAMADA":
                return EstadoCirugia.PROGRAMADA;
            case "EN_PROCESO":
            case "EN_CURSO":
                return EstadoCirugia.EN_PROCESO;
            case "FINALIZADA":
            case "CULMINADA":
                return EstadoCirugia.FINALIZADA;
            case "CANCELADA":
                return EstadoCirugia.CANCELADA;
            default:
                throw new IllegalArgumentException("Estado de cirugía no válido: " + texto);
        }
    }

    // ------------------------------------------------------------------
    // Resumen (calendario, lista, historial)
    // ------------------------------------------------------------------

    /** @param fila una fila de buscarDetalle / buscarCalendarioMensual / historialPorCliente. */
    public static CirugiaResumenDTO toResumenDTO(Map<String, Object> fila) {
        CirugiaResumenDTO dto = new CirugiaResumenDTO();
        llenarResumen(dto, fila);
        return dto;
    }

    public static List<CirugiaResumenDTO> toResumenDTOs(List<Map<String, Object>> filas) {
        List<CirugiaResumenDTO> dtos = new ArrayList<>();
        if (filas != null) {
            for (Map<String, Object> fila : filas) {
                dtos.add(toResumenDTO(fila));
            }
        }
        return dtos;
    }

    // ------------------------------------------------------------------
    // Detalle
    // ------------------------------------------------------------------

    /**
     * @param detalle resultado de CirugiaBL.obtenerDetalle(id)
     * @param equipo  Equipo asignado (EquipoBL.findById); null si la cirugía no tiene equipo
     * @param bandeja BandejaInstrumental asignada (BandejaInstrumentalBL.findById); null si no tiene
     */
    public static CirugiaDetalleDTO toDetalleDTO(Map<String, Object> detalle, Equipo equipo,
                                                 BandejaInstrumental bandeja) {
        CirugiaDetalleDTO dto = new CirugiaDetalleDTO();
        llenarResumen(dto, detalle);
        if (equipo != null) {
            dto.setEquipo(EquipoMapper.toDTO(equipo));
            // La marca vive en las especificaciones (clave/valor) del equipo.
            dto.setEquipoMarca(dto.getEquipo().getEspecificaciones().entrySet().stream()
                    .filter(e -> "marca".equalsIgnoreCase(e.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst().orElse(null));
        }
        if (bandeja != null) {
            dto.setBandejaInstrumental(BandejaInstrumentalMapper.toDTO(bandeja));
        }
        return dto;
    }

    // ------------------------------------------------------------------
    // Piezas internas
    // ------------------------------------------------------------------

    /** buscarDetalle y obtenerDetalle comparten estas claves. */
    private static void llenarResumen(CirugiaResumenDTO dto, Map<String, Object> m) {
        if (m == null) {
            return;
        }
        Integer id = entero(m.get("idCirugia"));
        dto.setIdCirugia(id == null ? 0 : id);
        dto.setFechaHoraInicio(fechaIso(m.get("fechaHoraInicio")));
        dto.setFechaHoraFin(fechaIso(m.get("fechaHoraFin")));
        dto.setTipoProcedimiento(texto(m.get("tipoProcedimiento")));
        dto.setDoctorNombre(texto(m.get("doctorNombre")));
        dto.setDiagnostico(texto(m.get("diagnostico")));
        dto.setMotivoCancelacion(texto(m.get("motivoCancelacion")));

        String codigo = texto(m.get("estado"));
        dto.setEstado(codigo);
        dto.setEstadoEtiqueta(etiquetaDeCodigo(codigo));

        String tipoCliente = texto(m.get("tipoCliente"));
        dto.setTipoCliente(tipoCliente);
        dto.setIdCliente(entero(m.get("idCliente")));
        String cliente = texto(m.get("clienteNombre"));
        dto.setClienteNombre(cliente);

        // El paciente solo sale de la solicitud de origen; un paciente particular sin solicitud es su propio paciente.
        String paciente = texto(m.get("pacienteNombre"));
        if (paciente == null && ClienteMapper.TIPO_PARTICULAR.equals(tipoCliente)) {
            paciente = cliente;
        }
        dto.setPacienteNombre(paciente);
        dto.setClinicaNombre(texto(m.get("clinicaNombre")));

        dto.setIdEquipo(entero(m.get("idEquipo")));
        dto.setEquipoNombre(texto(m.get("equipoNombre")));
        dto.setIdBandeja(entero(m.get("idBandeja")));
        dto.setBandejaTipo(texto(m.get("bandejaTipo")));
        dto.setIdSolicitudOrigen(entero(m.get("idSolicitudOrigen")));
        dto.setConsumoCompleto(booleano(m.get("consumoCompleto")));
        dto.setPrecioPactado(decimal(m.get("precioPactado")));
        dto.setEstadoCotizacion(capitalizar(texto(m.get("estadoCotizacion"))));
    }

    private static String etiquetaDeCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        try {
            return estadoAEtiqueta(EstadoCirugia.valueOf(codigo.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            return codigo;
        }
    }

    // JDBC puede devolver Integer, Long, Boolean, BigDecimal, Timestamp, LocalDateTime...

    private static String texto(Object v) {
        return v == null ? null : v.toString();
    }

    private static Integer entero(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        try {
            return Integer.valueOf(v.toString().trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Double decimal(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        try {
            return Double.valueOf(v.toString().trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static boolean booleano(Object v) {
        if (v == null) {
            return false;
        }
        if (v instanceof Boolean) {
            return (Boolean) v;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue() != 0;
        }
        String s = v.toString().trim();
        return "true".equalsIgnoreCase(s) || "1".equals(s);
    }

    private static String fechaIso(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof LocalDateTime) {
            return ISO.format((LocalDateTime) v);
        }
        if (v instanceof Timestamp) {
            return ISO.format(((Timestamp) v).toLocalDateTime());
        }
        return v.toString();
    }

    /** EMITIDA -> "Emitida"; null se mantiene null. */
    private static String capitalizar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        String t = codigo.trim().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }
}
