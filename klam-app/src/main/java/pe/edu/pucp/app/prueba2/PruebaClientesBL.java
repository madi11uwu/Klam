package pe.edu.pucp.app.prueba2;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.BaseBL;
import pe.edu.pucp.klam.bl.impl.ClinicaHospitalBLImpl;
import pe.edu.pucp.klam.bl.impl.PacienteParticularBLImpl;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;

/** Sin argumentos: validaciones locales. Con --crud: pruebas contra la BD. */
public class PruebaClientesBL {

    private static int correctas;
    private static int fallos;

    public static void main(String[] args) {
        correctas = 0;
        fallos = 0;
        if (args.length > 1 || (args.length == 1 && !args[0].equals("--crud"))) {
            throw new IllegalArgumentException("Uso: PruebaClientesBL [--crud]");
        }
        probarValidaciones("ClinicaHospital", new ClinicaHospitalBLImpl(),
                PruebaClientesBL::crearClinica);
        probarValidaciones("PacienteParticular", new PacienteParticularBLImpl(),
                PruebaClientesBL::crearPaciente);
        rechaza("RUC inválido", "El RUC debe tener exactamente 11 dígitos", () -> {
            ClinicaHospital c = crearClinica();
            c.setRuc("123");
            new ClinicaHospitalBLImpl().insert(c);
        });
        rechaza("DNI inválido", "El DNI debe tener exactamente 8 dígitos", () -> {
            PacienteParticular p = crearPaciente();
            p.setDni("ABCDEFGH");
            new PacienteParticularBLImpl().insert(p);
        });
        rechaza("Periodo de crédito demasiado largo",
                "El periodo de crédito no puede exceder 30 caracteres", () -> {
                    ClinicaHospital c = crearClinica();
                    c.setPeriodoCredito("x".repeat(31));
                    new ClinicaHospitalBLImpl().insert(c);
                });
        if (args.length == 1) {
            probarCrud("ClinicaHospital", new ClinicaHospitalBLImpl(),
                    PruebaClientesBL::crearClinica, c -> {
                        c.setTieneConsignacion(true);
                        c.setPeriodoCredito("60 dias");
                    });
            probarCrud("PacienteParticular", new PacienteParticularBLImpl(),
                    PruebaClientesBL::crearPaciente, p -> p.setPagoConfirmado(true));
        } else {
            System.out.println("CRUD de BD pendiente: ejecutar con --crud tras integrar los SP.");
        }
        System.out.printf("Resultado: %d verificaciones correctas; %d fallos.%n", correctas, fallos);
        if (fallos > 0) {
            throw new IllegalStateException("Fallaron " + fallos + " verificaciones");
        }
    }

    private static <T extends Cliente> void probarValidaciones(
            String tipo, BaseBL<T, Integer> bl, Supplier<T> crear) {
        rechaza(tipo + " nulo", "Debe proporcionar", () -> bl.insert(null));
        rechaza(tipo + " ID nulo", "El id debe ser un número positivo", () -> bl.findById(null));
        rechaza(tipo + " ID negativo", "El id debe ser un número positivo", () -> bl.delete(-1));
        validarCampo(tipo, bl, crear, c -> c.setNombre(null), "El nombre es obligatorio");
        validarCampo(tipo, bl, crear, c -> c.setNombre("  "), "El nombre es obligatorio");
        validarCampo(tipo, bl, crear, c -> c.setNombre("x".repeat(151)),
                "El nombre no puede exceder 150 caracteres");
        validarCampo(tipo, bl, crear, c -> c.setDireccion("x".repeat(201)),
                "La dirección no puede exceder 200 caracteres");
        validarCampo(tipo, bl, crear, c -> c.setTelefono("1".repeat(21)),
                "El teléfono no puede exceder 20 caracteres");
        validarCampo(tipo, bl, crear, c -> c.setEmailContacto("correo-invalido"),
                "El correo de contacto tiene un formato inválido");
        validarCampo(tipo, bl, crear, c -> c.setEmailContacto("x".repeat(121)),
                "El correo de contacto no puede exceder 120 caracteres");
    }

    private static <T extends Cliente> void validarCampo(String tipo, BaseBL<T, Integer> bl,
            Supplier<T> crear, Consumer<T> cambiar, String mensaje) {
        T cliente = crear.get();
        cambiar.accept(cliente);
        rechaza(tipo + " insert: " + mensaje, mensaje, () -> bl.insert(cliente));
        rechaza(tipo + " update: " + mensaje, mensaje, () -> bl.update(cliente));
    }

