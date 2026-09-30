package pe.edu.pucp.app.prueba2;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.BoletaBL;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.ConsumibleBL;
import pe.edu.pucp.klam.bl.FacturaBL;
import pe.edu.pucp.klam.bl.NotaCreditoBL;

import pe.edu.pucp.klam.bl.impl.BoletaBLImpl;
import pe.edu.pucp.klam.bl.impl.CirugiaBLImpl;
import pe.edu.pucp.klam.bl.impl.ConsumibleBLImpl;
import pe.edu.pucp.klam.bl.impl.FacturaBLImpl;
import pe.edu.pucp.klam.bl.impl.NotaCreditoBLImpl;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Boleta;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.EstadoPago;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Factura;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaBoleta;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaFactura;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaNotaCredito;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.NotaCredito;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Prueba CRUD de Factura, Boleta y NotaCredito usando únicamente la capa BL.
 *
 * La cirugía y el consumible NO se crean ni modifican aquí:
 * se toma un registro existente de cada uno para satisfacer las relaciones.
 */
public class PruebaFacturacionCRUD {

    private static final FacturaBL facturaBL = new FacturaBLImpl();
    private static final BoletaBL boletaBL = new BoletaBLImpl();
    private static final NotaCreditoBL notaCreditoBL = new NotaCreditoBLImpl();

    // Solo se usan para obtener dependencias existentes.
    private static final CirugiaBL cirugiaBL = new CirugiaBLImpl();
    private static final ConsumibleBL consumibleBL = new ConsumibleBLImpl();

    private static int idFacturaCreada = 0;
    private static int idBoletaCreada = 0;
    private static int idNotaFacturaCreada = 0;
    private static int idNotaBoletaCreada = 0;

    public static void ejecutar() {
        System.out.println("==================================================");
        System.out.println("     CRUD FACTURACIÓN (BL TESTS)");
        System.out.println("     Factura - Boleta - Nota de Crédito");
        System.out.println("==================================================");

        Cirugia cirugia = obtenerCirugiaExistente();
        Consumible consumible = obtenerConsumibleExistente();

        if (cirugia == null) {
            System.out.println("[ERROR] No hay ninguna cirugía disponible en la BD.");
            System.out.println("        Inserta al menos una cirugía antes de ejecutar esta prueba.");
            return;
        }

        if (consumible == null) {
            System.out.println("[ERROR] No hay ningún consumible disponible en la BD.");
            System.out.println("        Inserta al menos un consumible antes de ejecutar esta prueba.");
            return;
        }

        System.out.println("[PRE-STEP] Cirugía usada: ID " + cirugia.getId_cirugia());
        System.out.println("[PRE-STEP] Consumible usado: ID " + consumible.getId_consumible()
                + " - " + consumible.getNombreComercial());

        try {
            Factura factura = probarFactura(cirugia, consumible);
            Boleta boleta = probarBoleta(cirugia, consumible);

            probarNotaCreditoFactura(factura, consumible);
            probarNotaCreditoBoleta(boleta, consumible);

            probarValidaciones(cirugia);

        } catch (BLException e) {
            mostrarError(e);
        } catch (RuntimeException e) {
            System.out.println("[ERROR INESPERADO] " + e.getClass().getSimpleName()
                    + ": " + e.getMessage());
        } finally {
            limpiarDatosDePrueba();
        }
    }

