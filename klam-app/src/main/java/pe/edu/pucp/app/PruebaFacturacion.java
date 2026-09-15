package pe.edu.pucp.app;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Boleta;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Cotizacion;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.DocumentoFacturacion;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.EstadoPago;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Factura;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaBoleta;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaDocumento;
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

        Factura factura = probarCotizacionYFactura(fresa, esfera);
        probarBoleta(fresa, duramadre);
        probarGuardaDeLineas(factura, fresa);
        probarNotaCredito(factura, fresa);
        probarAnulacion(factura);

        resumen();
    }

    // -----------------------------------------------------------------
    // 1. Cotizacion aceptada y factura a cliente institucional
    // -----------------------------------------------------------------
    private static Factura probarCotizacionYFactura(Consumible fresa, Consumible esfera) {
        titulo("1. Cotizacion y factura a clinica (RUC)");

        Cotizacion cotizacion = new Cotizacion(1, 4500.00, LocalDateTime.now());
        cotizacion.setIdCotizacion(1);
        System.out.println("   " + cotizacion);

        // el estado nunca puede quedar nulo, ni siquiera mientras esta EMITIDA
        try {
            cotizacion.setEstado(null);
            registrarFallo("Una cotizacion no debe aceptar estado nulo", "acepto el null");
        } catch (IllegalArgumentException e) {
            verificar("Una cotizacion rechaza estado nulo", true, true);
        }
        verificar("La cotizacion sigue EMITIDA tras el rechazo",
                "EMITIDA", cotizacion.getEstado().name());

        // precio pactado negativo, igual que el CHECK del script SQL
        try {
            new Cotizacion(1, -4500.00, LocalDateTime.now());
            registrarFallo("Una cotizacion no debe aceptar precio pactado negativo", "acepto la cotizacion");
        } catch (IllegalArgumentException e) {
            verificar("El constructor de Cotizacion rechaza precio pactado negativo", true, true);
        }
        try {
            cotizacion.setPrecioPactado(-1.00);
            registrarFallo("setPrecioPactado no debe aceptar precio negativo", "acepto el valor");
        } catch (IllegalArgumentException e) {
            verificar("setPrecioPactado rechaza precio negativo", true, true);
        }
        verificar("La cotizacion conserva su precio tras el rechazo",
                4500.00, cotizacion.getPrecioPactado());

        cotizacion.aceptar();
        verificar("La cotizacion queda ACEPTADA",
                "ACEPTADA", cotizacion.getEstado().name());
        try {
            cotizacion.rechazar();
            registrarFallo("Una cotizacion aceptada no debe poder rechazarse", "acepto el cambio");
        } catch (IllegalStateException e) {
            verificar("Una cotizacion ya resuelta no cambia de estado", true, true);
        }

        Factura factura = new Factura(1, LocalDateTime.now(), "20481234567");
        factura.setIdDocumento(1);
        factura.agregarLinea(new LineaFactura(2, 1200.00, "Fresa de corte 4mm", fresa, "CONS-001"));
        factura.agregarLinea(new LineaFactura(1, 2100.00, "Esfera de navegacion", esfera, "CONS-002"));

        factura.calcularMontoTotal();
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

        // ruc_receptor es CHAR(11) NOT NULL en el script SQL
        try {
            new Factura(1, LocalDateTime.now(), null);
            registrarFallo("Una factura no debe aceptar RUC nulo", "acepto la factura");
        } catch (IllegalArgumentException e) {
            verificar("El constructor de Factura rechaza RUC nulo", true, true);
        }
        try {
            new Factura(1, LocalDateTime.now(), "2048123456");
            registrarFallo("Una factura no debe aceptar RUC de 10 digitos", "acepto la factura");
        } catch (IllegalArgumentException e) {
            verificar("El constructor de Factura rechaza RUC de 10 digitos", true, true);
        }
        try {
            factura.setRucReceptor("2048123456A");
            registrarFallo("setRucReceptor no debe aceptar letras", "acepto el valor");
        } catch (IllegalArgumentException e) {
            verificar("setRucReceptor rechaza un RUC con letras", true, true);
        }
        verificar("La factura conserva su RUC tras el rechazo",
                "20481234567", factura.getRucReceptor());

        return factura;
    }

    // -----------------------------------------------------------------
    // 2. Boleta a paciente particular
    // -----------------------------------------------------------------
    private static void probarBoleta(Consumible fresa, Consumible duramadre) {
        titulo("2. Boleta a paciente particular (DNI)");

        Boleta boleta = new Boleta(2, LocalDateTime.now(), "70123456");
        boleta.setIdDocumento(2);
        boleta.agregarLinea(new LineaBoleta(1, 1200.00, "Fresa de corte 4mm", fresa, "CONS-001"));
        boleta.agregarLinea(new LineaBoleta(2, 1000.00, "Duramadre artificial 5x5", duramadre, "CONS-003"));

        boleta.calcularMontoTotal();
        boleta.setEstadoPago(EstadoPago.PAGADO);
        System.out.println("   " + boleta);

        verificar("El monto base es la suma de las lineas (1x1200 + 2x1000)",
                3200.00, boleta.getMontoBase());
        verificar("El total es base mas IGV",
                3776.00, boleta.getMontoTotal());
        verificar("Queda registrada como PAGADO",
                "PAGADO", boleta.getEstadoPago().name());

        try {
            boleta.setEstadoPago(EstadoPago.PENDIENTE);
            registrarFallo("Un documento PAGADO no debe volver a PENDIENTE", "acepto el cambio");
        } catch (IllegalStateException e) {
            verificar("Un documento PAGADO no vuelve a PENDIENTE", true, true);
        }

        try {
            boleta.setEstadoPago(null);
            registrarFallo("Un documento no debe aceptar estado de pago nulo", "acepto el null");
        } catch (IllegalArgumentException e) {
            verificar("Un documento rechaza estado de pago nulo", true, true);
        }
        verificar("La boleta sigue PAGADO tras el rechazo",
                "PAGADO", boleta.getEstadoPago().name());

        // dni_receptor es CHAR(8) NOT NULL en el script SQL
        try {
            new Boleta(2, LocalDateTime.now(), "7012345");
            registrarFallo("Una boleta no debe aceptar DNI de 7 digitos", "acepto la boleta");
        } catch (IllegalArgumentException e) {
            verificar("El constructor de Boleta rechaza DNI de 7 digitos", true, true);
        }
        try {
            boleta.setDniReceptor(null);
            registrarFallo("setDniReceptor no debe aceptar DNI nulo", "acepto el valor");
        } catch (IllegalArgumentException e) {
            verificar("setDniReceptor rechaza DNI nulo", true, true);
        }
        verificar("La boleta conserva su DNI tras el rechazo",
                "70123456", boleta.getDniReceptor());

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

        // setLineas no debe ser una puerta trasera para saltarse la validacion
        List<LineaDocumento> ajenas = new ArrayList<>();
        ajenas.add(new LineaBoleta(1, 500.00, "Linea ajena", fresa, "CONS-004"));
        try {
            factura.setLineas(ajenas);
            registrarFallo("setLineas tampoco debe aceptar lineas de otro tipo", "acepto la lista");
        } catch (IllegalArgumentException e) {
            verificar("setLineas rechaza lineas de otro tipo", true, true);
        }

        // cambiar la tasa debe recalcular sin intervencion manual
        factura.setTasaIgv(0.10);
        verificar("Cambiar la tasa de IGV recalcula el total",
                4950.00, factura.getMontoTotal());
        factura.setTasaIgv(0.18);

        // modificar una linea ya agregada no debe dejar los montos viejos
        LineaDocumento primera = factura.getLineas().get(0);
        primera.setCantidad(3);
        verificar("Cambiar la cantidad de una linea ya agregada actualiza la base (3x1200 + 1x2100)",
                5700.00, factura.getMontoBase());
        verificar("Cambiar la cantidad de una linea ya agregada actualiza el IGV",
                1026.00, factura.getIgv());
        verificar("Cambiar la cantidad de una linea ya agregada actualiza el total",
                6726.00, factura.getMontoTotal());
        primera.setCantidad(2);

        // valores que el CHECK del script SQL rechazaria
        try {
            new LineaFactura(0, 100.00, "Cantidad cero", fresa, "CONS-005");
            registrarFallo("Una linea no debe aceptar cantidad cero", "acepto la linea");
        } catch (IllegalArgumentException e) {
            verificar("El constructor rechaza cantidad cero", true, true);
        }
        try {
            new LineaFactura(1, -100.00, "Precio negativo", fresa, "CONS-005");
            registrarFallo("Una linea no debe aceptar precio negativo", "acepto la linea");
        } catch (IllegalArgumentException e) {
            verificar("El constructor rechaza precio unitario negativo", true, true);
        }
        try {
            primera.setCantidad(-3);
            registrarFallo("setCantidad no debe aceptar cantidad negativa", "acepto el valor");
        } catch (IllegalArgumentException e) {
            verificar("setCantidad rechaza cantidad negativa", true, true);
        }
        try {
            primera.setPrecioUnitario(-50.00);
            registrarFallo("setPrecioUnitario no debe aceptar precio negativo", "acepto el valor");
        } catch (IllegalArgumentException e) {
            verificar("setPrecioUnitario rechaza precio negativo", true, true);
        }
        verificar("La factura no cambia tras los valores rechazados",
                5310.00, factura.getMontoTotal());

        // una linea creada con el constructor vacio queda con cantidad 0
        try {
            factura.agregarLinea(new LineaFactura());
            registrarFallo("agregarLinea no debe aceptar una linea con cantidad 0", "acepto la linea");
        } catch (IllegalArgumentException e) {
            verificar("agregarLinea rechaza una linea creada vacia (cantidad 0)", true, true);
        }
        List<LineaDocumento> conVacia = new ArrayList<>();
        conVacia.add(new LineaFactura(1, 300.00, "Linea valida", fresa, "CONS-006"));
        conVacia.add(new LineaFactura());
        try {
            factura.setLineas(conVacia);
            registrarFallo("setLineas no debe aceptar una linea con cantidad 0", "acepto la lista");
        } catch (IllegalArgumentException e) {
            verificar("setLineas rechaza una lista con una linea creada vacia", true, true);
        }
        verificar("La factura conserva su detalle tras los rechazos",
                2, factura.getLineas().size());

        // completada con setters, la misma linea si es valida
        Factura otra = new Factura(3, LocalDateTime.now(), "20481234567");
        LineaFactura completada = new LineaFactura();
        completada.setCantidad(1);
        completada.setPrecioUnitario(300.00);
        completada.setDescripcion("Linea completada con setters");
        otra.agregarLinea(completada);
        verificar("Una linea vacia completada con setters si se acepta",
                300.00, otra.getMontoBase());

        // descripcion es NOT NULL en el script SQL
        try {
            new LineaFactura(1, 100.00, null, fresa, "CONS-005");
            registrarFallo("Una linea no debe aceptar descripcion nula", "acepto la linea");
        } catch (IllegalArgumentException e) {
            verificar("El constructor rechaza descripcion nula", true, true);
        }
        try {
            primera.setDescripcion("   ");
            registrarFallo("setDescripcion no debe aceptar descripcion vacia", "acepto el valor");
        } catch (IllegalArgumentException e) {
            verificar("setDescripcion rechaza descripcion vacia", true, true);
        }
        verificar("La linea conserva su descripcion tras el rechazo",
                "Fresa de corte 4mm", primera.getDescripcion());

        // tasa de IGV negativa
        try {
            factura.setTasaIgv(-0.18);
            registrarFallo("setTasaIgv no debe aceptar tasa negativa", "acepto el valor");
        } catch (IllegalArgumentException e) {
            verificar("setTasaIgv rechaza tasa negativa", true, true);
        }
        verificar("La factura conserva su tasa y su total tras el rechazo",
                5310.00, factura.getMontoTotal());
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

        nota.calcularMontoTotal();
        System.out.println("   " + nota);

        verificar("La nota apunta al documento que corrige",
                factura.getIdDocumento(), nota.getDocumentoOriginal().getIdDocumento());
        verificar("El monto de la nota es la suma de sus lineas",
                1200.00, nota.getMontoTotal());
        verificar("La nota no excede el total del documento original",
                false, nota.excedeAlDocumentoOriginal());

        // setLineas no debe ser una puerta trasera para meter lineas nulas
        List<LineaNotaCredito> conNula = new ArrayList<>();
        conNula.add(null);
        try {
            nota.setLineas(conNula);
            registrarFallo("setLineas de la nota no debe aceptar lineas nulas", "acepto la lista");
        } catch (IllegalArgumentException e) {
            verificar("setLineas de la nota rechaza lineas nulas", true, true);
        }
        verificar("La nota conserva su detalle tras el rechazo",
                1, nota.getLineas().size());

        // reemplazar el detalle debe recalcular sin intervencion manual
        List<LineaNotaCredito> nuevas = new ArrayList<>();
        nuevas.add(new LineaNotaCredito(2, 1200.00, "Fresa de corte 4mm",
                fresa, "CONS-001", "No utilizadas durante el procedimiento"));
        nota.setLineas(nuevas);
        verificar("setLineas de la nota recalcula el total (2x1200)",
                2400.00, nota.getMontoTotal());

        // modificar una linea ya agregada no debe dejar el total viejo
        nota.getLineas().get(0).setPrecioUnitario(1000.00);
        verificar("Cambiar el precio de una linea ya agregada actualiza el total de la nota (2x1000)",
                2000.00, nota.getMontoTotal());

        // una linea creada con el constructor vacio queda con cantidad 0
        try {
            nota.agregarLinea(new LineaNotaCredito());
            registrarFallo("agregarLinea de la nota no debe aceptar cantidad 0", "acepto la linea");
        } catch (IllegalArgumentException e) {
            verificar("agregarLinea de la nota rechaza una linea creada vacia", true, true);
        }
        List<LineaNotaCredito> conVacia = new ArrayList<>();
        conVacia.add(new LineaNotaCredito());
        try {
            nota.setLineas(conVacia);
            registrarFallo("setLineas de la nota no debe aceptar cantidad 0", "acepto la lista");
        } catch (IllegalArgumentException e) {
            verificar("setLineas de la nota rechaza una linea creada vacia", true, true);
        }
        verificar("La nota conserva su total tras los rechazos",
                2000.00, nota.getMontoTotal());
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
        titulo("RESUMEN");
        System.out.println(" Verificaciones ejecutadas: " + verificaciones);
        System.out.println(" Fallos: " + fallos);
        System.out.println(fallos == 0
                ? " RESULTADO: el dominio de facturacion soporta el modelo de negocio."
                : " RESULTADO: hay verificaciones fallidas, revisar arriba.");
        System.out.println();
    }
}