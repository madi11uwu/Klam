package pe.edu.pucp.klam.rest.mapper;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.ValidacionException;
import pe.edu.pucp.klam.rest.dto.ConsumoCirugiaDTO;
import pe.edu.pucp.klam.rest.dto.ConsumoDetalleDTO;
import pe.edu.pucp.klam.rest.dto.ConsumoLineaDTO;
import pe.edu.pucp.klam.rest.dto.LineaRegistroConsumoDTO;
import pe.edu.pucp.klam.rest.dto.RegistroConsumoDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ConsumoMapper {
    private ConsumoMapper() {
    }

    public static ConsumoCirugiaDTO toDTO(Map<String, Object> fila) {
        ConsumoCirugiaDTO dto = new ConsumoCirugiaDTO();
        llenarResumen(dto, fila);
        return dto;
    }

    public static List<ConsumoCirugiaDTO> toDTOs(List<Map<String, Object>> filas) {
        List<ConsumoCirugiaDTO> dtos = new ArrayList<>();
        for (Map<String, Object> fila : filas) {
            dtos.add(toDTO(fila));
        }
        return dtos;
    }

    @SuppressWarnings("unchecked")
    public static ConsumoDetalleDTO toDetalleDTO(Map<String, Object> fila) {
        ConsumoDetalleDTO dto = new ConsumoDetalleDTO();
        llenarResumen(dto, fila);
        dto.setDoctorNombre(texto(fila.get("doctorNombre")));
        dto.setPacienteNombre(texto(fila.get("pacienteNombre")));
        dto.setClinicaNombre(texto(fila.get("clinicaNombre")));
        dto.setArchivoOrdenCompra(texto(fila.get("archivoOrdenCompra")));
        dto.setMotivoRechazoOrdenCompra(texto(fila.get("motivoRechazoOrdenCompra")));
        dto.setTotalDespachado(entero(fila.get("totalDespachado")));
        dto.setTotalConsumido(entero(fila.get("totalConsumido")));
        dto.setDiferencia(entero(fila.get("diferencia")));

        List<ConsumoLineaDTO> lineas = new ArrayList<>();
        if (fila.get("consumibles") instanceof List<?> consumibles) {
            for (Object c : consumibles) {
                lineas.add(toLineaDTO((Map<String, Object>) c));
            }
        }
        dto.setConsumibles(lineas);
        return dto;
    }

    public static ConsumoLineaDTO toLineaDTO(Map<String, Object> fila) {
        ConsumoLineaDTO dto = new ConsumoLineaDTO();
        dto.setIdConsumible(entero(fila.get("idConsumible")));
        dto.setNombre(texto(fila.get("nombre")));
        dto.setMedida(texto(fila.get("medida")));
        dto.setCantidadDespachada(entero(fila.get("cantidadDespachada")));
        dto.setCantidadConsumida(entero(fila.get("cantidadConsumida")));
        dto.setDiferencia(entero(fila.get("diferencia")));
        return dto;
    }

    // idConsumible -> cantidad consumida, listo para ConsumoBL.guardar
    public static Map<Integer, Integer> toConsumos(RegistroConsumoDTO dto) throws BLException {
        Map<Integer, Integer> consumos = new LinkedHashMap<>();
        if (dto.getLineas() == null) {
            return consumos;
        }
        for (LineaRegistroConsumoDTO linea : dto.getLineas()) {
            if (linea == null || linea.getIdConsumible() == null || linea.getCantidadConsumida() == null) {
                throw new ValidacionException("Cada línea debe indicar el consumible y la cantidad consumida");
            }
            if (consumos.put(linea.getIdConsumible(), linea.getCantidadConsumida()) != null) {
                throw new ValidacionException("El consumible " + linea.getIdConsumible() + " está repetido");
            }
        }
        return consumos;
    }

    private static void llenarResumen(ConsumoCirugiaDTO dto, Map<String, Object> fila) {
        dto.setIdCirugia(entero(fila.get("idCirugia")));
        dto.setFechaHoraInicio(texto(fila.get("fechaHoraInicio")));
        dto.setTipoProcedimiento(texto(fila.get("tipoProcedimiento")));
        dto.setTipoCliente(texto(fila.get("tipoCliente")));
        dto.setClienteNombre(texto(fila.get("clienteNombre")));
        dto.setIdBandeja(fila.get("idBandeja") instanceof Number n ? Integer.valueOf(n.intValue()) : null);
        dto.setRequiereOrdenCompra(Boolean.TRUE.equals(fila.get("requiereOrdenCompra")));
        dto.setEstadoOrdenCompra(texto(fila.get("estadoOrdenCompra")));
        dto.setDocumentacionCierreCompleta(Boolean.TRUE.equals(fila.get("documentacionCierreCompleta")));
        dto.setEstadoConsumo(texto(fila.get("estadoConsumo")));
    }

    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private static int entero(Object valor) {
        return valor instanceof Number n ? n.intValue() : 0;
    }
}
