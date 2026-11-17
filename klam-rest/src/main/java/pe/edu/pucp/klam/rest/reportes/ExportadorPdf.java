package pe.edu.pucp.klam.rest.reportes;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import pe.edu.pucp.klam.bl.reportes.CirugiaComparativa;
import pe.edu.pucp.klam.bl.reportes.LineaComparativa;
import pe.edu.pucp.klam.bl.reportes.ReporteComparativo;
import pe.edu.pucp.klam.bl.reportes.ReporteUsoInsumos;
import pe.edu.pucp.klam.bl.reportes.ResumenUsoInsumo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

/** Genera los reportes RF18 y RF19 en PDF (iText 5, la misma librería usada en el módulo de reportes del curso). */
public final class ExportadorPdf {

    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

    private ExportadorPdf() {
    }

    public static byte[] usoInsumos(ReporteUsoInsumos reporte, CabeceraReporte cabecera) throws IOException {
        return generar(cabecera, documento -> {
            PdfPTable tabla = tabla(new float[]{4f, 1.4f, 3f, 1.6f, 1.6f},
                    "Insumo", "Medida", "Tipo de cirugía", "Cantidad utilizada", "Frecuencia de uso");
            for (ResumenUsoInsumo insumo : reporte.insumos()) {
                fila(tabla, 3, insumo.nombre(), insumo.medida(), insumo.tipoProcedimiento(),
                        String.valueOf(insumo.cantidadUtilizada()), String.valueOf(insumo.frecuencia()));
            }
            if (reporte.insumos().isEmpty()) {
                sinDatos(documento, "No hay consumos registrados con los criterios seleccionados.");
            } else {
                documento.add(tabla);
            }
        });
    }

    public static byte[] comparativo(ReporteComparativo reporte, CabeceraReporte cabecera) throws IOException {
        return generar(cabecera, documento -> {
            if (reporte.cirugias().isEmpty()) {
                sinDatos(documento, "No hay cirugías con consumo completo con los criterios seleccionados.");
                return;
            }
            for (CirugiaComparativa c : reporte.cirugias()) {
                documento.add(new Paragraph(" "));
                documento.add(new Paragraph("CIR-" + String.format("%03d", c.idCirugia()) + " · " + c.procedimiento(),
                        NEGRITA));
                documento.add(new Paragraph("Fecha: " + fecha(c.fecha()) + "   Paciente: " + vacio(c.paciente())
                        + "   Documento: " + vacio(c.documentoPaciente()), NORMAL));
                documento.add(new Paragraph("Médico: " + vacio(c.medico()) + "   Cliente: " + vacio(c.cliente()),
                        NORMAL));
                PdfPTable tabla = tabla(new float[]{4f, 1.4f, 1.8f, 1.8f, 1.8f},
                        "Insumo", "Medida", "Despachada", "Consumida", "Diferencia");
                for (LineaComparativa l : c.lineas()) {
                    fila(tabla, 2, l.nombre(), l.medida(), String.valueOf(l.despachada()),
                            String.valueOf(l.consumida()), String.valueOf(l.diferencia()));
                }
                documento.add(tabla);
            }
        });
    }

    // ---------- Auxiliares ----------

    @FunctionalInterface
    private interface Contenido {
        void escribir(Document documento) throws DocumentException;
    }

    private static byte[] generar(CabeceraReporte cabecera, Contenido contenido) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 36, 36, 48, 36);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();
            documento.add(new Paragraph(cabecera.titulo(), TITULO));
            documento.add(new Paragraph("Generado: " + cabecera.generado(), NORMAL));
            documento.add(new Paragraph("Periodo: " + cabecera.periodo(), NORMAL));
            for (String criterio : cabecera.criterios()) {
                documento.add(new Paragraph(criterio, NORMAL));
            }
            contenido.escribir(documento);
        } catch (DocumentException e) {
            throw new IOException("No se pudo generar el PDF del reporte", e);
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }
        return salida.toByteArray();
    }

    private static PdfPTable tabla(float[] anchos, String... encabezados) throws DocumentException {
        PdfPTable tabla = new PdfPTable(anchos.length);
        tabla.setWidthPercentage(100);
        tabla.setWidths(anchos);
        tabla.setSpacingBefore(6f);
        for (String texto : encabezados) {
            PdfPCell celda = new PdfPCell(new Phrase(texto, NEGRITA));
            celda.setBackgroundColor(BaseColor.LIGHT_GRAY);
            tabla.addCell(celda);
        }
        return tabla;
    }

    /** Agrega una fila; desde la columna primeraNumerica en adelante el valor se alinea a la derecha. */
    private static void fila(PdfPTable tabla, int primeraNumerica, String... valores) {
        for (int i = 0; i < valores.length; i++) {
            PdfPCell celda = new PdfPCell(new Phrase(vacio(valores[i]), NORMAL));
            if (i >= primeraNumerica) {
                celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
            }
            tabla.addCell(celda);
        }
    }

    private static void sinDatos(Document documento, String mensaje) throws DocumentException {
        documento.add(new Paragraph(" "));
        documento.add(new Paragraph(mensaje, NORMAL));
    }

    private static String fecha(LocalDateTime f) {
        return f == null ? "—" : String.format("%02d/%02d/%d", f.getDayOfMonth(), f.getMonthValue(), f.getYear());
    }

    private static String vacio(String texto) {
        return texto == null || texto.isBlank() ? "—" : texto;
    }
}