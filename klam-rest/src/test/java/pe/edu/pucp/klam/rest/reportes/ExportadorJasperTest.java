package pe.edu.pucp.klam.rest.reportes;

import org.junit.jupiter.api.Test;
import pe.edu.pucp.klam.bl.reportes.CirugiaComparativa;
import pe.edu.pucp.klam.bl.reportes.FiltroConsumo;
import pe.edu.pucp.klam.bl.reportes.LineaComparativa;
import pe.edu.pucp.klam.bl.reportes.ReporteComparativo;
import pe.edu.pucp.klam.bl.reportes.ReporteUsoInsumos;
import pe.edu.pucp.klam.bl.reportes.ResumenUsoInsumo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportadorJasperTest {

    @Test
    void testUsoInsumosConDatos() throws IOException {
        FiltroConsumo filtro = new FiltroConsumo(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                "Traumatología", 1, "Unidad", null, null, null);
        ResumenUsoInsumo insumo = new ResumenUsoInsumo("Gasa estéril 10x10", "Sobre", "Artroscopía de rodilla", 15, 4);
        ReporteUsoInsumos reporte = new ReporteUsoInsumos(LocalDateTime.now(), filtro, 4, List.of(insumo));
        CabeceraReporte cabecera = new CabeceraReporte("Insumos más utilizados (RF18)", "21/11/2026 19:00",
                "01/01/2026 – 31/01/2026", List.of("Tipo de cirugía: Traumatología", "Tipo de consumible: Gasa estéril"));

        byte[] pdf = ExportadorJasper.usoInsumos(reporte, cabecera);
        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
        String cabeceraPdf = new String(pdf, 0, Math.min(pdf.length, 10), StandardCharsets.ISO_8859_1);
        assertTrue(cabeceraPdf.startsWith("%PDF-"));

        com.lowagie.text.pdf.PdfReader reader = new com.lowagie.text.pdf.PdfReader(pdf);
        assertTrue(reader.getNumberOfPages() >= 1);
        reader.close();
    }

    @Test
    void testUsoInsumosVacio() throws IOException {
        FiltroConsumo filtro = new FiltroConsumo(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                null, null, null, null, null, null);
        ReporteUsoInsumos reporte = new ReporteUsoInsumos(LocalDateTime.now(), filtro, 0, List.of());
        CabeceraReporte cabecera = new CabeceraReporte("Insumos más utilizados (RF18)", "21/11/2026 19:00",
                "01/01/2026 – 31/01/2026", List.of("Tipo de cirugía: Todas", "Tipo de consumible: Todos"));

        byte[] pdf = ExportadorJasper.usoInsumos(reporte, cabecera);
        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
        String cabeceraPdf = new String(pdf, 0, Math.min(pdf.length, 10), StandardCharsets.ISO_8859_1);
        assertTrue(cabeceraPdf.startsWith("%PDF-"));

        com.lowagie.text.pdf.PdfReader reader = new com.lowagie.text.pdf.PdfReader(pdf);
        assertEquals(1, reader.getNumberOfPages());
        reader.close();
    }

    @Test
    void testComparativoConDatos() throws IOException {
        FiltroConsumo filtro = new FiltroConsumo(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                null, null, null, 1, null, null);
        LineaComparativa linea = new LineaComparativa("Tornillo interferencial", "Pieza", 2, 2, 0);
        CirugiaComparativa cirugia = new CirugiaComparativa(1, LocalDateTime.of(2026, 1, 15, 10, 0),
                "Reconstrucción LCA", "Juan Pérez", "12345678", "Dr. House", "CLINICA_HOSPITAL",
                "Clínica San Felipe", List.of(linea));
        ReporteComparativo reporte = new ReporteComparativo(LocalDateTime.now(), filtro, List.of(cirugia));
        CabeceraReporte cabecera = new CabeceraReporte("Control de consumo por cirugía (RF19)", "21/11/2026 19:00",
                "01/01/2026 – 31/01/2026", List.of("Cirugía: CIR-001", "Cliente: Clínica San Felipe"));

        byte[] pdf = ExportadorJasper.comparativo(reporte, cabecera);
        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
        String cabeceraPdf = new String(pdf, 0, Math.min(pdf.length, 10), StandardCharsets.ISO_8859_1);
        assertTrue(cabeceraPdf.startsWith("%PDF-"));

        com.lowagie.text.pdf.PdfReader reader = new com.lowagie.text.pdf.PdfReader(pdf);
        assertTrue(reader.getNumberOfPages() >= 1);
        reader.close();
    }

    @Test
    void testComparativoVacio() throws IOException {
        FiltroConsumo filtro = new FiltroConsumo(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                null, null, null, null, null, null);
        ReporteComparativo reporte = new ReporteComparativo(LocalDateTime.now(), filtro, List.of());
        CabeceraReporte cabecera = new CabeceraReporte("Control de consumo por cirugía (RF19)", "21/11/2026 19:00",
                "01/01/2026 – 31/01/2026", List.of("Cirugía: Todas", "Cliente: Todos"));

        byte[] pdf = ExportadorJasper.comparativo(reporte, cabecera);
        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
        String cabeceraPdf = new String(pdf, 0, Math.min(pdf.length, 10), StandardCharsets.ISO_8859_1);
        assertTrue(cabeceraPdf.startsWith("%PDF-"));

        com.lowagie.text.pdf.PdfReader reader = new com.lowagie.text.pdf.PdfReader(pdf);
        assertEquals(1, reader.getNumberOfPages());
        reader.close();
    }
}
