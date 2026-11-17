package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;

public final class GeneradorHashPassword {

    private GeneradorHashPassword() {
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            comprobaciones();
            System.out.println();
            System.out.println("Para generar hashes, pase pares etiqueta=contraseña como argumentos.");
            System.out.println("Ejemplo: admin.rojas=ClaveAdmin2026 vend.torres=ClaveVend2026");
            return;
        }
        generar(args);
    }

    private static void generar(String[] args) {
        System.out.println("=== Valores para DML_KLAM.sql ===");
        for (String argumento : args) {
            int igual = argumento.indexOf('=');
            String etiqueta = igual < 0 ? "(sin etiqueta)" : argumento.substring(0, igual);
            String password = igual < 0 ? argumento : argumento.substring(igual + 1);
            try {
                Contrasenas.validarFormato(password);
                System.out.println(etiqueta + "  ->  " + Contrasenas.hashear(password));
            } catch (BLException ex) {
                System.out.println(etiqueta + "  ->  RECHAZADA: " + ex.getMessage());
            }
        }
        System.out.println();
        System.out.println("Recuerde: al DML solo van estos valores. Las contraseñas en claro");
        System.out.println("se comunican al equipo por fuera del repositorio.");
    }

    private static void comprobaciones() {
        String password = "Klam2026segura";
        String primero;
        String segundo;
        try {
            primero = Contrasenas.hashear(password);
            segundo = Contrasenas.hashear(password);
        } catch (BLException e) {
            System.out.println("Error al hashear: " + e.getMessage());
            return;
        }

        System.out.println("=== Comprobaciones del bloque 2 (RF01) ===");
        System.out.println("valor 1 : " + primero);
        System.out.println("valor 2 : " + segundo);
        System.out.println();

        verificar("Misma contraseña produce valores distintos (sal única)",
                !primero.equals(segundo));
        verificar("Verifica correctamente contra su propio valor",
                Contrasenas.coincide(password, primero));
        verificar("Verifica correctamente contra el segundo valor",
                Contrasenas.coincide(password, segundo));
        verificar("Rechaza una contraseña incorrecta",
                !Contrasenas.coincide("otraClave123", primero));
        verificar("Rechaza un valor guardado nulo",
                !Contrasenas.coincide(password, null));
        verificar("Rechaza un valor guardado corrupto",
                !Contrasenas.coincide(password, "pbkdf2-sha256$roto"));
        verificar("Rechaza una contraseña nula",
                !Contrasenas.coincide(null, primero));
        verificar("Detecta como pendiente un valor heredado",
                Contrasenas.requiereMigracion("2y10$examplehash0000000000000000000001"));
        verificar("No marca como pendiente un valor ya migrado",
                !Contrasenas.requiereMigracion(primero));
        verificar("Sigue aceptando una cuenta aún sin migrar",
                Contrasenas.coincide("claveAntigua", "claveAntigua"));
        verificar("Rechaza la contraseña equivocada en una cuenta sin migrar",
                !Contrasenas.coincide("otraCosa", "claveAntigua"));
        verificar("La política de formato sigue rechazando una clave débil",
                formatoRechazado("corta1"));
        verificar("La política de formato acepta una clave válida",
                !formatoRechazado(password));

        System.out.println();
        long inicio = System.nanoTime();
        Contrasenas.coincide(password, primero);
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        System.out.println("Costo de una verificación: " + ms + " ms");
        System.out.println("Objetivo: entre 200 y 500 ms en la máquina de la demostración.");
        System.out.println("Si queda fuera de ese rango, ajuste ITERACIONES en Contrasenas.");
    }

    private static boolean formatoRechazado(String password) {
        try {
            Contrasenas.validarFormato(password);
            return false;
        } catch (BLException ex) {
            return true;
        }
    }

    private static void verificar(String descripcion, boolean correcto) {
        System.out.println((correcto ? "[ OK  ] " : "[FALLA] ") + descripcion);
    }
}
