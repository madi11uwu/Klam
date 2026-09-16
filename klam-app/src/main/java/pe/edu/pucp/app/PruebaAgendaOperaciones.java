package pe.edu.pucp.app;

import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.CategoriaEquipo;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;

import java.util.HashMap;
import java.util.Map;

/**
 * Prueba del modulo Agenda de Operaciones (Rol 2).
 *
 * Verifica la correcta instanciacion, copia defensiva y comportamiento
 * de la interfaz Verificable para Consumibles, Bandejas Instrumentales y Equipos.
 */
public class PruebaAgendaOperaciones {

    private static int verificaciones = 0;
    private static int fallos = 0;

    public static void ejecutar() {
        verificaciones = 0;
        fallos = 0;

        titulo("MODULO DE AGENDA DE OPERACIONES - ROL 2");

        probarConsumible();
        probarBandejaInstrumental();
        probarEquipo();
        probarConstructorCopiaConsumible();
        probarConstructorCopiaBandeja();
        probarConstructorCopiaEquipo();

        resumen();
    }

    // -----------------------------------------------------------------
    // 1. Pruebas de Consumible
    // -----------------------------------------------------------------
    private static void probarConsumible() {
        titulo("1. Pruebas de Consumible");

        Consumible consumible = new Consumible(1, "Fresa cortante", "Medtronic", "70");

        System.out.println("   Nombre: " + consumible.getNombreComercial());
        System.out.println("   Marca: " + consumible.getMarca());
        System.out.println("   Medida: " + consumible.getMedida());

        verificar("Nombre del consumible correcto", "Fresa cortante", consumible.getNombreComercial());
        verificar("Medida del consumible correcta", "70", consumible.getMedida());
    }

    // -----------------------------------------------------------------
    // 2. Pruebas de Bandeja Instrumental
    // -----------------------------------------------------------------
    private static void probarBandejaInstrumental() {
        titulo("2. Pruebas de Bandeja Instrumental");

        Consumible fresaCortante = new Consumible(1, "Fresa cortante", "Medtronic", "70");
        Consumible fresaDiamante = new Consumible(2, "Fresa diamante", "Medtronic", "70");
        Consumible cuchilla = new Consumible(3, "Cuchilla quirurgica", "Medtronic", "70");

        BandejaInstrumental bandeja = new BandejaInstrumental();
        bandeja.setId_bandeja(1);
        bandeja.setTipo("CABEZA");
        bandeja.setEsterilizado(true);

        bandeja.agregarConsumible(fresaCortante, 2);
        bandeja.agregarConsumible(fresaDiamante, 1);
        bandeja.agregarConsumible(cuchilla, 3);

        System.out.println("   Tipo de bandeja: " + bandeja.getTipo());
        System.out.println("   Cantidad de tipos de consumibles: " + bandeja.getConsumibles().size());

        for (Map.Entry<Consumible, Integer> entrada : bandeja.getConsumibles().entrySet()) {
            System.out.println("     - " + entrada.getKey().getNombreComercial()
                    + " | Medida: " + entrada.getKey().getMedida()
                    + " | Cantidad: " + entrada.getValue());
        }

        verificar("La bandeja contiene 3 tipos de consumibles", 3, bandeja.getConsumibles().size());
        verificar("La fresa cortante tiene cantidad 2", 2, bandeja.obtenerCantidadConsumible(fresaCortante));

        // Prueba de interfaz Verificable
        verificar("La bandeja esterilizada cumple la verificacion", true, bandeja.verificar());

        bandeja.setEsterilizado(false);
        verificar("Una bandeja no esterilizada falla la verificacion", false, bandeja.verificar());
    }

    // -----------------------------------------------------------------
    // 3. Pruebas de Equipo
    // -----------------------------------------------------------------
    private static void probarEquipo() {
        titulo("3. Pruebas de Equipo");

        Equipo equipo = new Equipo();
        equipo.setId_equipo(1);
        equipo.setNombre("Craneotomo 01");
        equipo.setCategoria(CategoriaEquipo.CRANEOTOMO);
        equipo.setDisponible(true);

        Map<String, Object> especificaciones = new HashMap<>();
        especificaciones.put("marca", "Medtronic");
        especificaciones.put("velocidad", "75000 rpm");
        especificaciones.put("tipo", "Electrico");

        equipo.setEspecificaciones(especificaciones);

        System.out.println("   Equipo: " + equipo.getNombre());
        System.out.println("   Categoria: " + equipo.getCategoria());
        System.out.println("   Especificaciones:");
        for (Map.Entry<String, Object> entrada : equipo.getEspecificaciones().entrySet()) {
            System.out.println("     - " + entrada.getKey() + ": " + entrada.getValue());
        }

        verificar("El equipo tiene 3 especificaciones", 3, equipo.getEspecificaciones().size());

        // Prueba de interfaz Verificable
        verificar("El equipo disponible cumple la verificacion", true, equipo.verificar());

        equipo.setDisponible(false);
        verificar("El equipo no disponible falla la verificacion", false, equipo.verificar());
    }

