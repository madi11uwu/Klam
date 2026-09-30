package pe.edu.pucp.klam.app.prueba2;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.BandejaInstrumentalBL;
import pe.edu.pucp.klam.bl.ConsumibleBL;
import pe.edu.pucp.klam.bl.EquipoBL;
import pe.edu.pucp.klam.bl.impl.BandejaInstrumentalBLImpl;
import pe.edu.pucp.klam.bl.impl.ConsumibleBLImpl;
import pe.edu.pucp.klam.bl.impl.EquipoBLImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.CategoriaEquipo;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;

import java.util.Map;

/**
 * Prueba del CRUD del modulo de Inventario (Rol 2) contra la base de datos:
 * Equipo, Consumible y BandejaInstrumental.
 * Usa solo la capa de negocio (BL), nunca los DAO directamente.
 */
public class PruebaInventarioCRUD {

    private static final EquipoBL equipoBL = new EquipoBLImpl();
    private static final ConsumibleBL consumibleBL = new ConsumibleBLImpl();
    private static final BandejaInstrumentalBL bandejaBL = new BandejaInstrumentalBLImpl();

    public static void ejecutar() {
        System.out.println("==================================================");
        System.out.println("        CRUD INVENTARIO (ROL 2)");
        System.out.println("==================================================");

        probar("EQUIPO", PruebaInventarioCRUD::probarEquipo);
        probar("CONSUMIBLE", PruebaInventarioCRUD::probarConsumible);
        probar("BANDEJA INSTRUMENTAL", PruebaInventarioCRUD::probarBandeja);
    }

    // -----------------------------------------------------------------
    // EQUIPO (cabecera + especificaciones)
    // -----------------------------------------------------------------
    private static void probarEquipo() {
        Equipo equipo = new Equipo();
        equipo.setNombre("Craneotomo de prueba");
        equipo.setCategoria(CategoriaEquipo.CRANEOTOMO);
        equipo.setDisponible(true);
        equipo.setEspecificaciones(Map.of("marca", "Medtronic", "voltaje", "220V"));

        equipoBL.insert(equipo);
        int id = equipo.getIdEquipo();
        System.out.println("[INSERT] id " + id);

        Equipo leido = equipoBL.findById(id);
        System.out.println("[FIND BY ID] " + describir(leido));

        leido.setNombre("Craneotomo de prueba (editado)");
        leido.setDisponible(false);
        Map<String, Object> esp = leido.getEspecificaciones();
        esp.put("voltaje", "110V");
        esp.put("peso", "2.5kg");
        leido.setEspecificaciones(esp);
        equipoBL.update(leido);
        System.out.println("[UPDATE] " + describir(equipoBL.findById(id)));

        System.out.println("[FIND ALL] activos: " + equipoBL.findAll().size());

        equipoBL.delete(id);
        System.out.println("[DELETE] activo = " + equipoBL.findById(id).isActivo());

        esperarError("equipo sin nombre", () -> {
            Equipo invalido = new Equipo();
            invalido.setNombre("");
            invalido.setCategoria(CategoriaEquipo.OTRO);
            equipoBL.insert(invalido);
        });
    }

    // -----------------------------------------------------------------
    // CONSUMIBLE (una sola tabla)
    // -----------------------------------------------------------------
    private static void probarConsumible() {
        Consumible consumible = new Consumible();
        consumible.setNombreComercial("Gasa de prueba 10x10");
        consumible.setMarca("Medline");
        consumible.setMedida("10x10 cm");

        consumibleBL.insert(consumible);
        int id = consumible.getIdConsumible();
        System.out.println("[INSERT] id " + id);

        Consumible leido = consumibleBL.findById(id);
        System.out.println("[FIND BY ID] " + describir(leido));

        leido.setNombreComercial("Gasa de prueba 20x20");
        leido.setMedida("20x20 cm");
        consumibleBL.update(leido);
        System.out.println("[UPDATE] " + describir(consumibleBL.findById(id)));

        System.out.println("[FIND BY NOMBRE 'gasa'] encontrados: "
                + consumibleBL.findByNombre("gasa").size());
        System.out.println("[FIND ALL] activos: " + consumibleBL.findAll().size());

        consumibleBL.delete(id);
        System.out.println("[DELETE] activo = " + consumibleBL.findById(id).isActivo());

        esperarError("consumible sin nombre", () -> {
            Consumible invalido = new Consumible();
            invalido.setNombreComercial(" ");
            consumibleBL.insert(invalido);
        });
    }

