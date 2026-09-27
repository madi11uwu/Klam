package pe.edu.pucp.app;

import java.time.LocalDateTime;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Boleta;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Cotizacion;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.DocumentoFacturacion;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.EstadoPago;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Factura;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaBoleta;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaFactura;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaNotaCredito;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.NotaCredito;

/**
 * Prueba del modulo de Documentacion y Finanzas (Rol 6).
 *
 * Verifica que la capa de dominio da soporte al modelo de negocio de BIOKLAM:
 * se cotiza una cirugia, se factura lo consumido a una clinica, se emite una
 * boleta a un paciente particular y se corrige una factura con una nota de
 * credito por consumibles devueltos.
 *
 * Puede ejecutarse por si sola o invocarse desde Programa.main().
 */
public class PruebaFacturacion {

    private static int verificaciones = 0;
    private static int fallos = 0;

    public static void main(String[] args) {
        ejecutar();
    }

    public static void ejecutar() {
        titulo("MODULO DE DOCUMENTACION Y FINANZAS - ROL 6");

        Consumible fresa = crearConsumible(1, "Fresa de corte 4mm", "Medtronic", "4mm");
        Consumible esfera = crearConsumible(2, "Esfera de navegacion", "Brainlab", "Estandar");
        Consumible duramadre = crearConsumible(3, "Duramadre artificial 5x5", "Integra", "5x5 cm");

        Cirugia craneotomia = crearCirugia(1, "Craneotomia con navegacion");
        Cirugia biopsia = crearCirugia(2, "Biopsia estereotaxica");

        Factura factura = probarCotizacionYFactura(craneotomia, fresa, esfera);
        probarBoleta(biopsia, fresa, duramadre);
        probarGuardaDeLineas(factura, fresa);
        probarNotaCredito(factura, fresa);
        probarAnulacion(factura);

        resumen();
    }

    // -----------------------------------------------------------------
    // 1. Cotizacion aceptada y factura a cliente institucional
    // -----------------------------------------------------------------
    private static Factura probarCotizacionYFactura(Cirugia cirugia, Consumible fresa, Consumible esfera) {
        titulo("1. Cotizacion y factura a clinica (RUC)");

        Cotizacion cotizacion = new Cotizacion(cirugia, 4500.00, LocalDateTime.now());
        cotizacion.setIdCotizacion(1);
        System.out.println("   " + cotizacion);
        cotizacion.aceptar();
        verificar("La cotizacion queda ACEPTADA",
                "ACEPTADA", cotizacion.getEstado().name());
        try {
            cotizacion.rechazar();
            registrarFallo("Una cotizacion aceptada no debe poder rechazarse", "acepto el cambio");
        } catch (IllegalStateException e) {
            verificar("Una cotizacion ya resuelta no cambia de estado", true, true);
        }

        Factura factura = new Factura(cirugia, LocalDateTime.now(), "20481234567");
        factura.setIdDocumento(1);
        factura.agregarLinea(new LineaFactura(2, 1200.00, "Fresa de corte 4mm", fresa, "CONS-001"));
        factura.agregarLinea(new LineaFactura(1, 2100.00, "Esfera de navegacion", esfera, "CONS-002"));
        System.out.println("   " + factura);

        verificar("La factura tiene 2 lineas de detalle",
                2, factura.getLineas().size());
        verificar("El monto base es la suma de las lineas (2x1200 + 1x2100)",
                4500.00, factura.getMontoBase());
        verificar("El IGV es el 18% del monto base",
                810.00, factura.getIgv());
        verificar("El total es base mas IGV",
                5310.00, factura.getMontoTotal());
        verificar("Nace con estado de pago PENDIENTE",
                "PENDIENTE", factura.getEstadoPago().name());
        verificar("La linea conserva la referencia al consumible",
                "Fresa de corte 4mm", factura.getLineas().get(0).getConsumible().getNombreComercial());

        // la relacion se modela con el objeto: desde el documento se llega a la cirugia
        verificar("La cotizacion apunta a la cirugia que se va a operar",
                "Craneotomia con navegacion", cotizacion.getCirugia().getTipoProcedimiento());
        verificar("Desde la factura se llega al tipo de procedimiento de su cirugia",
                "Craneotomia con navegacion", factura.getCirugia().getTipoProcedimiento());
        verificar("La factura y la cotizacion apuntan a la misma cirugia",
                true, factura.getCirugia() == cotizacion.getCirugia());

        return factura;
    }

    // -----------------------------------------------------------------
    // 2. Boleta a paciente particular
    // -----------------------------------------------------------------
    private static void probarBoleta(Cirugia cirugia, Consumible fresa, Consumible duramadre) {
        titulo("2. Boleta a paciente particular (DNI)");

        Boleta boleta = new Boleta(cirugia, LocalDateTime.now(), "70123456");
        boleta.setIdDocumento(2);
        boleta.agregarLinea(new LineaBoleta(1, 1200.00, "Fresa de corte 4mm", fresa, "CONS-001"));
        boleta.agregarLinea(new LineaBoleta(2, 1000.00, "Duramadre artificial 5x5", duramadre, "CONS-003"));
        boleta.setEstadoPago(EstadoPago.PAGADO);
        System.out.println("   " + boleta);

        verificar("El monto base es la suma de las lineas (1x1200 + 2x1000)",
                3200.00, boleta.getMontoBase());
        verificar("El IGV es el 18% del monto base",
                576.00, boleta.getIgv());
        verificar("El total es base mas IGV",
                3776.00, boleta.getMontoTotal());
        verificar("Queda registrada como PAGADO",
                "PAGADO", boleta.getEstadoPago().name());
        verificar("La boleta corresponde a otra cirugia",
                2, boleta.getCirugia().getId_cirugia());

        try {
            boleta.setEstadoPago(EstadoPago.PENDIENTE);
            registrarFallo("Un documento PAGADO no debe volver a PENDIENTE", "acepto el cambio");
        } catch (IllegalStateException e) {
            verificar("Un documento PAGADO no vuelve a PENDIENTE", true, true);
        }

        // Factura y boleta comparten el mismo calculo heredado del padre.
        DocumentoFacturacion comoDocumento = boleta;
        verificar("Una Boleta es tratable como DocumentoFacturacion",
                3776.00, comoDocumento.calcularMontoTotal());
    }

