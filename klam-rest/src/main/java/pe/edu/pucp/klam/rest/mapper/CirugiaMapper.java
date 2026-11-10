package pe.edu.pucp.klam.rest.mapper;

import pe.edu.pucp.klam.rest.dto.CirugiaResumenDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Convierte filas de CirugiaBL.buscarDetalle en DTOs de cirugia. */
public final class CirugiaMapper {

    private CirugiaMapper() {
    }

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
}
