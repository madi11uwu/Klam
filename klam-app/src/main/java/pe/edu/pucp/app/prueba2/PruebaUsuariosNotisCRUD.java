package pe.edu.pucp.app.prueba2;

import pe.edu.pucp.klam.bl.AdministradorBL;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.NotificacionBL;
import pe.edu.pucp.klam.bl.TecnicoInstrumentistaBL;
import pe.edu.pucp.klam.bl.VendedorBL;
import pe.edu.pucp.klam.bl.impl.AdministradorBLImpl;
import pe.edu.pucp.klam.bl.impl.NotificacionBLImpl;
import pe.edu.pucp.klam.bl.impl.TecnicoInstrumentistaBLImpl;
import pe.edu.pucp.klam.bl.impl.VendedorBLImpl;
import pe.edu.pucp.klam.modelo.comunicaciones.Notificacion;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Administrador;
import pe.edu.pucp.klam.modelo.usuariosPermisos.TecnicoInstrumentista;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;

import java.time.LocalDateTime;

/**
 * Prueba del CRUD de los módulos de Usuarios y Notificaciones contra la base de datos:
 * Administrador, Vendedor, TecnicoInstrumentista y Notificacion.
 * Usa solo la capa de negocio (BL), nunca los DAO directamente.
 */
public class PruebaUsuariosNotisCRUD {

    private static final AdministradorBL administradorBL = new AdministradorBLImpl();
    private static final VendedorBL vendedorBL = new VendedorBLImpl();
    private static final TecnicoInstrumentistaBL tecnicoBL = new TecnicoInstrumentistaBLImpl();
    private static final NotificacionBL notificacionBL = new NotificacionBLImpl();

    // Sufijo por ejecucion: username y email son UNIQUE y el delete es baja logica,
    // asi la prueba se puede correr varias veces sin "Duplicate entry".
    private static final String SUFIJO = String.valueOf(System.currentTimeMillis() % 1000000);

    public static void ejecutar() {
        System.out.println("==================================================");
        System.out.println("   CRUD USUARIOS Y NOTIFICACIONES (BL TESTS)      ");
        System.out.println("==================================================");

        probar("ADMINISTRADOR", PruebaUsuariosNotisCRUD::probarAdministrador);
        probar("VENDEDOR", PruebaUsuariosNotisCRUD::probarVendedor);
        probar("TÉCNICO INSTRUMENTISTA", PruebaUsuariosNotisCRUD::probarTecnicoInstrumentista);
        probar("NOTIFICACIÓN", PruebaUsuariosNotisCRUD::probarNotificacion);
    }

    // -----------------------------------------------------------------
    // ADMINISTRADOR
    // -----------------------------------------------------------------
    private static void probarAdministrador() {
        Administrador admin = new Administrador();
        admin.setUsername("admin_test" + SUFIJO);
        admin.setEmail("admin" + SUFIJO + "@klam.com");
        admin.setNombres("Carlos");
        admin.setApellidos("Pérez");
        admin.setPasswordHash("hash_de_prueba_123");

        administradorBL.insert(admin);
        int id = admin.getIdUsuario();
        System.out.println("[INSERT] id " + id);

        Administrador leido = administradorBL.findById(id);
        System.out.println("[FIND BY ID] " + describir(leido));

        leido.setNombres("Carlos Modificado");
        administradorBL.update(leido);
        System.out.println("[UPDATE] " + describir(administradorBL.findById(id)));

        System.out.println("[FIND ALL] total: " + administradorBL.findAll().size());

        administradorBL.delete(id);
        System.out.println("[DELETE] Eliminación completada. (Intentar buscar arrojará error/null si es borrado físico)");

        esperarError("administrador con correo inválido", () -> {
            Administrador invalido = new Administrador();
            invalido.setUsername("admin_fail");
            invalido.setEmail("correo-invalido");
            invalido.setNombres("Juan");
            invalido.setApellidos("Pérez");
            invalido.setPasswordHash("hash_fail");
            administradorBL.insert(invalido);
        });
    }

