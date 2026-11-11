package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.BandejaInstrumentalBL;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.EquipoBL;
import pe.edu.pucp.klam.bl.impl.BandejaInstrumentalBLImpl;
import pe.edu.pucp.klam.bl.impl.CirugiaBLImpl;
import pe.edu.pucp.klam.bl.impl.EquipoBLImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;
import pe.edu.pucp.klam.rest.dto.CirugiaDespachableDTO;
import pe.edu.pucp.klam.rest.dto.ConsumibleDespachadoDTO;
import pe.edu.pucp.klam.rest.dto.DespachoDTO;
import pe.edu.pucp.klam.rest.dto.DespachoDetalleDTO;
import pe.edu.pucp.klam.rest.dto.NuevoDespachoDTO;

/**
 * RF14: despacho de equipos y kits hacia el establecimiento, retorno al almacén y consulta de
 * despachos pendientes de retorno.
 *
 * GET  /despachos?estado=pendientes|retornados|todos&q=texto   lista (más recientes primero)
 * GET  /despachos/{codigo}                                     detalle con consumibles del kit
 * POST /despachos                  {"idCirugia": n}            registra el despacho (201 {codigo})
 * POST /despachos/{codigo}/retorno                             registra el retorno (204)
 * GET  /despachos/cirugias-despachables                        cirugías programadas/en curso
 * GET  /despachos/disponibilidad                               equipos y bandejas libres
 */