    // -----------------------------------------------------------------
    // FACTURA
    // -----------------------------------------------------------------
    private static Factura probarFactura(Cirugia cirugia, Consumible consumible) {
        System.out.println("\n--- FACTURA ---");

        Factura factura = new Factura(
                cirugia,
                LocalDateTime.now(),
                "20123456789"
        );

        LineaFactura linea = new LineaFactura();
        linea.setCantidad(2);
        linea.setPrecioUnitario(150.00);
        linea.setDescripcion("Consumible de prueba para factura");
        linea.setConsumible(consumible);

        factura.agregarLinea(linea);

        facturaBL.insert(factura);
        idFacturaCreada = factura.getIdDocumento();

        System.out.println("[INSERT] id " + idFacturaCreada);
        System.out.println("         " + describir(factura));

        Factura leida = facturaBL.findById(idFacturaCreada);
        System.out.println("[FIND BY ID] " + describir(leida));

        // Transición válida: PENDIENTE -> PAGADO
        leida.setEstadoPago(EstadoPago.PAGADO);
        facturaBL.update(leida);

        Factura actualizada = facturaBL.findById(idFacturaCreada);
        System.out.println("[UPDATE] " + describir(actualizada));

        List<Factura> todas = facturaBL.findAll();
        System.out.println("[FIND ALL] total: " + todas.size());

        return actualizada;
    }

    // -----------------------------------------------------------------
    // BOLETA
    // -----------------------------------------------------------------
    private static Boleta probarBoleta(Cirugia cirugia, Consumible consumible) {
        System.out.println("\n--- BOLETA ---");

        Boleta boleta = new Boleta(
                cirugia,
                LocalDateTime.now(),
                "12345678"
        );

        LineaBoleta linea = new LineaBoleta();
        linea.setCantidad(3);
        linea.setPrecioUnitario(80.00);
        linea.setDescripcion("Consumible de prueba para boleta");
        linea.setConsumible(consumible);

        boleta.agregarLinea(linea);

        boletaBL.insert(boleta);
        idBoletaCreada = boleta.getIdDocumento();

        System.out.println("[INSERT] id " + idBoletaCreada);
        System.out.println("         " + describir(boleta));

        Boleta leida = boletaBL.findById(idBoletaCreada);
        System.out.println("[FIND BY ID] " + describir(leida));

        // Modificamos un dato permitido.
        leida.setDniReceptor("87654321");
        boletaBL.update(leida);

        Boleta actualizada = boletaBL.findById(idBoletaCreada);
        System.out.println("[UPDATE] " + describir(actualizada));

        List<Boleta> todas = boletaBL.findAll();
        System.out.println("[FIND ALL] total: " + todas.size());

        return actualizada;
    }

    // -----------------------------------------------------------------
    // NOTA DE CRÉDITO SOBRE FACTURA
    // -----------------------------------------------------------------
    private static void probarNotaCreditoFactura(Factura factura, Consumible consumible) {
        System.out.println("\n--- NOTA DE CRÉDITO / FACTURA ---");

        NotaCredito nota = new NotaCredito(
                factura,
                "Corrección parcial de factura de prueba",
                LocalDateTime.now()
        );

        LineaNotaCredito linea = new LineaNotaCredito();
        linea.setCantidad(1);
        linea.setPrecioUnitario(50.00);
        linea.setDescripcion("Devolución parcial de factura");
        linea.setConsumible(consumible);

        nota.agregarLinea(linea);

        notaCreditoBL.insert(nota);
        idNotaFacturaCreada = nota.getIdNotaCredito();

        System.out.println("[INSERT] id " + idNotaFacturaCreada);
        System.out.println("         " + describir(nota));

        NotaCredito leida = notaCreditoBL.findById(idNotaFacturaCreada);
        System.out.println("[FIND BY ID] " + describir(leida));

        leida.setMotivo("Corrección parcial de factura - motivo actualizado");
        notaCreditoBL.update(leida);

        NotaCredito actualizada = notaCreditoBL.findById(idNotaFacturaCreada);
        System.out.println("[UPDATE] " + describir(actualizada));

        System.out.println("[FIND BY FACTURA] total: "
                + notaCreditoBL.findByFacturaId(factura.getIdDocumento()).size());
    }

