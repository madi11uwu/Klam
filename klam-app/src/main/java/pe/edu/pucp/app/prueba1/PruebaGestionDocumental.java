package pe.edu.pucp.app.prueba1;

import java.time.LocalDateTime;
import java.util.List;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.DocumentoIngresoBL;
import pe.edu.pucp.klam.bl.OrdenCompraBL;
import pe.edu.pucp.klam.bl.impl.DocumentoIngresoBLImpl;
import pe.edu.pucp.klam.bl.impl.OrdenCompraBLImpl;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.EstadoValidacionDocumento;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.LineaOrdenCompra;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.OrdenCompra;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.TipoDocumentoIngreso;

public class PruebaGestionDocumental {

    private static int verificaciones = 0;
    private static int fallos = 0;

    private static final OrdenCompraBL ordenCompraBL = new OrdenCompraBLImpl();
    private static final DocumentoIngresoBL documentoIngresoBL = new DocumentoIngresoBLImpl();

    public static void ejecutar() {
        verificaciones = 0;
        fallos = 0;

        titulo("MODULO DE GESTION DOCUMENTAL DE INGRESO - ROL 5");

        probarInstanciacionYValidacion();
        probarCopiaDefensiva();
        probarCRUDOrdenCompraYDocumentoBL();

        resumen();
    }

    private static void probarInstanciacionYValidacion() {
        titulo("1. Instanciacion de OrdenCompra y DocumentoIngreso");

        OrdenCompra orden = new OrdenCompra();
        orden.setIdOrdenCompra(1);
        orden.setArchivoRespaldoPath("/archivos/ordenes/OC001.pdf");
        orden.setFechaRecepcion(LocalDateTime.now());

        verificar("Orden de Compra es verificable si tiene path de respaldo", true, orden.verificar());

        DocumentoIngreso doc = new DocumentoIngreso();
        doc.setId_documento(1001);
        doc.setTipoDocumento(TipoDocumentoIngreso.ORDEN_COMPRA);
        doc.setArchivoPath("/archivos/docs/DOC1001.pdf");
        doc.setEstadoValidacion(EstadoValidacionDocumento.VALIDADO);
        doc.setFechaCarga(LocalDateTime.now());
        doc.setOrdenCompra(orden);

        verificar("El documento ingresado es valido", true, doc.validar());
        verificar("El documento asigna correctamente el tipo", TipoDocumentoIngreso.ORDEN_COMPRA, doc.getTipoDocumento());
        verificar("Asocia correctamente el ID de la Orden de Compra", Integer.valueOf(1), doc.getOrdenCompra().getIdOrdenCompra());
    }

    private static void probarCopiaDefensiva() {
        titulo("2. Prueba de Copia Defensiva");

        OrdenCompra ordenOriginal = new OrdenCompra();
        ordenOriginal.setIdOrdenCompra(200);
        ordenOriginal.setArchivoRespaldoPath("/path/original.pdf");
        ordenOriginal.setFechaRecepcion(LocalDateTime.now());

        DocumentoIngreso doc = new DocumentoIngreso();
        doc.setId_documento(2002);
        doc.setTipoDocumento(TipoDocumentoIngreso.ORDEN_COMPRA);
        doc.setArchivoPath("/path/doc.pdf");
        doc.setEstadoValidacion(EstadoValidacionDocumento.VALIDADO);
        doc.setFechaCarga(LocalDateTime.now());
        doc.setOrdenCompra(ordenOriginal);

        // Modificamos el objeto original externamente
        ordenOriginal.setIdOrdenCompra(999);

        verificar("La copia interna mantiene el ID intacto (Copia Defensiva)",
                Integer.valueOf(200), doc.getOrdenCompra().getIdOrdenCompra());
    }

    private static void probarCRUDOrdenCompraYDocumentoBL() {
        titulo("3. Flujo Lógica de Negocio (BL) - OrdenCompra con Lineas y DocumentoIngreso");

        try {
            // A. Registrar Orden de Compra con líneas de detalle
            OrdenCompra orden = new OrdenCompra();
            orden.setArchivoRespaldoPath("/archivos/ordenes/OC_BL_100.pdf");
            orden.setFechaRecepcion(LocalDateTime.now());

            LineaOrdenCompra linea1 = new LineaOrdenCompra();
            linea1.setCantidad(10);
            linea1.setPrecioUnitario(150.0);
            linea1.setDescripcion("Catéteres guía vasculares");

            LineaOrdenCompra linea2 = new LineaOrdenCompra();
            linea2.setCantidad(5);
            linea2.setPrecioUnitario(320.5);
            linea2.setDescripcion("Kits de sutura quirúrgica");

            orden.getLineasOrdenCompra().add(linea1);
            orden.getLineasOrdenCompra().add(linea2);

            ordenCompraBL.insert(orden);
            verificar("Registro de OrdenCompra transaccional en BL exitoso", true, orden.getIdOrdenCompra() > 0);

            // B. Registrar Documento de Ingreso asociado
            DocumentoIngreso doc = new DocumentoIngreso();
            doc.setTipoDocumento(TipoDocumentoIngreso.ORDEN_COMPRA);
            doc.setArchivoPath("/archivos/docs/DOC_BL_100.pdf");
            doc.setEstadoValidacion(EstadoValidacionDocumento.PENDIENTE);
            doc.setFechaCarga(LocalDateTime.now());
            doc.setOrdenCompra(orden);

            documentoIngresoBL.insert(doc);
            verificar("Registro de DocumentoIngreso en BL exitoso", true, doc.getId_documento() > 0);

            // C. Cambiar estado de validación en la capa BL
            documentoIngresoBL.cambiarEstadoValidacion(doc.getId_documento(), EstadoValidacionDocumento.VALIDADO);
            DocumentoIngreso docConsultado = documentoIngresoBL.findById(doc.getId_documento());
            verificar("Transicion de estado a VALIDADO en BL exitosa",
                    EstadoValidacionDocumento.VALIDADO, docConsultado.getEstadoValidacion());

            // D. Listar órdenes de compra
            List<OrdenCompra> listaOrdenes = ordenCompraBL.findAll();
            verificar("Listado de Ordenes de Compra en BL retorne elementos", true, !listaOrdenes.isEmpty());

        } catch (BLException e) {
            registrarFallo("Excepcion inesperada en la prueba de BL", e.getMessage());
        }

        // E. Prueba de validación de negocio (Rechazo de OrdenCompra sin líneas)
        try {
            OrdenCompra ordenSinLineas = new OrdenCompra();
            ordenSinLineas.setArchivoRespaldoPath("/archivos/ordenes/OC_INVALIDA.pdf");
            ordenSinLineas.setFechaRecepcion(LocalDateTime.now());

            ordenCompraBL.insert(ordenSinLineas);
            registrarFallo("Rechaza OrdenCompra sin lineas", "No lanzo BLException");
        } catch (BLException e) {
            verificar("Rechaza insercion de OrdenCompra sin lineas correctamente", true, true);
        }
    }

    // -----------------------------------------------------------------
    // Utilidades
    // -----------------------------------------------------------------
    private static void verificar(String descripcion, Object esperado, Object obtenido) {
        verificaciones++;
        boolean ok = (esperado == null && obtenido == null) || (esperado != null && esperado.equals(obtenido));
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
                ? " RESULTADO: El modulo de gestion documental funciona correctamente."
                : " RESULTADO: Hay fallos en el modulo de gestion documental.");
        System.out.println();
    }
}