    // -----------------------------------------------------------------
    // VENDEDOR
    // -----------------------------------------------------------------
    private static void probarVendedor() {
        Vendedor vendedor = new Vendedor();
        vendedor.setUsername("vendedor_test" + SUFIJO);
        vendedor.setEmail("ventas" + SUFIJO + "@klam.com");
        vendedor.setNombres("María");
        vendedor.setApellidos("Gómez");
        vendedor.setComisionAcumulada(150.50);
        vendedor.setPasswordHash("hash_de_prueba_123");

        vendedorBL.insert(vendedor);
        int id = vendedor.getIdUsuario();
        System.out.println("[INSERT] id " + id);

        Vendedor leido = vendedorBL.findById(id);
        System.out.println("[FIND BY ID] " + describir(leido));

        leido.setComisionAcumulada(250.75);
        vendedorBL.update(leido);
        System.out.println("[UPDATE] " + describir(vendedorBL.findById(id)));

        System.out.println("[FIND ALL] total: " + vendedorBL.findAll().size());

        vendedorBL.delete(id);
        System.out.println("[DELETE] Eliminación completada.");

        esperarError("vendedor con comisión negativa", () -> {
            Vendedor invalido = new Vendedor();
            invalido.setUsername("vendedor_fail");
            invalido.setEmail("fail@klam.com");
            invalido.setNombres("Ana");
            invalido.setApellidos("López");
            invalido.setComisionAcumulada(-50.0);
            invalido.setPasswordHash("hash_fail");
            vendedorBL.insert(invalido);
        });
    }

    // -----------------------------------------------------------------
    // TÉCNICO INSTRUMENTISTA
    // -----------------------------------------------------------------
    private static void probarTecnicoInstrumentista() {
        TecnicoInstrumentista tecnico = new TecnicoInstrumentista();
        tecnico.setUsername("tecnico_test" + SUFIJO);
        tecnico.setEmail("tecnico" + SUFIJO + "@klam.com");
        tecnico.setNombres("Jorge");
        tecnico.setApellidos("Salinas");
        tecnico.setEspecialidad("Neurocirugía");
        tecnico.setPasswordHash("hash_de_prueba_123");

        tecnicoBL.insert(tecnico);
        int id = tecnico.getIdUsuario();
        System.out.println("[INSERT] id " + id);

        TecnicoInstrumentista leido = tecnicoBL.findById(id);
        System.out.println("[FIND BY ID] " + describir(leido));

        leido.setEspecialidad("Traumatología");
        tecnicoBL.update(leido);
        System.out.println("[UPDATE] " + describir(tecnicoBL.findById(id)));

        System.out.println("[FIND ALL] total: " + tecnicoBL.findAll().size());

        tecnicoBL.delete(id);
        System.out.println("[DELETE] Eliminación completada.");

        esperarError("técnico sin especialidad", () -> {
            TecnicoInstrumentista invalido = new TecnicoInstrumentista();
            invalido.setUsername("tec_fail");
            invalido.setEmail("failtec@klam.com");
            invalido.setNombres("Luis");
            invalido.setApellidos("Torres");
            invalido.setEspecialidad("");
            invalido.setPasswordHash("hash_fail");
            tecnicoBL.insert(invalido);
        });
    }