    // -----------------------------------------------------------------
    // 4. Constructor Copia Consumible
    // -----------------------------------------------------------------
    private static void probarConstructorCopiaConsumible() {
        titulo("4. Constructor Copia - Consumible");

        Consumible original = new Consumible(1, "Fresa diamante", "Medtronic", "70");
        Consumible copia = new Consumible(original);

        verificar("La copia conserva el nombre comercial", original.getNombreComercial(), copia.getNombreComercial());
        verificar("La copia conserva la medida", original.getMedida(), copia.getMedida());
        verificar("Original y copia son referencias distintas de objeto", true, original != copia);
    }

    // -----------------------------------------------------------------
    // 5. Constructor Copia Bandeja
    // -----------------------------------------------------------------
    private static void probarConstructorCopiaBandeja() {
        titulo("5. Constructor Copia - Bandeja Instrumental");

        Consumible fresa = new Consumible(1, "Fresa cortante", "Medtronic", "70");

        BandejaInstrumental original = new BandejaInstrumental();
        original.setId_bandeja(1);
        original.setTipo("CABEZA");
        original.setEsterilizado(true);
        original.agregarConsumible(fresa, 2);

        BandejaInstrumental copia = new BandejaInstrumental(original);

        verificar("La copia conserva el tipo de bandeja", original.getTipo(), copia.getTipo());
        verificar("La copia conserva la cantidad de consumibles", original.getConsumibles().size(), copia.getConsumibles().size());
        verificar("Original y copia son objetos distintos", true, original != copia);

        // Modificamos la copia para verificar la independencia del Map (Copia Defensiva)
        copia.eliminarConsumible(fresa);

        verificar("Modificar la copia mantiene intacto el Map del original", 1, original.getConsumibles().size());
        verificar("La copia redujo sus consumibles a cero", true, copia.getConsumibles().isEmpty());
    }

    // -----------------------------------------------------------------
    // 6. Constructor Copia Equipo
    // -----------------------------------------------------------------
    private static void probarConstructorCopiaEquipo() {
        titulo("6. Constructor Copia - Equipo");

        Equipo original = new Equipo();
        original.setId_equipo(1);
        original.setNombre("Neuronavegador 01");
        original.setCategoria(CategoriaEquipo.NAVEGADOR);
        original.setDisponible(true);

        Map<String, Object> especificaciones = new HashMap<>();
        especificaciones.put("marca", "Brainlab");
        especificaciones.put("pantalla", "Tactil");
        original.setEspecificaciones(especificaciones);

        Equipo copia = new Equipo(original);

        verificar("La copia conserva el nombre", original.getNombre(), copia.getNombre());
        verificar("La copia conserva la categoria", original.getCategoria(), copia.getCategoria());
        verificar("La copia conserva la cantidad de especificaciones", original.getEspecificaciones().size(), copia.getEspecificaciones().size());
        verificar("Original y copia son referencias de Equipo distintas", true, original != copia);

        // Modificamos especificaciones de la copia
        Map<String, Object> especificacionesCopia = copia.getEspecificaciones();
        especificacionesCopia.put("resolucion", "Full HD");
        copia.setEspecificaciones(especificacionesCopia);

        verificar("Modificar la copia no afecta el tamano del Map original", 2, original.getEspecificaciones().size());
        verificar("La copia incrementa sus especificaciones de forma independiente", 3, copia.getEspecificaciones().size());
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

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println(" " + texto);
        System.out.println("=".repeat(70));
    }

    private static void resumen() {
        titulo("RESUMEN ROL 2");
        System.out.println(" Verificaciones ejecutadas: " + verificaciones);
        System.out.println(" Fallos: " + fallos);
        System.out.println(fallos == 0
                ? " RESULTADO: El modulo de agenda de operaciones funciona correctamente."
                : " RESULTADO: Hay fallos en el modulo de agenda de operaciones.");
        System.out.println();
    }
}