    // -----------------------------------------------------------------
    // BANDEJA INSTRUMENTAL (cabecera + consumibles despachados/consumidos)
    // -----------------------------------------------------------------
    private static void probarBandeja() {
        // Consumibles que iran en la bandeja
        Consumible sutura = new Consumible();
        sutura.setNombreComercial("Sutura de prueba");
        sutura.setMarca("Ethicon");
        sutura.setMedida("4-0");
        consumibleBL.insert(sutura);

        Consumible clip = new Consumible();
        clip.setNombreComercial("Clip de prueba");
        clip.setMarca("Aesculap");
        clip.setMedida("Mediano");
        consumibleBL.insert(clip);

        BandejaInstrumental bandeja = new BandejaInstrumental();
        bandeja.setTipo("Bandeja de prueba - craneotomia");
        bandeja.setEsterilizado(true);
        bandeja.agregarConsumible(sutura, 10);
        bandeja.agregarConsumible(clip, 4);

        bandejaBL.insert(bandeja);
        int id = bandeja.getIdBandeja();
        System.out.println("[INSERT] id " + id);

        BandejaInstrumental leida = bandejaBL.findById(id);
        System.out.println("[FIND BY ID] " + describir(leida));

        // Despues de la cirugia: se registra lo que realmente se uso
        leida.registrarConsumo(sutura, 6);
        leida.registrarConsumo(clip, 2);
        bandejaBL.update(leida);
        BandejaInstrumental actualizada = bandejaBL.findById(id);
        System.out.println("[UPDATE] " + describir(actualizada));
        System.out.println("         sobrante sutura = " + actualizada.obtenerDiferencia(sutura));

        System.out.println("[FIND ALL] activas: " + bandejaBL.findAll().size());

        bandejaBL.delete(id);
        System.out.println("[DELETE] activo = " + bandejaBL.findById(id).isActivo());

        esperarError("consumir mas de lo despachado", () -> {
            BandejaInstrumental b = bandejaBL.findById(id);
            b.registrarConsumo(sutura, 99);
            bandejaBL.update(b);
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
            System.out.println("[VALIDACION] FALLO: se permitio " + caso);
        } catch (BLException e) {
            System.out.println("[VALIDACION] OK (" + caso + "): " + e.getMessage());
        }
    }

    private static String describir(Equipo e) {
        return "nombre=" + e.getNombre() + ", categoria=" + e.getCategoria()
                + ", disponible=" + e.isDisponible() + ", activo=" + e.isActivo()
                + ", especificaciones=" + e.getEspecificaciones();
    }

    private static String describir(Consumible c) {
        return "nombre=" + c.getNombreComercial() + ", marca=" + c.getMarca()
                + ", medida=" + c.getMedida() + ", activo=" + c.isActivo();
    }

    private static String describir(BandejaInstrumental b) {
        StringBuilder sb = new StringBuilder("tipo=" + b.getTipo()
                + ", esterilizado=" + b.isEsterilizado() + ", activo=" + b.isActivo());
        for (Map.Entry<Consumible, Integer> e : b.getConsumibles().entrySet()) {
            sb.append("\n         - ").append(e.getKey().getNombreComercial())
              .append(": despachado=").append(e.getValue())
              .append(", consumido=").append(b.obtenerCantidadConsumida(e.getKey()));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        ejecutar();
    }
}
