package pe.edu.pucp.app;

import java.time.LocalDateTime;
import java.util.UUID;

import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;
import pe.edu.pucp.klam.modelo.comunicaciones.Notificacion;

/**
 * Prueba del modulo Agenda y Alertas (Rol 1).
 *
 * Verifica el correcto funcionamiento de Notificaciones, registro de Cirugias
 * y la implementacion de la interfaz Cancelable.
 */
public class PruebaAgendaAlertas {

    private static int verificaciones = 0;
    private static int fallos = 0;

    public static void ejecutar() {
        verificaciones = 0;
        fallos = 0;

        titulo("MODULO DE AGENDA Y ALERTAS - ROL 1");

        probarNotificacion();
        probarCirugiaYRelaciones();
        probarCancelacionCirugia();

        resumen();
    }

    // -----------------------------------------------------------------
    // 1. Prueba de Notificaciones
    // -----------------------------------------------------------------
    private static void probarNotificacion() {
        titulo("1. Creacion y lectura de Notificacion");

        String idNotif = UUID.randomUUID().toString();
        LocalDateTime fechaActual = LocalDateTime.now();

        Notificacion notificacion = new Notificacion(
                idNotif,
                fechaActual,
                "Alerta: Falta instrumental BAN-002",
                false
        );

        System.out.println("   ID Notificacion : " + notificacion.getId_notifacion());
        System.out.println("   Fecha y Hora    : " + notificacion.getFechaHora());
        System.out.println("   Titulo          : " + notificacion.getTitulo());

        verificar("Asigna correctamente el ID generado", idNotif, notificacion.getId_notifacion());
        verificar("El estado inicial leido es falso (NO)", false, notificacion.isEstado_leida());

        notificacion.setEstado_leida(true);
        verificar("El estado cambia a leido (SI) tras actualizar", true, notificacion.isEstado_leida());
    }

    // -----------------------------------------------------------------
    // 2. Prueba de Cirugia y Relaciones
    // -----------------------------------------------------------------
    // -----------------------------------------------------------------
    // 2. Prueba de Cirugia y Relaciones
    // -----------------------------------------------------------------
    private static void probarCirugiaYRelaciones() {
        titulo("2. Registro de Cirugia y asignacion de recursos");

        Equipo equipoA = new Equipo(1, "Equipo Quirurgico A");
        BandejaInstrumental bandeja01 = new BandejaInstrumental(1, "BAN-001");

        Cirugia cirugia = new Cirugia();
        cirugia.setId_cirugia(101);
        cirugia.setFechaHoraInicio(LocalDateTime.of(2026, 9, 20, 8, 0));
        cirugia.setFechaHoraFin(LocalDateTime.of(2026, 9, 20, 10, 30));
        cirugia.setTipoProcedimiento("Apendicectomia Laparoscopica");
        cirugia.setDoctornombre("Dr. Roberto Gomez");
        cirugia.setEstado(EstadoCirugia.PROGRAMADA);
        cirugia.setEquipo(equipoA);
        cirugia.setBandejaInstrumental(bandeja01);

        System.out.println("   Procedimiento   : " + cirugia.getTipoProcedimiento());
        System.out.println("   Doctor a cargo  : " + cirugia.getDoctornombre());

        // CORRECCION 1: Se pasa "101" como String en lugar del entero 101
        verificar("Identificador agendable correcto", "101", cirugia.getIdentificador());

        verificar("Estado inicial es PROGRAMADA", EstadoCirugia.PROGRAMADA, cirugia.getEstado());
        verificar("Equipo asignado correctamente", "Equipo Quirurgico A", cirugia.getEquipo().getNombre());

        // CORRECCION 2: Se usa .getNombre() (o .getCodigo()) en lugar de .getId_bandeja()
        verificar("Bandeja asignada correctamente", "BAN-001", cirugia.getBandejaInstrumental().getTipo());
    }

    // -----------------------------------------------------------------
    // 3. Prueba de Interfaz Cancelable
    // -----------------------------------------------------------------
    private static void probarCancelacionCirugia() {
        titulo("3. Cancelacion de Cirugia (Interfaz Cancelable)");

        Cirugia cirugia = new Cirugia();
        cirugia.setId_cirugia(102);
        cirugia.setEstado(EstadoCirugia.PROGRAMADA);

        String motivo = "Paciente no cumplio con el tiempo de ayuno requerido";
        cirugia.cancelar(motivo);

        System.out.println("   Nuevo Estado       : " + cirugia.getEstado());
        System.out.println("   Motivo Cancelacion : " + cirugia.getMotivoCancelacion());

        verificar("El estado cambia a CANCELADA", EstadoCirugia.CANCELADA, cirugia.getEstado());
        verificar("El motivo de cancelacion se guarda correctamente", motivo, cirugia.getMotivoCancelacion());
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
        titulo("RESUMEN ROL 1");
        System.out.println(" Verificaciones ejecutadas: " + verificaciones);
        System.out.println(" Fallos: " + fallos);
        System.out.println(fallos == 0
                ? " RESULTADO: El modulo de agenda y alertas funciona correctamente."
                : " RESULTADO: Hay fallos en el modulo de agenda y alertas.");
        System.out.println();
    }
}