@Path("/despachos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DespachosResource {
    @Context private HttpHeaders headers;
    private Response acceso(String rol) {
        SesionesRest.Sesion sesion = SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (sesion == null) return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Sesión inválida o expirada")).build();
        if (rol != null && !rol.equals(sesion.rol())) return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", "Acceso denegado")).build();
        return null;
    }


    private final EquipoBL equipoBL = new EquipoBLImpl();
    private final CirugiaBL cirugiaBL = new CirugiaBLImpl();
    private final BandejaInstrumentalBL bandejaBL = new BandejaInstrumentalBLImpl();

    @GET
    public Response listar(@QueryParam("estado") String estado, @QueryParam("q") String q) {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        final String filtro = estado == null || estado.isBlank() ? "todos" : estado;
        if (!Set.of("todos", "pendientes", "retornados").contains(filtro)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El estado debe ser 'todos', 'pendientes' o 'retornados'"))
                    .build();
        }
        try {
            List<Map<String, Object>> despachos = "pendientes".equals(filtro)
                    ? equipoBL.listarRetornosPendientes()
                    : equipoBL.listarDespachos();
            Map<Integer, Map<String, Object>> cirugias = indiceCirugias();
            List<DespachoDTO> lista = despachos.stream()
                    .map(d -> new DespachoDTO(d, cirugias.get(entero(d.get("idCirugia")))))
                    .filter(d -> !"retornados".equals(filtro) || !d.isPendienteRetorno())
                    .filter(d -> coincide(d, q))
                    .toList();
            return Response.ok(lista).header("Cache-Control", "no-store").build();
        } catch (BLException ex) {
            return responderError(ex);
        }
    }

    @GET
    @Path("/cirugias-despachables")
    public Response cirugiasDespachables() {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        try {
            List<Map<String, Object>> candidatas = new ArrayList<>();
            candidatas.addAll(cirugiaBL.buscarDetalle(null, null, null, null, null, EstadoCirugia.PROGRAMADA));
            candidatas.addAll(cirugiaBL.buscarDetalle(null, null, null, null, null, EstadoCirugia.EN_PROCESO));

            Map<Integer, Equipo> equipos = new HashMap<>();
            for (Equipo e : equipoBL.findAll()) {
                equipos.put(e.getIdEquipo(), e);
            }
            Set<Integer> conDespachoPendiente = new HashSet<>();
            for (Map<String, Object> d : equipoBL.listarRetornosPendientes()) {
                Integer idCirugia = entero(d.get("idCirugia"));
                if (idCirugia != null) conDespachoPendiente.add(idCirugia);
            }

            candidatas.sort(Comparator.comparing(c -> String.valueOf(c.get("fechaHoraInicio"))));
            List<CirugiaDespachableDTO> lista = new ArrayList<>();
            for (Map<String, Object> c : candidatas) {
                Integer idCirugia = entero(c.get("idCirugia"));
                Integer idEquipo = entero(c.get("idEquipo"));
                Integer idBandeja = entero(c.get("idBandeja"));
                Equipo equipo = idEquipo == null ? null : equipos.get(idEquipo);
                Boolean equipoDisponible = equipo == null ? null : equipo.isDisponible();

                String motivo = null;
                if (idEquipo == null && idBandeja == null) {
                    motivo = "La cirugía no tiene equipo ni bandeja asignados";
                } else if (idCirugia != null && conDespachoPendiente.contains(idCirugia)) {
                    motivo = "Ya tiene un despacho pendiente de retorno";
                } else if (idEquipo != null && (equipo == null || !equipo.isActivo())) {
                    motivo = "El equipo asignado está dado de baja";
                } else if (idEquipo != null && !equipo.isDisponible()) {
                    motivo = "El equipo " + equipo.getNombre() + " no está disponible (ya fue despachado)";
                }
                lista.add(new CirugiaDespachableDTO(c, equipoDisponible, motivo));
            }
            return Response.ok(lista).header("Cache-Control", "no-store").build();
        } catch (BLException ex) {
            return responderError(ex);
        }
    }

    @GET
    @Path("/disponibilidad")
    public Response disponibilidad() {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        try {
            List<Map<String, Object>> equipos = new ArrayList<>();
            for (Equipo e : equipoBL.listarDisponibles()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("idEquipo", e.getIdEquipo());
                fila.put("nombre", e.getNombre());
                fila.put("categoria", e.getCategoria() == null ? null : e.getCategoria().name());
                equipos.add(fila);
            }
            List<Map<String, Object>> bandejas = new ArrayList<>();
            for (BandejaInstrumental b : bandejaBL.listarDisponibles()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("idBandeja", b.getIdBandeja());
                fila.put("tipo", b.getTipo());
                fila.put("procedimiento", b.getProcedimiento());
                bandejas.add(fila);
            }
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("equipos", equipos);
            cuerpo.put("bandejas", bandejas);
            return Response.ok(cuerpo).header("Cache-Control", "no-store").build();
        } catch (BLException ex) {
            return responderError(ex);
        }
    }

    @GET
    @Path("/{codigo}")
    public Response consultar(@PathParam("codigo") String codigo) {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        Response invalido = validarCodigo(codigo);
        if (invalido != null) return invalido;
        try {
            Map<String, Object> despacho = buscarPorCodigo(codigo);
            if (despacho == null) return noExiste(codigo);

            Map<String, Object> cirugia = cirugiaBL.obtenerDetalle(entero(despacho.get("idCirugia")));
            return Response.ok(new DespachoDetalleDTO(despacho, cirugia, consumiblesDelKit(despacho)))
                    .header("Cache-Control", "no-store").build();
        } catch (BLException ex) {
            return responderError(ex);
        }
    }

    @POST
    public Response registrar(NuevoDespachoDTO datos) {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        if (datos == null || datos.getIdCirugia() == null || datos.getIdCirugia() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Debe indicar una cirugía válida"))
                    .build();
        }
        try {
            String codigo = equipoBL.registrarDespacho(datos.getIdCirugia());
            return Response.status(Response.Status.CREATED).entity(Map.of("codigo", codigo)).build();
        } catch (BLException ex) {
            return responderError(ex);
        }
    }

    @POST
    @Path("/{codigo}/retorno")
    @Consumes(MediaType.WILDCARD)
    public Response registrarRetorno(@PathParam("codigo") String codigo) {
        Response acceso = acceso("ADMINISTRADOR");
        if (acceso != null) return acceso;
        Response invalido = validarCodigo(codigo);
        if (invalido != null) return invalido;
        try {
            if (buscarPorCodigo(codigo) == null) return noExiste(codigo);
            equipoBL.registrarRetornoPorCodigo(codigo);
            return Response.noContent().build();
        } catch (BLException ex) {
            return responderError(ex);
        }
    }

    // ---- apoyo ----

    private Map<String, Object> buscarPorCodigo(String codigo) throws BLException {
        try {
            return equipoBL.obtenerDespachoPorCodigo(codigo);
        } catch (BLException ex) {
            if (ex.getCause() == null) return null;
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    private List<ConsumibleDespachadoDTO> consumiblesDelKit(Map<String, Object> despacho) throws BLException {
        Integer idBandeja = entero(despacho.get("idBandeja"));
        List<ConsumibleDespachadoDTO> consumibles = new ArrayList<>();
        if (idBandeja == null) return consumibles;
        Map<String, Object> detalleBandeja = bandejaBL.consultarDetalle(idBandeja);
        if (detalleBandeja != null) {
            Object filas = detalleBandeja.get("consumibles");
            if (filas instanceof List<?> lista) {
                for (Object fila : lista) {
                    consumibles.add(new ConsumibleDespachadoDTO((Map<String, Object>) fila));
                }
            }
        }
        return consumibles;
    }

    private Map<Integer, Map<String, Object>> indiceCirugias() throws BLException {
        Map<Integer, Map<String, Object>> indice = new HashMap<>();
        for (Map<String, Object> c : cirugiaBL.buscarDetalle(null, null, null, null, null, null)) {
            Integer id = entero(c.get("idCirugia"));
            if (id != null) indice.put(id, c);
        }
        return indice;
    }

    private boolean coincide(DespachoDTO d, String q) {
        if (q == null || q.isBlank()) return true;
        String texto = String.join(" ",
                nvl(d.getCodigo()), String.format("CIR-%03d", d.getIdCirugia()), nvl(d.getCliente()),
                nvl(d.getClinica()), nvl(d.getEquipo()), nvl(d.getKit()), nvl(d.getProcedimiento()))
                .toLowerCase(Locale.ROOT);
        return texto.contains(q.trim().toLowerCase(Locale.ROOT));
    }

    private static String nvl(String valor) {
        return valor == null ? "" : valor;
    }

    private static Integer entero(Object valor) {
        return valor instanceof Number n ? Integer.valueOf(n.intValue()) : null;
    }

    private Response validarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank() || codigo.length() > 20) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El código del despacho no es válido"))
                    .build();
        }
        return null;
    }

    private Response noExiste(String codigo) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "No existe un despacho con código " + codigo))
                .build();
    }

    private Response responderError(BLException ex) {
        if (ex.getCause() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", ex.getMessage()))
                .build();
    }
}
