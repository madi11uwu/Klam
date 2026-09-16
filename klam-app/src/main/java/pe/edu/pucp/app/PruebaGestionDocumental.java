package pe.edu.pucp.app;

import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.OrdenCompra;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.TipoDocumentoIngreso;

import java.time.LocalDateTime;

public class PruebaGestionDocumental {

    private static int verificaciones = 0;
    private static int fallos = 0;

    public static void ejecutar() {
        verificaciones = 0;
        fallos = 0;

        titulo("MODULO DE GESTION DOCUMENTAL DE INGRESO - ROL 5");

        probarInstanciacionYValidacion();
        probarCopiaDefensiva();
        probarExcepciones();

        resumen();
    }

    private static void probarInstanciacionYValidacion() {
        titulo("1. Instanciacion de OrdenCompra y DocumentoIngreso");

        OrdenCompra orden = new OrdenCompra();
        orden.setIdOrdenCompra("OC-2026-001");
        orden.setArchivoRespaldoPath("/archivos/ordenes/OC001.pdf");
        orden.setFechaRecepcion(LocalDateTime.now());

        verificar("Orden de Compra es verificable si tiene path", true, orden.verificar());

        DocumentoIngreso doc = new DocumentoIngreso();
        doc.setId_documento("DOC-1001");
        doc.setTipoDocumento(TipoDocumentoIngreso.ORDEN_COMPRA);
        doc.setArchivoPath("/archivos/docs/DOC1001.pdf");
        doc.setEstadoValidacion("VALIDADO");
        doc.setFechaCarga(LocalDateTime.now());
        doc.setOrdenCompra(orden);

        verificar("El documento ingresado es valido", true, doc.validar());
        verificar("El documento asigna correctamente el tipo", TipoDocumentoIngreso.ORDEN_COMPRA, doc.getTipoDocumento());
        verificar("Asocia correctamente el ID de la Orden de Compra", "OC-2026-001", doc.getOrdenCompra().getIdOrdenCompra());
    }

    private static void probarCopiaDefensiva() {
        titulo("2. Prueba de Copia Defensiva");

        OrdenCompra ordenOriginal = new OrdenCompra();
        ordenOriginal.setIdOrdenCompra("OC-ORIGINAL");
        ordenOriginal.setArchivoRespaldoPath("/path/original.pdf");
        ordenOriginal.setFechaRecepcion(LocalDateTime.now());

        DocumentoIngreso doc = new DocumentoIngreso();
        doc.setId_documento("DOC-2002");
        doc.setTipoDocumento(TipoDocumentoIngreso.ORDEN_COMPRA);
        doc.setArchivoPath("/path/doc.pdf");
        doc.setEstadoValidacion("VALIDADO");
        doc.setFechaCarga(LocalDateTime.now());
        doc.setOrdenCompra(ordenOriginal);

        // Modificamos el objeto original externamente
        ordenOriginal.setIdOrdenCompra("OC-MODIFICADA");

        verificar("La copia interna mantiene el ID intacto (Copia Defensiva)",
                "OC-ORIGINAL", doc.getOrdenCompra().getIdOrdenCompra());
    }

    private static void probarExcepciones() {
        titulo("3. Validaciones de limites y valores nulos");

        try {
            DocumentoIngreso docInvalido = new DocumentoIngreso();
            docInvalido.setId_documento("");
            registrarFallo("No debe permitir ID vacio", "acepto ID vacio");
        } catch (IllegalArgumentException e) {
            verificar("Rechaza identificador vacio correctamente", true, true);
        }
    }

    // -----------------------------------------------------------------
    // Utilidades
    // -----------------------------------------------------------------
    private static void verificar(String descripcion, Object esperado, Object obtenido) {
        verificaciones++;
        boolean ok = esperado.equals(obtenido);
        if (ok) {
            System.out.println("   [OK]     " + descripcion);
        } else {
            fallos++;
            System.out.println("   [FALLO]  " + descripcion + " (esperado: " + esperado + ", obtenido: " + obtenido + ")");
        }
    }

    private static void registrarFallo(String descripcion, String detalle) {
        verificaciones++;
        fallos++;
        System.out.println("   [FALLO]  " + descripcion + "  (" + detalle + ")");
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println(" " + texto);
        System.out.println("=".repeat(70));
    }

    private static void resumen() {
        titulo("RESUMEN ROL 5");
        System.out.println(" Verificaciones ejecutadas: " + verificaciones);
        System.out.println(" Fallos: " + fallos);
        System.out.println(fallos == 0
                ? " RESULTADO: El dominio de gestion documental funciona correctamente."
                : " RESULTADO: Hay fallos en el modulo de gestion documental.");
        System.out.println();
    }
}