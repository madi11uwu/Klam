package pe.edu.pucp.app;

import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.OrdenCompra;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.TipoDocumentoIngreso;

import java.time.LocalDateTime;

public class PruebaGestionDocumental {
    public static void main(String[] args) {
        System.out.println("=== PRUEBA DE CLASES DE DOMINIO - ROL 5 ===\n");

        // 1. Instanciación de OrdenCompra
        OrdenCompra orden = new OrdenCompra();
        orden.setIdOrdenCompra("OC-2026-001");
        orden.setArchivoRespaldoPath("/archivos/ordenes/OC001.pdf");
        orden.setFechaRecepcion(LocalDateTime.now());

        System.out.println("Orden de Compra creada: " + orden.getIdOrdenCompra());
        System.out.println("¿Es verificable?: " + orden.verificar());

        // 2. Instanciación de DocumentoIngreso asociando la OrdenCompra
        DocumentoIngreso doc = new DocumentoIngreso();
        doc.setId_documento("DOC-1001");
        doc.setTipoDocumento(TipoDocumentoIngreso.ORDEN_COMPRA);
        doc.setArchivoPath("/archivos/docs/DOC1001.pdf");
        doc.setEstadoValidacion("VALIDADO");
        doc.setFechaCarga(LocalDateTime.now());
        doc.setOrdenCompra(orden); // Asignación con copia defensiva

        System.out.println("\nDocumento de Ingreso creado: " + doc.getId_documento());
        System.out.println("Tipo: " + doc.getTipoDocumento());
        System.out.println("¿Es válido?: " + doc.validar());
        System.out.println("Orden asociada: " + doc.getOrdenCompra().getIdOrdenCompra());

        // 3. Prueba de Copia Defensiva (Modificar la orden original no debe alterar la copia interna)
        orden.setIdOrdenCompra("OC-MODIFICADA-999");
        System.out.println("\n--- Prueba de Copia Defensiva ---");
        System.out.println("ID Orden original modificada: " + orden.getIdOrdenCompra());
        System.out.println("ID Orden dentro del Documento (debe ser OC-2026-001): "
                + doc.getOrdenCompra().getIdOrdenCompra());

        // 4. Prueba del Constructor de Copia
        DocumentoIngreso docCopia = new DocumentoIngreso(doc);
        System.out.println("\nCopia de Documento creada exitosamente: " + docCopia.getId_documento());

        // 5. Prueba de Manejo de Excepciones (Validación de Nulos)
        System.out.println("\n--- Prueba de Validaciones (Excepciones esperadas) ---");
        try {
            DocumentoIngreso docInvalido = new DocumentoIngreso();
            docInvalido.setId_documento(""); // Debe lanzar excepción
        } catch (IllegalArgumentException e) {
            System.out.println("Excepción capturada correctamente: " + e.getMessage());
        }

        System.out.println("\n¡Todas las pruebas finalizaron con éxito!");
    }
}