    // -----------------------------------------------------------------
    // NOTA DE CRÉDITO SOBRE BOLETA
    // -----------------------------------------------------------------
    private static void probarNotaCreditoBoleta(Boleta boleta, Consumible consumible) {
        System.out.println("\n--- NOTA DE CRÉDITO / BOLETA ---");

        NotaCredito nota = new NotaCredito(
                boleta,
                "Corrección parcial de boleta de prueba",
                LocalDateTime.now()
        );

        LineaNotaCredito linea = new LineaNotaCredito();
        linea.setCantidad(1);
        linea.setPrecioUnitario(30.00);
        linea.setDescripcion("Devolución parcial de boleta");
        linea.setConsumible(consumible);

        nota.agregarLinea(linea);

        notaCreditoBL.insert(nota);
        idNotaBoletaCreada = nota.getIdNotaCredito();

        System.out.println("[INSERT] id " + idNotaBoletaCreada);
        System.out.println("         " + describir(nota));

        NotaCredito leida = notaCreditoBL.findById(idNotaBoletaCreada);
        System.out.println("[FIND BY ID] " + describir(leida));

        leida.setMotivo("Corrección parcial de boleta - motivo actualizado");
        notaCreditoBL.update(leida);

        NotaCredito actualizada = notaCreditoBL.findById(idNotaBoletaCreada);
        System.out.println("[UPDATE] " + describir(actualizada));

        System.out.println("[FIND BY BOLETA] total: "
                + notaCreditoBL.findByBoletaId(boleta.getIdDocumento()).size());

        System.out.println("[FIND ALL NOTAS] total: " + notaCreditoBL.findAll().size());
    }

    // -----------------------------------------------------------------
    // VALIDACIONES BL
    // -----------------------------------------------------------------
    private static void probarValidaciones(Cirugia cirugia) {
        System.out.println("\n--- VALIDACIONES ESPERADAS ---");

        esperarError("factura sin líneas", () -> {
            Factura invalida = new Factura(
                    cirugia,
                    LocalDateTime.now(),
                    "20987654321"
            );
            facturaBL.insert(invalida);
        });

        esperarError("boleta sin líneas", () -> {
            Boleta invalida = new Boleta(
                    cirugia,
                    LocalDateTime.now(),
                    "11223344"
            );
            boletaBL.insert(invalida);
        });

        esperarError("nota de crédito sin líneas", () -> {
            Factura original = facturaBL.findById(idFacturaCreada);

            NotaCredito invalida = new NotaCredito(
                    original,
                    "Nota sin detalle",
                    LocalDateTime.now()
            );

            notaCreditoBL.insert(invalida);
        });

        esperarError("buscar factura con id inválido", () ->
                facturaBL.findById(0)
        );

        esperarError("buscar boleta con id inválido", () ->
                boletaBL.findById(-1)
        );
    }

    // -----------------------------------------------------------------
    // DEPENDENCIAS
    // -----------------------------------------------------------------
    private static Cirugia obtenerCirugiaExistente() {
        List<Cirugia> cirugias = cirugiaBL.findAll();

        for (Cirugia cirugia : cirugias) {
            if (cirugia != null && cirugia.getId_cirugia() > 0 && cirugia.isActivo()) {
                return cirugia;
            }
        }

        return null;
    }

    private static Consumible obtenerConsumibleExistente() {
        List<Consumible> consumibles = consumibleBL.findAll();

        for (Consumible consumible : consumibles) {
            if (consumible != null
                    && consumible.getId_consumible() > 0
                    && consumible.isActivo()) {
                return consumible;
            }
        }

        return null;
    }

    // -----------------------------------------------------------------
    // LIMPIEZA
    // -----------------------------------------------------------------
    private static void limpiarDatosDePrueba() {
        System.out.println("\n--- LIMPIEZA ---");

        // Las notas deben eliminarse ANTES que sus documentos originales.
        eliminarNotaSiExiste(idNotaFacturaCreada);
        eliminarNotaSiExiste(idNotaBoletaCreada);

        eliminarFacturaSiExiste(idFacturaCreada);
        eliminarBoletaSiExiste(idBoletaCreada);
    }

