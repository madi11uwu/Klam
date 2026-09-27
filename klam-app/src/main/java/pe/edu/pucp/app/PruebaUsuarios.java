package pe.edu.pucp.app;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.klam.modelo.usuariosPermisos.Administrador;
import pe.edu.pucp.klam.modelo.usuariosPermisos.TecnicoInstrumentista;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;

/**
 * Prueba del modulo de Usuarios, Roles y Permisos (Rol 4).
 *
 * Verifica el soporte del dominio al modelo de negocio de BIOKLAM:
 * creacion de usuarios de los 3 roles, polimorfismo sobre UsuarioPlataforma,
 * constructores de copia, logica propia de comisiones, eliminado logico (activo/inactivo)
 * y metodos pendientes de integracion.
 */
public class PruebaUsuarios {

    private static int verificaciones = 0;
    private static int fallos = 0;

    public static void ejecutar() {
        verificaciones = 0;
        fallos = 0;

        titulo("MODULO DE USUARIOS, ROLES Y CONEXION A AWS - ROL 4");

        Administrador admin = crearAdministrador(1, "jsebastian", "jsebastian@bioklam.com",
                "Jose Sebastian", "Parra Salazar", "ADM-001");
        Vendedor vendedor = crearVendedor(2, "mfromero", "mfromero@bioklam.com",
                "Maria Fernanda", "Romero Ilave", "VEN-001");
        TecnicoInstrumentista tecnico = crearTecnico(3, "hcabello", "hcabello@bioklam.com",
                "Halim Samir", "Cabello Mendoza", "TEC-001", "Neurocirugia");

        probarCreacionYAtributos(admin, vendedor, tecnico);
        probarPolimorfismo(admin, vendedor, tecnico);
        probarConstructorDeCopia(vendedor);
        probarLogicaDeComision(vendedor);
        probarEliminadoLogico(admin, vendedor, tecnico);
        probarMetodosPendientesDeIntegracion(admin, vendedor, tecnico);

        resumen();
    }

    // -----------------------------------------------------------------
    // 1. Creacion de usuarios de cada rol y sus atributos
    // -----------------------------------------------------------------
    private static void probarCreacionYAtributos(Administrador admin, Vendedor vendedor,
                                                 TecnicoInstrumentista tecnico) {
        titulo("1. Creacion de usuarios de cada rol");

        System.out.println("   " + admin);
        System.out.println("   " + vendedor);
        System.out.println("   " + tecnico);

        verificar("El administrador nace con rol ADMINISTRADOR",
                "ADMINISTRADOR", admin.getRol());
        verificar("El vendedor nace con rol VENDEDOR",
                "VENDEDOR", vendedor.getRol());
        verificar("El tecnico nace con rol TECNICO_INSTRUMENTISTA",
                "TECNICO_INSTRUMENTISTA", tecnico.getRol());
        verificar("El id de negocio del administrador se guarda correctamente",
                "ADM-001", admin.getiAdmin());
        verificar("El id de negocio del vendedor se guarda correctamente",
                "VEN-001", vendedor.getIdVendedor());
        verificar("El id de negocio del tecnico se guarda correctamente",
                "TEC-001", tecnico.getIdTecnico());
        verificar("El nombre completo se arma a partir de nombres y apellidos",
                "Maria Fernanda Romero Ilave", vendedor.getNombreCompleto());
        verificar("Todos los usuarios nacen activos",
                true, admin.isActivo() && vendedor.isActivo() && tecnico.isActivo());
    }

    // -----------------------------------------------------------------
    // 2. Polimorfismo sobre UsuarioPlataforma
    // -----------------------------------------------------------------
    private static void probarPolimorfismo(Administrador admin, Vendedor vendedor,
                                           TecnicoInstrumentista tecnico) {
        titulo("2. Polimorfismo (lista de UsuarioPlataforma)");

        List<UsuarioPlataforma> usuarios = new ArrayList<>();
        usuarios.add(admin);
        usuarios.add(vendedor);
        usuarios.add(tecnico);

        for (UsuarioPlataforma u : usuarios) {
            System.out.println("   - " + u.getNombreCompleto() + " | rol: " + u.getRol());
        }

        verificar("La lista polimorfica contiene los 3 usuarios",
                3, usuarios.size());
        verificar("El primer elemento sigue siendo un Administrador por dentro",
                true, usuarios.get(0) instanceof Administrador);
        verificar("El tercer elemento sigue siendo un TecnicoInstrumentista por dentro",
                true, usuarios.get(2) instanceof TecnicoInstrumentista);
    }

    // -----------------------------------------------------------------
    // 3. Constructor de copia: independencia entre objetos
    // -----------------------------------------------------------------
    private static void probarConstructorDeCopia(Vendedor vendedor) {
        titulo("3. Constructor de copia");

        Vendedor copia = new Vendedor(vendedor);
        copia.setComisionAcumulada(999.0);

        verificar("La copia toma los mismos datos del original al crearse",
                vendedor.getIdVendedor(), copia.getIdVendedor());
        verificar("Modificar la copia no afecta al vendedor original",
                0.0, vendedor.getComisionAcumulada());
        verificar("La copia si quedo con el nuevo valor asignado",
                999.0, copia.getComisionAcumulada());
    }

