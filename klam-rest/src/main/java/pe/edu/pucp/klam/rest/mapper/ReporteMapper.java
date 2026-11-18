package pe.edu.pucp.klam.rest.mapper;

import pe.edu.pucp.klam.bl.reportes.CirugiaComparativa;
import pe.edu.pucp.klam.bl.reportes.FiltroConsumo;
import pe.edu.pucp.klam.bl.reportes.LineaComparativa;
import pe.edu.pucp.klam.bl.reportes.ReporteComparativo;
import pe.edu.pucp.klam.bl.reportes.ReporteUsoInsumos;
import pe.edu.pucp.klam.bl.reportes.ResumenUsoInsumo;
import pe.edu.pucp.klam.rest.dto.CirugiaComparativaDTO;
import pe.edu.pucp.klam.rest.dto.LineaComparativaDTO;
import pe.edu.pucp.klam.rest.dto.ReporteComparativoDTO;
import pe.edu.pucp.klam.rest.dto.ReporteUsoInsumosDTO;
import pe.edu.pucp.klam.rest.dto.ResumenUsoInsumoDTO;
import pe.edu.pucp.klam.rest.reportes.CabeceraReporte;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Convierte los resultados de BL a DTO JSON y arma el encabezado de los archivos exportados. */
public final class ReporteMapper {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ReporteMapper() {
    }

    // ---------- JSON ----------

    public static ReporteUsoInsumosDTO toDTO(ReporteUsoInsumos r, String nombreConsumible) {
        List<ResumenUsoInsumoDTO> insumos = new ArrayList<>();
        for (ResumenUsoInsumo i : r.insumos()) {
            insumos.add(new ResumenUsoInsumoDTO(i.nombre(), i.medida(), i.tipoProcedimiento(),
                    i.cantidadUtilizada(), i.frecuencia()));
        }
        FiltroConsumo f = r.filtro();
        return new ReporteUsoInsumosDTO(iso(r.generado()), dia(f.desde()), dia(f.hasta()),
                f.tipoProcedimiento(), f.medida(), nombreConsumible, r.cirugiasConsideradas(), insumos);
    }

    public static ReporteComparativoDTO toDTO(ReporteComparativo r) {
        List<CirugiaComparativaDTO> cirugias = new ArrayList<>();
        for (CirugiaComparativa c : r.cirugias()) {
            List<LineaComparativaDTO> lineas = new ArrayList<>();
            for (LineaComparativa l : c.lineas()) {
                lineas.add(new LineaComparativaDTO(l.nombre(), l.medida(), l.despachada(), l.consumida(),
                        l.diferencia()));
            }
            cirugias.add(new CirugiaComparativaDTO(c.idCirugia(), iso(c.fecha()), c.procedimiento(), c.paciente(),
                    c.documentoPaciente(), c.medico(), c.tipoCliente(), c.cliente(), lineas));
        }
        FiltroConsumo f = r.filtro();
        return new ReporteComparativoDTO(iso(r.generado()), dia(f.desde()), dia(f.hasta()), f.idCirugia(),
                f.tipoCliente(), f.idCliente(), cirugias);
    }

    // ---------- Encabezados de exportacion ----------

    public static CabeceraReporte cabeceraUso(ReporteUsoInsumos r, String nombreConsumible) {
        FiltroConsumo f = r.filtro();
        List<String> criterios = List.of(
                "Tipo de cirugía: " + textoODefecto(f.tipoProcedimiento(), "Todas"),
                "Tipo de consumible: " + textoODefecto(nombreConsumible, "Todos"),
                "Medida: " + textoODefecto(f.medida(), "Todas"),
                "Solo cirugías finalizadas con consumo completo. Cirugías consideradas: "
                        + r.cirugiasConsideradas());
        return new CabeceraReporte("Insumos más utilizados (RF18)", fechaHora(r.generado()),
                periodo(f), criterios);
    }

    public static CabeceraReporte cabeceraComparativo(ReporteComparativo r, String nombreCliente) {
        FiltroConsumo f = r.filtro();
        List<String> criterios = List.of(
                "Cirugía: " + (f.idCirugia() == null ? "Todas" : "CIR-" + String.format("%03d", f.idCirugia())),
                "Cliente: " + textoODefecto(nombreCliente, "Todos"),
                "Solo cirugías finalizadas con consumo completo. Cirugías incluidas: " + r.cirugias().size());
        return new CabeceraReporte("Control de consumo por cirugía (RF19)", fechaHora(r.generado()),
                periodo(f), criterios);
    }

    // ---------- Auxiliares ----------

    private static String periodo(FiltroConsumo f) {
        String desde = f.desde() == null ? "inicio" : DIA.format(f.desde());
        String hasta = f.hasta() == null ? "hoy" : DIA.format(f.hasta());
        return desde + " – " + hasta;
    }

    private static String fechaHora(LocalDateTime f) {
        return f == null ? "" : f.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private static String iso(LocalDateTime f) {
        return f == null ? null : f.format(ISO);
    }

    private static String dia(LocalDate d) {
        return d == null ? null : d.toString();
    }

    private static String textoODefecto(String texto, String porDefecto) {
        return texto == null || texto.isBlank() ? porDefecto : texto;
    }
}