    private static void eliminarNotaSiExiste(int id) {
        if (id <= 0) {
            return;
        }

        try {
            NotaCredito nota = notaCreditoBL.findById(id);
            if (nota != null) {
                notaCreditoBL.delete(id);
                System.out.println("[DELETE] Nota de crédito " + id + " eliminada.");
            }
        } catch (RuntimeException e) {
            System.out.println("[CLEANUP] No se pudo eliminar nota " + id
                    + ": " + e.getMessage());
        }
    }

    private static void eliminarFacturaSiExiste(int id) {
        if (id <= 0) {
            return;
        }

        try {
            Factura factura = facturaBL.findById(id);
            if (factura != null && factura.isActivo()) {
                facturaBL.delete(id);
                System.out.println("[DELETE] Factura " + id + " dada de baja.");
            }
        } catch (RuntimeException e) {
            System.out.println("[CLEANUP] No se pudo eliminar factura " + id
                    + ": " + e.getMessage());
        }
    }

    private static void eliminarBoletaSiExiste(int id) {
        if (id <= 0) {
            return;
        }

        try {
            Boleta boleta = boletaBL.findById(id);
            if (boleta != null && boleta.isActivo()) {
                boletaBL.delete(id);
                System.out.println("[DELETE] Boleta " + id + " dada de baja.");
            }
        } catch (RuntimeException e) {
            System.out.println("[CLEANUP] No se pudo eliminar boleta " + id
                    + ": " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------
    // UTILITARIOS
    // -----------------------------------------------------------------
    private static void esperarError(String caso, Runnable accion) {
        try {
            accion.run();
            System.out.println("[VALIDACIÓN] FALLO: se permitió " + caso);
        } catch (BLException e) {
            System.out.println("[VALIDACIÓN] OK (" + caso + "): " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[VALIDACIÓN MODELO] OK (" + caso + "): " + e.getMessage());
        }
    }

    private static void mostrarError(BLException e) {
        System.out.println("[ERROR] " + e.getMessage());

        if (e.getCause() != null) {
            System.out.println("        Causa: " + e.getCause().getMessage());
        }
    }

    private static String describir(Factura factura) {
        if (factura == null) {
            return "Nulo";
        }

        return "id=" + factura.getIdDocumento()
                + ", RUC=" + factura.getRucReceptor()
                + ", base=S/ " + String.format("%.2f", factura.getMontoBase())
                + ", IGV=S/ " + String.format("%.2f", factura.getIgv())
                + ", total=S/ " + String.format("%.2f", factura.getMontoTotal())
                + ", estado=" + factura.getEstadoPago();
    }

    private static String describir(Boleta boleta) {
        if (boleta == null) {
            return "Nulo";
        }

        return "id=" + boleta.getIdDocumento()
                + ", DNI=" + boleta.getDniReceptor()
                + ", base=S/ " + String.format("%.2f", boleta.getMontoBase())
                + ", IGV=S/ " + String.format("%.2f", boleta.getIgv())
                + ", total=S/ " + String.format("%.2f", boleta.getMontoTotal())
                + ", estado=" + boleta.getEstadoPago();
    }

    private static String describir(NotaCredito nota) {
        if (nota == null) {
            return "Nulo";
        }

        String documento = "sin documento";
        if (nota.getDocumentoOriginal() != null) {
            documento = nota.getDocumentoOriginal().getClass().getSimpleName()
                    + " (ID: " + nota.getDocumentoOriginal().getIdDocumento() + ")";
        }

        return "id=" + nota.getIdNotaCredito()
                + ", motivo=" + nota.getMotivo()
                + ", monto=S/ " + String.format("%.2f", nota.getMontoTotal())
                + ", documento=" + documento;
    }

    public static void main(String[] args) {
        ejecutar();
    }
}
