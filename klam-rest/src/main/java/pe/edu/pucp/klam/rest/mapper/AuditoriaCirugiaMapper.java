package pe.edu.pucp.klam.rest.mapper;

import pe.edu.pucp.klam.rest.dto.AuditoriaCirugiaDTO;
import pe.edu.pucp.klam.rest.dto.AuditoriaCirugiaDetalleDTO;
import pe.edu.pucp.klam.rest.dto.CambioAuditoriaDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Convierte filas de AuditoriaCirugiaBL en DTOs de bitacora. */
public final class AuditoriaCirugiaMapper {

    private AuditoriaCirugiaMapper() {
    }

    public static AuditoriaCirugiaDTO aDTO(Map<String, Object> fila) {
        AuditoriaCirugiaDTO dto = new AuditoriaCirugiaDTO();
        dto.setIdAuditoria(MapperUtil.aEntero(fila.get("idAuditoria")));
        dto.setIdCirugia(MapperUtil.aEntero(fila.get("idCirugia")));
        dto.setFechaHora(MapperUtil.aIso(fila.get("fechaHora")));
        dto.setAccion(MapperUtil.aTexto(fila.get("accion")));
        dto.setResponsable(MapperUtil.aTexto(fila.get("responsable")));
        dto.setDetalle(MapperUtil.aTexto(fila.get("detalle")));
        return dto;
    }

    public static List<AuditoriaCirugiaDTO> aDTOs(List<Map<String, Object>> filas) {
        List<AuditoriaCirugiaDTO> salida = new ArrayList<>(filas.size());
        for (Map<String, Object> fila : filas) {
            salida.add(aDTO(fila));
        }
        return salida;
    }

    public static AuditoriaCirugiaDetalleDTO aDetalle(Map<String, Object> entrada,
                                                      List<Map<String, Object>> cambios) {
        AuditoriaCirugiaDetalleDTO dto = new AuditoriaCirugiaDetalleDTO();
        AuditoriaCirugiaDTO base = aDTO(entrada);
        dto.setIdAuditoria(base.getIdAuditoria());
        dto.setIdCirugia(base.getIdCirugia());
        dto.setFechaHora(base.getFechaHora());
        dto.setAccion(base.getAccion());
        dto.setResponsable(base.getResponsable());
        dto.setDetalle(base.getDetalle());

        List<CambioAuditoriaDTO> lista = new ArrayList<>(cambios.size());
        for (Map<String, Object> cambio : cambios) {
            CambioAuditoriaDTO c = new CambioAuditoriaDTO();
            c.setCampo(MapperUtil.aTexto(cambio.get("campo")));
            c.setValorAnterior(MapperUtil.aTexto(cambio.get("valorAnterior")));
            c.setValorPosterior(MapperUtil.aTexto(cambio.get("valorPosterior")));
            lista.add(c);
        }
        dto.setCambios(lista);
        return dto;
    }
}
