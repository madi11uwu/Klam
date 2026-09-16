package pe.edu.pucp.app;

import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.CategoriaEquipo;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;

import java.util.HashMap;
import java.util.Map;
public class Programa {
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println(" PRUEBA DEL MODULO DE INVENTARIO - ROL 2");
        System.out.println("==============================================");

        probarConsumible();
        probarBandejaInstrumental();
        probarEquipo();
        probarConstructorCopiaConsumible();
        probarConstructorCopiaBandeja();
        probarConstructorCopiaEquipo();
    }
    // ============================================================
    // CONSUMIBLE
    // ============================================================

    private static void probarConsumible() {

        System.out.println();
        System.out.println("----- PRUEBA CONSUMIBLE -----");

        Consumible consumible = new Consumible(
                1,
                "Fresa cortante",
                "Medtronic",
                "70"
        );

        System.out.println("Nombre: " + consumible.getNombreComercial());
        System.out.println("Marca: " + consumible.getMarca());
        System.out.println("Medida: " + consumible.getMedida());

        mostrarPrueba(
                "Nombre del consumible",
                consumible.getNombreComercial().equals("Fresa cortante")
        );

        mostrarPrueba(
                "Medida del consumible",
                consumible.getMedida().equals("70")
        );
    }


    // ============================================================
    // BANDEJA INSTRUMENTAL
    // ============================================================

    private static void probarBandejaInstrumental() {

        System.out.println();
        System.out.println("----- PRUEBA BANDEJA INSTRUMENTAL -----");

        Consumible fresaCortante =
                new Consumible(
                        1,
                        "Fresa cortante",
                        "Medtronic",
                        "70"
                );

        Consumible fresaDiamante =
                new Consumible(
                        2,
                        "Fresa diamante",
                        "Medtronic",
                        "70"
                );

        Consumible cuchilla =
                new Consumible(
                        3,
                        "Cuchilla quirurgica",
                        "Medtronic",
                        "70"
                );


        BandejaInstrumental bandeja =
                new BandejaInstrumental();

        bandeja.setId_bandeja(1);
        bandeja.setTipo("CABEZA");
        bandeja.setEsterilizado(true);


        bandeja.agregarConsumible(
                fresaCortante,
                2
        );

        bandeja.agregarConsumible(
                fresaDiamante,
                1
        );

        bandeja.agregarConsumible(
                cuchilla,
                3
        );


        System.out.println(
                "Tipo de bandeja: "
                        + bandeja.getTipo()
        );

        System.out.println(
                "Cantidad de tipos de consumibles: "
                        + bandeja.getConsumibles().size()
        );


        for (Map.Entry<Consumible, Integer> entrada
                : bandeja.getConsumibles().entrySet()) {

            System.out.println(
                    entrada.getKey().getNombreComercial()
                            + " - Medida: "
                            + entrada.getKey().getMedida()
                            + " - Cantidad: "
                            + entrada.getValue()
            );
        }


        mostrarPrueba(
                "La bandeja contiene 3 tipos de consumibles",
                bandeja.getConsumibles().size() == 3
        );

        mostrarPrueba(
                "La fresa cortante tiene cantidad 2",
                bandeja.obtenerCantidadConsumible(fresaCortante) == 2
        );

        // Este verificar() ES EL DE TU INTERFAZ VERIFICABLE

        mostrarPrueba(
                "La bandeja esterilizada cumple la verificacion",
                bandeja.verificar()
        );


        bandeja.setEsterilizado(false);

        mostrarPrueba(
                "Una bandeja no esterilizada no cumple la verificacion",
                !bandeja.verificar()
        );
    }


    // ============================================================
    // EQUIPO
    // ============================================================

    private static void probarEquipo() {

        System.out.println();
        System.out.println("----- PRUEBA EQUIPO -----");

        Equipo equipo = new Equipo();

        equipo.setId_equipo(1);
        equipo.setNombre("Craneotomo 01");
        equipo.setCategoria(
                CategoriaEquipo.CRANEOTOMO
        );
        equipo.setDisponible(true);


        Map<String, Object> especificaciones =
                new HashMap<>();

        especificaciones.put(
                "marca",
                "Medtronic"
        );

        especificaciones.put(
                "velocidad",
                "75000 rpm"
        );

        especificaciones.put(
                "tipo",
                "Electrico"
        );


        equipo.setEspecificaciones(
                especificaciones
        );


        System.out.println(
                "Equipo: "
                        + equipo.getNombre()
        );

        System.out.println(
                "Categoria: "
                        + equipo.getCategoria()
        );


        System.out.println(
                "Especificaciones:"
        );


        for (Map.Entry<String, Object> entrada
                : equipo.getEspecificaciones().entrySet()) {

            System.out.println(
                    entrada.getKey()
                            + ": "
                            + entrada.getValue()
            );
        }


        mostrarPrueba(
                "El equipo tiene 3 especificaciones",
                equipo.getEspecificaciones().size() == 3
        );


        // verificar() de Verificable

        mostrarPrueba(
                "El equipo disponible cumple la verificacion",
                equipo.verificar()
        );


        equipo.setDisponible(false);


        mostrarPrueba(
                "El equipo no disponible no cumple la verificacion",
                !equipo.verificar()
        );
    }


    // ============================================================
    // CONSTRUCTOR COPIA CONSUMIBLE
    // ============================================================

    private static void probarConstructorCopiaConsumible() {

        System.out.println();
        System.out.println("----- CONSTRUCTOR COPIA CONSUMIBLE -----");


        Consumible original =
                new Consumible(
                        1,
                        "Fresa diamante",
                        "Medtronic",
                        "70"
                );


        Consumible copia =
                new Consumible(original);


        mostrarPrueba(
                "La copia conserva el nombre comercial",
                copia.getNombreComercial()
                        .equals(original.getNombreComercial())
        );


        mostrarPrueba(
                "La copia conserva la medida",
                copia.getMedida()
                        .equals(original.getMedida())
        );


        mostrarPrueba(
                "Original y copia son objetos diferentes",
                original != copia
        );
    }


    // ============================================================
    // CONSTRUCTOR COPIA BANDEJA
    // ============================================================

    private static void probarConstructorCopiaBandeja() {

        System.out.println();
        System.out.println("----- CONSTRUCTOR COPIA BANDEJA -----");


        Consumible fresa =
                new Consumible(
                        1,
                        "Fresa cortante",
                        "Medtronic",
                        "70"
                );


        BandejaInstrumental original =
                new BandejaInstrumental();

        original.setId_bandeja(1);
        original.setTipo("CABEZA");
        original.setEsterilizado(true);

        original.agregarConsumible(
                fresa,
                2
        );


        BandejaInstrumental copia =
                new BandejaInstrumental(
                        original
                );


        mostrarPrueba(
                "La copia conserva el tipo de bandeja",
                copia.getTipo()
                        .equals(original.getTipo())
        );


        mostrarPrueba(
                "La copia conserva los consumibles",
                copia.getConsumibles().size()
                        ==
                        original.getConsumibles().size()
        );


        mostrarPrueba(
                "Original y copia son Bandejas diferentes",
                original != copia
        );


        // Modificamos la copia

        copia.eliminarConsumible(
                fresa
        );


        mostrarPrueba(
                "Modificar la copia no modifica el Map del original",
                original.getConsumibles().size() == 1
        );


        mostrarPrueba(
                "La copia queda sin consumibles",
                copia.getConsumibles().isEmpty()
        );
    }


    // ============================================================
    // CONSTRUCTOR COPIA EQUIPO
    // ============================================================

    private static void probarConstructorCopiaEquipo() {

        System.out.println();
        System.out.println("----- CONSTRUCTOR COPIA EQUIPO -----");


        Equipo original =
                new Equipo();

        original.setId_equipo(1);
        original.setNombre(
                "Neuronavegador 01"
        );

        original.setCategoria(
                CategoriaEquipo.NAVEGADOR
        );

        original.setDisponible(true);


        Map<String, Object> especificaciones =
                new HashMap<>();

        especificaciones.put(
                "marca",
                "Brainlab"
        );

        especificaciones.put(
                "pantalla",
                "Tactil"
        );


        original.setEspecificaciones(
                especificaciones
        );


        Equipo copia =
                new Equipo(
                        original
                );


        mostrarPrueba(
                "La copia conserva el nombre",
                copia.getNombre()
                        .equals(original.getNombre())
        );


        mostrarPrueba(
                "La copia conserva la categoria",
                copia.getCategoria()
                        ==
                        original.getCategoria()
        );


        mostrarPrueba(
                "La copia conserva las especificaciones",
                copia.getEspecificaciones().size()
                        ==
                        original.getEspecificaciones().size()
        );


        mostrarPrueba(
                "Original y copia son Equipos diferentes",
                original != copia
        );


        // Modificamos las especificaciones de la copia

        Map<String, Object> especificacionesCopia =
                copia.getEspecificaciones();

        especificacionesCopia.put(
                "resolucion",
                "Full HD"
        );

        copia.setEspecificaciones(
                especificacionesCopia
        );


        mostrarPrueba(
                "Modificar especificaciones de la copia no modifica el original",
                original.getEspecificaciones().size() == 2
        );


        mostrarPrueba(
                "La copia tiene una especificacion adicional",
                copia.getEspecificaciones().size() == 3
        );
    }


    // ============================================================
    // METODO AUXILIAR DEL MAIN
    // NO ES LA INTERFAZ VERIFICABLE
    // ============================================================

    private static void mostrarPrueba(
            String descripcion,
            boolean resultado) {

        if (resultado) {

            System.out.println(
                    "[OK] "
                            + descripcion
            );

        } else {

            System.out.println(
                    "[FALLO] "
                            + descripcion
            );
        }
    }
}
