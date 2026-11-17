package pe.edu.pucp.klam.rest.reportes;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pe.edu.pucp.klam.bl.reportes.CirugiaComparativa;
import pe.edu.pucp.klam.bl.reportes.LineaComparativa;
import pe.edu.pucp.klam.bl.reportes.ReporteComparativo;
import pe.edu.pucp.klam.bl.reportes.ReporteUsoInsumos;
import pe.edu.pucp.klam.bl.reportes.ResumenUsoInsumo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** Genera los reportes RF18 y RF19 en formato .xlsx (Apache POI). */
public final class ExportadorExcel {

    private ExportadorExcel() {
    }

    public static byte[] usoInsumos(ReporteUsoInsumos reporte, CabeceraReporte cabecera) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos e = new Estilos(libro);
            Sheet hoja = libro.createSheet("Uso de insumos");
            int fila = escribirCabecera(hoja, 0, cabecera, e);

            fila = escribirEncabezadoTabla(hoja, fila, e, "Insumo", "Medida", "Tipo de cirugía",
                    "Cantidad utilizada", "Frecuencia de uso");
            for (ResumenUsoInsumo insumo : reporte.insumos()) {
                Row r = hoja.createRow(fila++);
                celda(r, 0, insumo.nombre(), e.normal);
                celda(r, 1, insumo.medida(), e.normal);
                celda(r, 2, insumo.tipoProcedimiento(), e.normal);
                celda(r, 3, insumo.cantidadUtilizada(), e.numero);
                celda(r, 4, insumo.frecuencia(), e.numero);
            }
            if (reporte.insumos().isEmpty()) {
                Row r = hoja.createRow(fila);
                celda(r, 0, "No hay consumos registrados con los criterios seleccionados.", e.normal);
            }
            ajustar(hoja, 5);
            libro.write(salida);
            return salida.toByteArray();
        }
    }

    public static byte[] comparativo(ReporteComparativo reporte, CabeceraReporte cabecera) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos e = new Estilos(libro);
            Sheet hoja = libro.createSheet("Comparativo");
            int fila = escribirCabecera(hoja, 0, cabecera, e);

            if (reporte.cirugias().isEmpty()) {
                Row r = hoja.createRow(fila);
                celda(r, 0, "No hay cirugías con consumo completo con los criterios seleccionados.", e.normal);
            }
            for (CirugiaComparativa c : reporte.cirugias()) {
                fila++;
                Row titulo = hoja.createRow(fila++);
                celda(titulo, 0, "CIR-" + String.format("%03d", c.idCirugia()) + " · " + c.procedimiento(), e.subtitulo);
                Row datos = hoja.createRow(fila++);
                celda(datos, 0, "Fecha: " + fecha(c.fecha()) + "   Paciente: " + vacio(c.paciente())
                        + "   Médico: " + vacio(c.medico()) + "   Cliente: " + vacio(c.cliente()), e.normal);
                fila = escribirEncabezadoTabla(hoja, fila, e, "Insumo", "Medida", "Despachada", "Consumida", "Diferencia");
                for (LineaComparativa l : c.lineas()) {
                    Row r = hoja.createRow(fila++);
                    celda(r, 0, l.nombre(), e.normal);
                    celda(r, 1, l.medida(), e.normal);
                    celda(r, 2, l.despachada(), e.numero);
                    celda(r, 3, l.consumida(), e.numero);
                    celda(r, 4, l.diferencia(), e.numero);
                }
            }
            ajustar(hoja, 5);
            libro.write(salida);
            return salida.toByteArray();
        }
    }

    // ---------- Auxiliares ----------

    private static int escribirCabecera(Sheet hoja, int fila, CabeceraReporte cabecera, Estilos e) {
        Row titulo = hoja.createRow(fila++);
        celda(titulo, 0, cabecera.titulo(), e.titulo);
        celda(hoja.createRow(fila++), 0, "Generado: " + cabecera.generado(), e.normal);
        celda(hoja.createRow(fila++), 0, "Periodo: " + cabecera.periodo(), e.normal);
        for (String criterio : cabecera.criterios()) {
            celda(hoja.createRow(fila++), 0, criterio, e.normal);
        }
        return fila + 1;
    }

    private static int escribirEncabezadoTabla(Sheet hoja, int fila, Estilos e, String... titulos) {
        Row r = hoja.createRow(fila);
        for (int i = 0; i < titulos.length; i++) {
            celda(r, i, titulos[i], e.encabezado);
        }
        return fila + 1;
    }

    private static void celda(Row fila, int columna, String texto, CellStyle estilo) {
        var c = fila.createCell(columna);
        c.setCellValue(texto == null ? "" : texto);
        c.setCellStyle(estilo);
    }

    private static void celda(Row fila, int columna, int valor, CellStyle estilo) {
        var c = fila.createCell(columna);
        c.setCellValue(valor);
        c.setCellStyle(estilo);
    }

    private static void ajustar(Sheet hoja, int columnas) {
        for (int i = 0; i < columnas; i++) {
            hoja.autoSizeColumn(i);
        }
    }

    private static String fecha(java.time.LocalDateTime f) {
        return f == null ? "—" : String.format("%02d/%02d/%d", f.getDayOfMonth(), f.getMonthValue(), f.getYear());
    }

    private static String vacio(String texto) {
        return texto == null || texto.isBlank() ? "—" : texto;
    }

    private static final class Estilos {
        final CellStyle titulo;
        final CellStyle subtitulo;
        final CellStyle encabezado;
        final CellStyle normal;
        final CellStyle numero;

        Estilos(XSSFWorkbook libro) {
            Font negritaGrande = libro.createFont();
            negritaGrande.setBold(true);
            negritaGrande.setFontHeightInPoints((short) 14);
            titulo = libro.createCellStyle();
            titulo.setFont(negritaGrande);

            Font negrita = libro.createFont();
            negrita.setBold(true);
            subtitulo = libro.createCellStyle();
            subtitulo.setFont(negrita);

            encabezado = libro.createCellStyle();
            encabezado.setFont(negrita);
            encabezado.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            encabezado.setBorderBottom(BorderStyle.THIN);

            normal = libro.createCellStyle();

            numero = libro.createCellStyle();
            numero.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.RIGHT);
        }
    }
}