package pe.edu.pucp.app.prueba2;

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
import pe.edu.pucp.klam.modelo.usuariosPermisos.Administrador;

public class PruebaGestionDocumentalCRUD {

    private static int verificaciones = 0;
    private static int fallos = 0;

    private static final OrdenCompraBL ordenCompraBL = new OrdenCompraBLImpl();
    private static final DocumentoIngresoBL documentoIngresoBL = new DocumentoIngresoBLImpl();

    public static void ejecutar() {
        verificaciones = 0;
        fallos = 0;

        titulo("MODULO DE GESTION DOCUMENTAL DE INGRESO - ROL 5 (PRUEBA CRUD)");

        probarInstanciacionYValidacion();
        probarCopiaDefensiva();
        probarCRUDCompleto();

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

        ordenOriginal.setIdOrdenCompra(999);

        verificar("La copia interna mantiene el ID intacto (Copia Defensiva)",
                Integer.valueOf(200), doc.getOrdenCompra().getIdOrdenCompra());
    }

    private static void probarCRUDCompleto() {
        titulo("3. Demostracion de CRUD Completo en Capa Negocio (BL)");

        try {
            // =========================================================
            // A. CREATE (Insertar Orden de Compra + Documento de Ingreso)
            // =========================================================
            OrdenCompra orden = new OrdenCompra();
            orden.setArchivoRespaldoPath("/archivos/ordenes/OC_BL_100.pdf");
            orden.setFechaRecepcion(LocalDateTime.now());

            LineaOrdenCompra linea1 = new LineaOrdenCompra();
            linea1.setCantidad(10);
            linea1.setPrecioUnitario(150.0);
            linea1.setDescripcion("Catéteres guía vasculares");

            orden.setLineasOrdenCompra(List.of(linea1));
            ordenCompraBL.insert(orden);
            int idOrdenGenerado = orden.getIdOrdenCompra();

            verificar("C (Create) - Insercion transaccional de OrdenCompra exitosa", true, idOrdenGenerado > 0);

            DocumentoIngreso doc = new DocumentoIngreso();
            doc.setTipoDocumento(TipoDocumentoIngreso.ORDEN_COMPRA);
            doc.setArchivoPath("/archivos/docs/DOC_BL_100.pdf");
            doc.setEstadoValidacion(EstadoValidacionDocumento.PENDIENTE);
            doc.setFechaCarga(LocalDateTime.now());
            doc.setOrdenCompra(orden);
            Administrador admin = new Administrador();
            admin.setIdUsuario(1); // administrador del DML
            doc.setUsuarioCarga(admin);

            documentoIngresoBL.insert(doc);
            int idDocGenerado = doc.getId_documento();
            verificar("C (Create) - Insercion de DocumentoIngreso exitosa", true, idDocGenerado > 0);

            // =========================================================
            // B. READ (Consultar por ID y Listar)
            // =========================================================
            OrdenCompra ordenObtenida = ordenCompraBL.findById(idOrdenGenerado);
            verificar("R (Read) - Obtener OrdenCompra por ID", true, ordenObtenida != null);

            List<OrdenCompra> listaOrdenes = ordenCompraBL.findAll();
            verificar("R (Read) - Listar todas las Ordenes de Compra", true, !listaOrdenes.isEmpty());

            // =========================================================
            // C. UPDATE (Actualizar Datos/Estado)
            // =========================================================
            documentoIngresoBL.cambiarEstadoValidacion(idDocGenerado, EstadoValidacionDocumento.VALIDADO);
            DocumentoIngreso docActualizado = documentoIngresoBL.findById(idDocGenerado);
            verificar("U (Update) - Cambio de estado de validacion a VALIDADO",
                    EstadoValidacionDocumento.VALIDADO, docActualizado.getEstadoValidacion());

            ordenObtenida.setArchivoRespaldoPath("/archivos/ordenes/OC_BL_100_MODIFICADO.pdf");
            ordenCompraBL.update(ordenObtenida);
            OrdenCompra ordenTrasUpdate = ordenCompraBL.findById(idOrdenGenerado);
            verificar("U (Update) - Modificacion de ruta de respaldo en OrdenCompra",
                    "/archivos/ordenes/OC_BL_100_MODIFICADO.pdf", ordenTrasUpdate.getArchivoRespaldoPath());

            // =========================================================
            // D. DELETE (Eliminar Entidades)
            // =========================================================
            documentoIngresoBL.delete(idDocGenerado);
            DocumentoIngreso docEliminado = documentoIngresoBL.findById(idDocGenerado);
            verificar("D (Delete) - Baja logica de DocumentoIngreso exitosa", false, docEliminado.isActivo());

            ordenCompraBL.delete(idOrdenGenerado);
            OrdenCompra ordenEliminada = ordenCompraBL.findById(idOrdenGenerado);
            verificar("D (Delete) - Baja logica de OrdenCompra exitosa", false, ordenEliminada.isActivo());

        } catch (BLException e) {
            registrarFallo("Excepcion inesperada durante la ejecucion del CRUD",
                    e.getMessage() + " -> CAUSA: " + (e.getCause() != null ? e.getCause().getMessage() : "ninguna"));        }

        // =========================================================
        // Regla de Negocio: Rechazo de inserción sin líneas
        // =========================================================
        try {
            OrdenCompra ordenInvalida = new OrdenCompra();
            ordenInvalida.setArchivoRespaldoPath("/archivos/ordenes/OC_INVALIDA.pdf");
            ordenInvalida.setFechaRecepcion(LocalDateTime.now());

            ordenCompraBL.insert(ordenInvalida);
            registrarFallo("Validacion Negocio - Rechaza OrdenCompra sin lineas", "No lanzo BLException");
        } catch (BLException e) {
            verificar("Validacion Negocio - Rechaza insercion de OrdenCompra sin lineas correctamente", true, true);
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
                ? " RESULTADO: El modulo de gestion documental cumple con el CRUD al 100%."
                : " RESULTADO: Hay fallos en el modulo de gestion documental.");
        System.out.println();
    }

    public static void main(String[] args) {
        ejecutar();
    }
}