    // -----------------------------------------------------------------
    // 4. Logica propia del Vendedor: calcularComision()
    // -----------------------------------------------------------------
    private static void probarLogicaDeComision(Vendedor vendedor) {
        titulo("4. Logica de comision (autocontenida, sin dependencias externas)");

        double comisionGenerada = vendedor.calcularComision(1000.0);
        System.out.println("   Comision generada por una venta de 1000: " + comisionGenerada);
        System.out.println("   Comision acumulada del vendedor: " + vendedor.getComisionAcumulada());

        verificar("La comision de una venta de 1000 es el 5% (50.0)",
                50.0, comisionGenerada);
        verificar("La comision acumulada del vendedor se actualiza",
                50.0, vendedor.getComisionAcumulada());

        vendedor.calcularComision(2000.0);
        verificar("Una segunda venta suma a la comision ya acumulada (50 + 100)",
                150.0, vendedor.getComisionAcumulada());
    }

    // -----------------------------------------------------------------
    // 5. Eliminado logico (activo/inactivo)
    // -----------------------------------------------------------------
    private static void probarEliminadoLogico(Administrador admin, Vendedor vendedor,
                                              TecnicoInstrumentista tecnico) {
        titulo("5. Eliminado logico");

        tecnico.setActivo(false);
        System.out.println("   Tecnico luego de desactivar -> " + tecnico);

        verificar("El tecnico desactivado queda con activo = false",
                false, tecnico.isActivo());

        List<UsuarioPlataforma> usuarios = new ArrayList<>();
        usuarios.add(admin);
        usuarios.add(vendedor);
        usuarios.add(tecnico);

        int activos = 0;
        for (UsuarioPlataforma u : usuarios) {
            if (u.isActivo()) {
                activos++;
            }
        }
        verificar("Solo quedan 2 usuarios activos tras desactivar al tecnico",
                2, activos);

        // Reactivamos para no afectar siguientes evaluaciones
        tecnico.setActivo(true);
    }

    // -----------------------------------------------------------------
    // 6. Metodos que dependen de otros modulos (aun no implementados)
    // -----------------------------------------------------------------
    private static void probarMetodosPendientesDeIntegracion(Administrador admin, Vendedor vendedor,
                                                             TecnicoInstrumentista tecnico) {
        titulo("6. Metodos pendientes de integracion con otros roles");

        verificarLanzaExcepcion("crearCirugia() aun depende del modulo de Agenda",
                () -> admin.crearCirugia(null));
        verificarLanzaExcepcion("registrarIncidencia() aun depende de la clase Cirugia",
                () -> tecnico.registrarIncidencia(null, "Falla en craneotomo"));
        verificarLanzaExcepcion("consultarVentas() aun depende del modulo de ventas/cirugias",
                () -> vendedor.consultarVentas());
        verificarLanzaExcepcion("observarCalendario() aun depende del modulo de Agenda",
                () -> admin.observarCalendario());
    }

    // -----------------------------------------------------------------
    // Utilidades
    // -----------------------------------------------------------------
    private static Administrador crearAdministrador(int idUsuario, String username, String email,
                                                    String nombres, String apellidos, String idAdmin) {
        return new Administrador(idUsuario, username, "HASH_" + idUsuario, email,
                nombres, apellidos, true, idAdmin);
    }

    private static Vendedor crearVendedor(int idUsuario, String username, String email,
                                          String nombres, String apellidos, String idVendedor) {
        return new Vendedor(idUsuario, username, "HASH_" + idUsuario, email,
                nombres, apellidos, true, idVendedor, 0.0);
    }

    private static TecnicoInstrumentista crearTecnico(int idUsuario, String username, String email,
                                                      String nombres, String apellidos,
                                                      String idTecnico, String especialidad) {
        return new TecnicoInstrumentista(idUsuario, username, "HASH_" + idUsuario, email,
                nombres, apellidos, true, idTecnico, especialidad);
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

    private static void verificarLanzaExcepcion(String descripcion, Runnable accion) {
        verificaciones++;
        try {
            accion.run();
            fallos++;
            System.out.println("   [FALLO]  " + descripcion + "  (no lanzo excepcion)");
        } catch (UnsupportedOperationException e) {
            System.out.println("   [OK]     " + descripcion);
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println(" " + texto);
        System.out.println("=".repeat(70));
    }

    private static void resumen() {
        titulo("RESUMEN ROL 4");
        System.out.println(" Verificaciones ejecutadas: " + verificaciones);
        System.out.println(" Fallos: " + fallos);
        System.out.println(fallos == 0
                ? " RESULTADO: El dominio de usuarios soporta correctamente el modelo de negocio."
                : " RESULTADO: Hay verificaciones fallidas en el modulo de usuarios.");
        System.out.println();
    }
}