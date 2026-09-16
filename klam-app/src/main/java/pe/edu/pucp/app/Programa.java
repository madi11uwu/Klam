package pe.edu.pucp.app;

public class Programa {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("            SISTEMA BIOKLAM - PRUEBA INTEGRAL DE ROLES                ");
        System.out.println("======================================================================\n");

        try {
            // Rol 1: Agenda Alertas (Cirugias y Notificaciones)
            PruebaAgendaAlertas.ejecutar();

            // Rol 2: Agenda Operaciones / Inventario (Equipos y Consumibles)
            PruebaAgendaOperaciones.ejecutar();

            // Rol 3: Clientes (Clinicas / Hospitales y Pacientes Particulares)
            PruebaClientes.ejecutar();

            // Rol 4: Usuarios, Roles y Permisos
            PruebaUsuarios.ejecutar();

            // Rol 5: Gestion Documental de Ingreso
            PruebaGestionDocumental.ejecutar();

            // Rol 6: Documentacion y Finanzas / Facturacion
            PruebaFacturacion.ejecutar();

            System.out.println("======================================================================");
            System.out.println("     TODAS LAS PRUEBAS DEL SISTEMA HAN FINALIZADO CON EXITO           ");
            System.out.println("======================================================================");

        } catch (Exception e) {
            System.err.println("\n[ERROR CRITICO] Ocurrio un fallo no controlado durante las pruebas:");
            e.printStackTrace();
        }
    }
}