    // -----------------------------------------------------------------
    // 3. Coherencia entre cabecera y detalle
    // -----------------------------------------------------------------
    private static void probarGuardaDeLineas(Factura factura, Consumible fresa) {
        titulo("3. Coherencia entre cabecera y detalle");

        int lineasAntes = factura.getLineas().size();
        try {
            factura.agregarLinea(new LineaBoleta(1, 500.00, "Linea ajena", fresa, "CONS-004"));
            registrarFallo("Una Factura NO debe aceptar una LineaBoleta", "acepto la linea");
        } catch (IllegalArgumentException e) {
            verificar("Una Factura rechaza una LineaBoleta",
                    true, true);
            System.out.println("   Mensaje: " + e.getMessage());
        }
        verificar("La factura no se contamino con la linea rechazada",
                lineasAntes, factura.getLineas().size());

        // los montos se recalculan aunque cambie una linea ya agregada
        factura.getLineas().get(0).setCantidad(3);
        verificar("Cambiar una linea ya agregada actualiza el total ((3x1200 + 1x2100) x 1.18)",
                6726.00, factura.getMontoTotal());
        factura.getLineas().get(0).setCantidad(2);

        // validacion de datos representativa: mismas reglas que los CHECK del SQL
        try {
            new LineaFactura(0, 1200.00, "Cantidad cero", fresa, "CONS-005");
            registrarFallo("Una linea no debe aceptar cantidad cero", "acepto la linea");
        } catch (IllegalArgumentException e) {
            verificar("Una linea con cantidad cero es rechazada", true, true);
            System.out.println("   Mensaje: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------
    // 4. Nota de credito sobre la factura
    // -----------------------------------------------------------------
    private static void probarNotaCredito(Factura factura, Consumible fresa) {
        titulo("4. Nota de credito por consumible devuelto");

        NotaCredito nota = new NotaCredito(factura,
                "Consumible devuelto sin usar tras la cirugia", LocalDateTime.now());
        nota.setIdNotaCredito(1);
        nota.agregarLinea(new LineaNotaCredito(1, 1200.00, "Fresa de corte 4mm",
                fresa, "CONS-001", "No utilizada durante el procedimiento"));
        System.out.println("   " + nota);

        verificar("La nota apunta al documento que corrige",
                factura.getIdDocumento(), nota.getDocumentoOriginal().getIdDocumento());
        verificar("El monto de la nota es la suma de sus lineas",
                1200.00, nota.getMontoTotal());
        verificar("La nota no excede el monto base del documento original",
                false, nota.excedeAlDocumentoOriginal());
    }

    // -----------------------------------------------------------------
    // 5. Anulacion y eliminacion logica
    // -----------------------------------------------------------------
    private static void probarAnulacion(Factura factura) {
        titulo("5. Anulacion y eliminacion logica");

        factura.anular();
        verificar("La factura queda ANULADO tras anularse",
                "ANULADO", factura.getEstadoPago().name());

        try {
            factura.setEstadoPago(EstadoPago.PAGADO);
            registrarFallo("Un documento ANULADO no debe volver a PAGADO", "acepto el cambio");
        } catch (IllegalStateException e) {
            verificar("Un documento ANULADO no puede volver a PAGADO", true, true);
            System.out.println("   Mensaje: " + e.getMessage());
        }

        factura.setActivo(false);
        verificar("La factura se desactiva sin perderse (eliminacion logica)",
                false, factura.isActivo());
        verificar("Sus lineas siguen disponibles para consulta historica",
                2, factura.getLineas().size());
    }

    // -----------------------------------------------------------------
    // Utilidades
    // -----------------------------------------------------------------
    private static Cirugia crearCirugia(int id, String tipoProcedimiento) {
        Cirugia c = new Cirugia();
        c.setId_cirugia(id);
        c.setTipoProcedimiento(tipoProcedimiento);
        c.setFechaHoraInicio(LocalDateTime.now());
        return c;
    }

    private static Consumible crearConsumible(int id, String nombre, String marca, String medida) {
        Consumible c = new Consumible();
        c.setId_consumible(id);
        c.setNombreComercial(nombre);
        c.setMarca(marca);
        c.setMedida(medida);
        return c;
    }

    private static void verificar(String descripcion, Object esperado, Object obtenido) {
        verificaciones++;
        boolean ok;
        if (esperado instanceof Double && obtenido instanceof Double) {
            ok = Math.abs((Double) esperado - (Double) obtenido) < 0.001;
        } else {
            ok = esperado.equals(obtenido);
        }
        if (ok) {
            System.out.println("   [OK]     " + descripcion);
        } else {
            fallos++;
            System.out.println("   [FALLO]  " + descripcion
                    + "  (esperado: " + esperado + ", obtenido: " + obtenido + ")");
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
        titulo("RESUMEN ROL 6");
        System.out.println(" Verificaciones ejecutadas: " + verificaciones);
        System.out.println(" Fallos: " + fallos);
        System.out.println(fallos == 0
                ? " RESULTADO: el dominio de facturacion soporta el modelo de negocio."
                : " RESULTADO: hay verificaciones fallidas, revisar arriba.");
        System.out.println();
    }
}
