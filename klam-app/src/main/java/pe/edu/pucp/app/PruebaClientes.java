package pe.edu.pucp.app;

import java.time.LocalDateTime;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
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
        probarCirugiasDelCliente(clinica);
        probarEliminacionLogica(paciente);

        resumen();
    }

    private static ClinicaHospital probarCreacionClinica() {
        titulo("1. Creacion de Clinica / Hospital (RUC)");
        ClinicaHospital clinica = new ClinicaHospital(1, "Clinica Delgado",
                "Av. Angamos 400", "contacto@delgado.pe", "999888777",
                "20123456789", true, "30 dias");
        System.out.println("   Registrando: " + clinica.getNombre());
        verificar("El RUC asignado es correcto", "20123456789", clinica.getRuc());
        return clinica;
    }

    private static PacienteParticular probarCreacionPaciente() {
        titulo("2. Creacion de Paciente Particular (DNI)");
        PacienteParticular paciente = new PacienteParticular(2, "Juan Perez",
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

    private static void probarCirugiasDelCliente(Cliente cliente) {
        titulo("4. Cirugias del Cliente");

        verificar("Un cliente nace sin cirugias", 0, cliente.getCirugias().size());

        cliente.agregarCirugia(crearCirugia(1, "Craneotomia con navegacion"));
        cliente.agregarCirugia(crearCirugia(2, "Biopsia estereotaxica"));
        verificar("El cliente acumula sus cirugias", 2, cliente.getCirugias().size());
        verificar("Desde el cliente se llega al tipo de procedimiento de su cirugia",
                "Craneotomia con navegacion", cliente.getCirugias().get(0).getTipoProcedimiento());

        // la lista devuelta es una copia: modificarla no altera al cliente
        cliente.getCirugias().add(crearCirugia(3, "Cirugia ajena"));
        verificar("La lista devuelta es una copia defensiva", 2, cliente.getCirugias().size());

        try {
            cliente.agregarCirugia(null);
            registrarFallo("Un cliente no debe aceptar una cirugia nula", "acepto el null");
        } catch (IllegalArgumentException e) {
            verificar("agregarCirugia rechaza una cirugia nula", true, true);
        }
    }

    private static Cirugia crearCirugia(int id, String tipoProcedimiento) {
        Cirugia c = new Cirugia();
        c.setId_cirugia(id);
        c.setTipoProcedimiento(tipoProcedimiento);
        c.setFechaHoraInicio(LocalDateTime.now());
        return c;
    }

    private static void probarEliminacionLogica(Cliente cliente) {
        titulo("5. Eliminacion logica del Cliente");
        cliente.setActivo(false);
        verificar("El cliente se desactiva (activo = false)", false, cliente.isActivo());
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