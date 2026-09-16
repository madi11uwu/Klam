package pe.edu.pucp.app;

import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;

public class PruebaClientes {

    private static int verificaciones = 0;
    private static int fallos = 0;

    public static void main(String[] args) {
        ejecutar();
    }

    public static void ejecutar() {
        titulo("MODULO DE CLIENTES - ROL 3");

        ClinicaHospital clinica = probarCreacionClinica();
        PacienteParticular paciente = probarCreacionPaciente();

        probarPolimorfismoYEstado(clinica, paciente);
        probarEliminacionLogica(paciente);

        resumen();
    }

    private static ClinicaHospital probarCreacionClinica() {
        titulo("1. Creacion de Clinica / Hospital (RUC)");
        ClinicaHospital clinica = new ClinicaHospital("CLI-001", "Clinica Delgado",
                "Av. Angamos 400", "contacto@delgado.pe", "999888777",
                "20123456789", true, "30 dias");
        System.out.println("   Registrando: " + clinica.getNombre());
        verificar("El RUC asignado es correcto", "20123456789", clinica.getRuc());
        return clinica;
    }

    private static PacienteParticular probarCreacionPaciente() {
        titulo("2. Creacion de Paciente Particular (DNI)");
        PacienteParticular paciente = new PacienteParticular("PAC-001", "Juan Perez",
                "Av. Javier Prado 123", "juan.perez@email.com", "987654321",
                "70123456", false);
        System.out.println("   Registrando: " + paciente.getNombre());
        verificar("El DNI asignado es correcto", "70123456", paciente.getDni());
        return paciente;
    }

    private static void probarPolimorfismoYEstado(ClinicaHospital c, PacienteParticular p) {
        titulo("3. Polimorfismo de Clientes");
        Cliente cliente1 = c;
        Cliente cliente2 = p;
        verificar("La clinica es tratable como Cliente base", "CLI-001", cliente1.getId_cliente());
        verificar("El paciente es tratable como Cliente base", "PAC-001", cliente2.getId_cliente());
    }

    private static void probarEliminacionLogica(Cliente cliente) {
        titulo("4. Eliminacion logica del Cliente");
        cliente.setEstado(false);
        verificar("El cliente se desactiva (estado = false)", false, cliente.isEstado());
    }

    // -----------------------------------------------------------------
    // Utilidades (Sin cambios)
    // -----------------------------------------------------------------
    private static void verificar(String descripcion, Object esperado, Object obtenido) {
        verificaciones++;
        if (esperado.equals(obtenido)) {
            System.out.println("   [OK]     " + descripcion);
        } else {
            fallos++;
            System.out.println("   [FALLO]  " + descripcion
                    + "  (esperado: " + esperado + ", obtenido: " + obtenido + ")");
        }
    }

    private static void registrarFallo(String descripcion, String detalle) {
        verificaciones++; fallos++;
        System.out.println("   [FALLO]  " + descripcion + "  (" + detalle + ")");
    }

    private static void titulo(String texto) {
        System.out.println("\n" + "=".repeat(70) + "\n " + texto + "\n" + "=".repeat(70));
    }

    private static void resumen() {
        titulo("RESUMEN ROL 3");
        System.out.println(" Verificaciones ejecutadas: " + verificaciones);
        System.out.println(" Fallos: " + fallos);
        System.out.println(fallos == 0
                ? " RESULTADO: El modulo de clientes (Rol 3) funciona correctamente."
                : " RESULTADO: Hay fallos en el modulo de clientes (Rol 3).");
        System.out.println();
    }
}