    private static <T extends Cliente> void probarCrud(String tipo, BaseBL<T, Integer> bl,
            Supplier<T> crear, Consumer<T> cambiarEspecificos) {
        T cliente = crear.get();
        boolean eliminado = false;
        try {
            bl.insert(cliente);
            exigir(cliente.getId_cliente() > 0, "Insert devuelve un ID positivo");
            System.out.println(tipo + ": registro de prueba ID " + cliente.getId_cliente());
            comparar(cliente, bl.findById(cliente.getId_cliente()));
            exigir(bl.findAll().stream().anyMatch(c -> c.getId_cliente() == cliente.getId_cliente()),
                    "FindAll incluye el registro insertado");

            // Incluso con el mismo ID, una segunda inserción debe rechazarse.
            rechaza(tipo + " documento duplicado", "Ya existe un registro con el",
                    () -> bl.insert(cliente));

            cliente.setNombre(cliente.getNombre() + " modificado");
            cliente.setDireccion("Direccion modificada");
            cliente.setEmailContacto("modificado@example.com");
            cliente.setTelefono("987654321");
            cambiarEspecificos.accept(cliente);
            bl.update(cliente);
            comparar(cliente, bl.findById(cliente.getId_cliente()));

            bl.delete(cliente.getId_cliente());
            T baja = bl.findById(cliente.getId_cliente());
            exigir(baja == null || !baja.isActivo(), "Delete da de baja el registro");
            exigir(bl.findAll().stream().noneMatch(c ->
                    c.getId_cliente() == cliente.getId_cliente() && c.isActivo()),
                    "FindAll no muestra como activo el registro eliminado");
            eliminado = true;
            correctas++;
            System.out.println("[OK] CRUD " + tipo);
        } catch (Exception e) {
            fallos++;
            System.err.println("[FALLO] CRUD " + tipo + ": " + e.getMessage());
            e.printStackTrace(System.err);
        } finally {
            // Solo se da de baja el registro creado por esta ejecución.
            if (!eliminado && cliente.getId_cliente() > 0) {
                try {
                    T pendiente = bl.findById(cliente.getId_cliente());
                    if (pendiente != null && pendiente.isActivo()) {
                        bl.delete(cliente.getId_cliente());
                    }
                } catch (Exception e) {
                    fallos++;
                    System.err.println("Revisar registro de prueba ID " + cliente.getId_cliente()
                            + ": no se pudo completar su baja: " + e.getMessage());
                }
            }
        }
    }

    private static void comparar(Cliente esperado, Cliente actual) {
        exigir(actual != null, "FindById encuentra el registro");
        exigir(esperado.getId_cliente() == actual.getId_cliente(), "ID persistido");
        exigir(Objects.equals(esperado.getNombre(), actual.getNombre()), "Nombre persistido");
        exigir(Objects.equals(esperado.getDireccion(), actual.getDireccion()), "Dirección persistida");
        exigir(Objects.equals(esperado.getEmailContacto(), actual.getEmailContacto()), "Correo persistido");
        exigir(Objects.equals(esperado.getTelefono(), actual.getTelefono()), "Teléfono persistido");
        exigir(esperado.isActivo() == actual.isActivo(), "Estado persistido");
        if (esperado instanceof ClinicaHospital e && actual instanceof ClinicaHospital a) {
            exigir(Objects.equals(e.getRuc(), a.getRuc()), "RUC persistido");
            exigir(e.isTieneConsignacion() == a.isTieneConsignacion(), "Consignación persistida");
            exigir(Objects.equals(e.getPeriodoCredito(), a.getPeriodoCredito()), "Crédito persistido");
        } else if (esperado instanceof PacienteParticular e && actual instanceof PacienteParticular a) {
            exigir(Objects.equals(e.getDni(), a.getDni()), "DNI persistido");
            exigir(e.isPagoConfirmado() == a.isPagoConfirmado(), "Pago persistido");
        } else {
            throw new IllegalStateException("Tipo de cliente inesperado");
        }
    }

    private static ClinicaHospital crearClinica() {
        ClinicaHospital c = new ClinicaHospital();
        datosComunes(c, "Clinica prueba BL");
        c.setRuc("20" + ThreadLocalRandom.current().nextLong(100_000_000L, 1_000_000_000L));
        c.setTieneConsignacion(false);
        c.setPeriodoCredito(null);
        return c;
    }

    private static PacienteParticular crearPaciente() {
        PacienteParticular p = new PacienteParticular();
        datosComunes(p, "Paciente prueba BL");
        p.setDni(Integer.toString(ThreadLocalRandom.current().nextInt(10_000_000, 100_000_000)));
        // RF08 aplica a solicitudes de cirugía; registrar un paciente no exige pago previo.
        p.setPagoConfirmado(false);
        return p;
    }

    private static void datosComunes(Cliente c, String nombre) {
        c.setNombre(nombre);
        c.setDireccion("Direccion de prueba");
        c.setEmailContacto("prueba@example.com");
        c.setTelefono("999888777");
        c.setActivo(true);
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new IllegalStateException(mensaje);
        }
        correctas++;
        System.out.println("[OK] " + mensaje);
    }

    private static void rechaza(String caso, String mensaje, Runnable operacion) {
        try {
            operacion.run();
            fallos++;
            System.err.println("[FALLO] " + caso + ": se aceptó un dato inválido");
        } catch (BLException e) {
            if (e.getCause() == null && e.getMessage().startsWith(mensaje)) {
                correctas++;
                System.out.println("[OK] " + caso);
            } else {
                fallos++;
                System.err.println("[FALLO] " + caso + ": " + e);
            }
        } catch (Exception e) {
            fallos++;
            System.err.println("[FALLO] " + caso + ": excepción inesperada: " + e);
        }
    }
}
