package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.AuditoriaCirugiaBL;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.dao.CirugiaDAO;
import pe.edu.pucp.klam.dao.impl.CirugiaDAOImpl;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AuditoriaCirugiaBLImpl implements AuditoriaCirugiaBL {
    private final CirugiaDAO cirugiaDAO = new CirugiaDAOImpl();

    // ------------------------------------------------------------------
    // Bitacora de auditoria (RF16)
    // ------------------------------------------------------------------
    @Override
    public List<Map<String, Object>> listarBitacora() throws BLException {
        return buscarBitacora(null, null, null, null, null);
    }

    @Override
    public List<Map<String, Object>> listarBitacoraPorCirugia(int idCirugia) throws BLException {
        if (idCirugia <= 0) {
            throw new BLException("El id de la cirugía no es válido");
        }
        return buscarBitacora(idCirugia, null, null, null, null);
    }

    @Override
    public List<Map<String, Object>> buscarBitacora(Integer idCirugia, LocalDateTime desde, LocalDateTime hasta,
                                                    String accion, String responsable) throws BLException {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BLException("El inicio del rango no puede ser posterior al fin");
        }
        try {
            List<Map<String, Object>> filas = new ArrayList<>();
            for (Map<String, Object> f : cirugiaDAO.buscarAuditoria(null, idCirugia, desde, hasta, accion, responsable)) {
                filas.add(aFilaBitacora(f, false));
            }
            return filas;
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar la bitácora de auditoría", e);
        }
    }

    @Override
    public Map<String, Object> obtenerEntradaBitacora(int idAuditoria) throws BLException {
        if (idAuditoria <= 0) {
            throw new BLException("El id de la entrada de bitácora no es válido");
        }
        try {
            List<Map<String, Object>> filas = cirugiaDAO.buscarAuditoria(idAuditoria, null, null, null, null, null);
            return filas.isEmpty() ? null : aFilaBitacora(filas.get(0), true);
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar la entrada de bitácora " + idAuditoria, e);
        }
    }

    @Override
    public List<Map<String, Object>> obtenerDetalleBitacora(int idAuditoria) throws BLException {
        Map<String, Object> entrada = obtenerEntradaBitacora(idAuditoria);
        if (entrada == null) {
            throw new BLException("No existe la entrada de bitácora " + idAuditoria);
        }
        Map<String, String> antes = parsear((String) entrada.get("valoresAnteriores"));
        Map<String, String> despues = parsear((String) entrada.get("valoresPosteriores"));

        Set<String> campos = new LinkedHashSet<>(despues.keySet());
        campos.addAll(antes.keySet());
        List<Map<String, Object>> detalle = new ArrayList<>();
        for (String campo : campos) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("campo", campo);
            fila.put("valorAnterior", antes.get(campo));
            fila.put("valorPosterior", despues.get(campo));
            detalle.add(fila);
        }
        return detalle;
    }

    private Map<String, Object> aFilaBitacora(Map<String, Object> datos, boolean conValores) {
        Map<String, Object> fila = new LinkedHashMap<>(datos);
        if (!conValores) {
            fila.remove("valoresAnteriores");
            fila.remove("valoresPosteriores");
        }
        return fila;
    }

    private Map<String, String> parsear(String texto) {
        Map<String, String> valores = new LinkedHashMap<>();
        if (texto == null || texto.isBlank()) {
            return valores;
        }
        for (String linea : texto.split("\n")) {
            int i = linea.indexOf('=');
            if (i > 0) {
                valores.put(linea.substring(0, i), linea.substring(i + 1));
            }
        }
        return valores;
    }

}