    // -----------------------------------------------------------------
    // NOTIFICACIÓN
    // -----------------------------------------------------------------
    private static void probarNotificacion() {
        // 1. Necesitamos un usuario real en la base de datos para cumplir la FK del destinatario.
        Administrador adminDestino = new Administrador();
        adminDestino.setUsername("admin_noti" + SUFIJO);
        adminDestino.setEmail("adminnoti" + SUFIJO + "@klam.com");
        adminDestino.setNombres("Usuario");
        adminDestino.setApellidos("Destino");
        adminDestino.setPasswordHash("hash_temp");
        administradorBL.insert(adminDestino); // Lo insertamos para que se le asigne un ID en BD

        System.out.println("  [PRE-STEP] Creado destinatario de prueba con ID: " + adminDestino.getIdUsuario());

        // 2. Creamos la notificación asignando el destinatario con el nuevo constructor
        Notificacion noti = new Notificacion(
                0, // ID 0 o autogenerado
                LocalDateTime.now(),
                "Alerta de inventario bajo",
                "El producto X está por debajo del stock mínimo (5 unidades).",
                false,
                adminDestino
        );

        notificacionBL.insert(noti);
        int id = noti.getIdNotificacion(); // Nuevo nombre de getter
        System.out.println("[INSERT] id " + id);

        Notificacion leida = notificacionBL.findById(id);
        System.out.println("[FIND BY ID] " + describir(leida));

        leida.setEstadoLeida(true); // Nuevo nombre de setter
        leida.setTitulo("Alerta de inventario resuelta");
        notificacionBL.update(leida);
        System.out.println("[UPDATE] " + describir(notificacionBL.findById(id)));

        System.out.println("[FIND ALL] total: " + notificacionBL.findAll().size());

        // 3. Limpiamos los datos
        notificacionBL.delete(id);
        System.out.println("[DELETE] Notificación eliminada completada.");

        administradorBL.delete(adminDestino.getIdUsuario());
        System.out.println("  [POST-STEP] Destinatario de prueba eliminado.");

        // 4. Pruebas de error
        esperarError("notificación sin título", () -> {
            Notificacion invalida = new Notificacion(
                    0,
                    LocalDateTime.now(),
                    "", // Titulo vacío provocará BLException
                    "Un mensaje cualquiera",
                    false,
                    adminDestino
            );
            notificacionBL.insert(invalida);
        });

        esperarError("notificación sin destinatario válido", () -> {
            UsuarioPlataforma usuarioFantasma = new Administrador();
            // Usuario con ID 0, inválido según tu BL

            Notificacion invalida = new Notificacion(
                    0,
                    LocalDateTime.now(),
                    "Título válido",
                    "Mensaje válido",
                    false,
                    usuarioFantasma
            );
            notificacionBL.insert(invalida);
        });
    }

    // -----------------------------------------------------------------
    // Utilitarios
    // -----------------------------------------------------------------
    private static void probar(String nombre, Runnable prueba) {
        System.out.println("\n--- " + nombre + " ---");
        try {
            prueba.run();
        } catch (BLException e) {
            System.out.println("[ERROR] " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("        Causa: " + e.getCause().getMessage());
            }
        }
    }

    private static void esperarError(String caso, Runnable accion) {
        try {
            accion.run();
            System.out.println("[VALIDACION] FALLO: se permitió " + caso);
        } catch (BLException e) {
            System.out.println("[VALIDACION] OK (" + caso + "): " + e.getMessage());
        }
    }

    private static String describir(Administrador a) {
        if (a == null) return "Nulo";
        return "username=" + a.getUsername() + ", email=" + a.getEmail()
                + ", nombres=" + a.getNombres() + " " + a.getApellidos();
    }

    private static String describir(Vendedor v) {
        if (v == null) return "Nulo";
        return "username=" + v.getUsername() + ", email=" + v.getEmail()
                + ", comisión acumulada=" + v.getComisionAcumulada();
    }

    private static String describir(TecnicoInstrumentista t) {
        if (t == null) return "Nulo";
        return "username=" + t.getUsername() + ", email=" + t.getEmail()
                + ", especialidad=" + t.getEspecialidad();
    }

    private static String describir(Notificacion n) {
        if (n == null) return "Nulo";

        String tipoDest = "Desconocido";
        if (n.getDestinatario() != null) {
            tipoDest = n.getDestinatario().getClass().getSimpleName() + " (ID: " + n.getDestinatario().getIdUsuario() + ")";
        }

        return "título=" + n.getTitulo() + ", mensaje=" + n.getMensaje() + ", fechaHora=" + n.getFechaHora()
                + ", leída=" + n.isEstadoLeida() + ", destinatario=" + tipoDest;
    }

    public static void main(String[] args) {
        ejecutar();
    }
}