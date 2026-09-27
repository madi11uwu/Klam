package pe.edu.pucp.app.prueba2;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.BandejaInstrumentalBL;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.CotizacionBL;
import pe.edu.pucp.klam.bl.EquipoBL;
import pe.edu.pucp.klam.bl.impl.BandejaInstrumentalBLImpl;
import pe.edu.pucp.klam.bl.impl.CirugiaBLImpl;
import pe.edu.pucp.klam.bl.impl.CotizacionBLImpl;
import pe.edu.pucp.klam.bl.impl.EquipoBLImpl;
import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.CategoriaEquipo;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Cotizacion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class PruebaCirugiasCRUD {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final long RUN = System.currentTimeMillis() % 1_000_000L;

    // Clinica San Felipe, cargada por DML_KLAM.sql
    private static final int ID_CLINICA_DML = 2;
    // Cirugia FINALIZADA y cotizacion ACEPTADA, cargadas por DML_KLAM.sql
    private static final int ID_CIRUGIA_FINALIZADA_DML = 1;
    private static final int ID_COTIZACION_ACEPTADA_DML = 1;

    private static final CirugiaBL cirugiaBL = new CirugiaBLImpl();
    private static final CotizacionBL cotizacionBL = new CotizacionBLImpl();
    private static final EquipoBL equipoBL = new EquipoBLImpl();
    private static final BandejaInstrumentalBL bandejaBL = new BandejaInstrumentalBLImpl();

    public static void main(String[] args) {
        ejecutar();
    }

    public static void ejecutar() {
        try {
            probarCirugias();
            probarCotizaciones();
            probarReglasDeNegocio();
        } catch (BLException ex) {
            System.out.println("La prueba se detuvo por un error: " + ex.getMessage());
        }
    }

    private static void probarCirugias() throws BLException {
        titulo("CIRUGIA");

        Equipo equipo = new Equipo();
        equipo.setNombre("Navegador " + RUN);
        equipo.setCategoria(CategoriaEquipo.NAVEGADOR);
        equipo.setEspecificaciones(Map.of("marca", "Medtronic"));
        equipo.setDisponible(true);
        equipoBL.insert(equipo);

        BandejaInstrumental bandeja = new BandejaInstrumental();
        bandeja.setTipo("Bandeja craneotomía " + RUN);
        bandeja.setEsterilizado(true);
        bandejaBL.insert(bandeja);

        Cirugia cirugia = new Cirugia();
        cirugia.setFechaHoraInicio(LocalDateTime.of(2026, 10, 15, 8, 0));
        cirugia.setFechaHoraFin(LocalDateTime.of(2026, 10, 15, 11, 30));
        cirugia.setTipoProcedimiento("Craneotomía " + RUN);
        cirugia.setDoctorNombre("Dr. Prueba");
        cirugia.setEstado(EstadoCirugia.PROGRAMADA);
        cirugia.setEquipo(equipo);
        cirugia.setBandejaInstrumental(bandeja);
        cirugia.setCliente(clinicaDML());
        cirugia.setActivo(true);
        cirugiaBL.insert(cirugia);
        System.out.println("Insertada:   " + describir(cirugia));

        cirugia = cirugiaBL.findById(cirugia.getId_cirugia());
        System.out.println("Recuperada:  " + describir(cirugia));

        cirugia.setFechaHoraInicio(LocalDateTime.of(2026, 10, 16, 9, 0));
        cirugia.setFechaHoraFin(LocalDateTime.of(2026, 10, 16, 13, 0));
        cirugia.setDoctorNombre("Dra. Prueba Editada");
        cirugia.setEstado(EstadoCirugia.EN_PROCESO);
        cirugiaBL.update(cirugia);
        System.out.println("Actualizada: " + describir(cirugiaBL.findById(cirugia.getId_cirugia())));

        System.out.println("En proceso:  " + cirugiaBL.findByEstado(EstadoCirugia.EN_PROCESO).size()
                + " cirugía(s)");
        System.out.println("16/10/2026:  " + cirugiaBL.findByRangoFechas(
                LocalDateTime.of(2026, 10, 16, 0, 0),
                LocalDateTime.of(2026, 10, 16, 23, 59)).size() + " cirugía(s)");

        cirugiaBL.cancelar(cirugia.getId_cirugia(), "Paciente con fiebre " + RUN);
        System.out.println("Cancelada:   " + describir(cirugiaBL.findById(cirugia.getId_cirugia())));

        cirugiaBL.delete(cirugia.getId_cirugia());
        System.out.println("Eliminada:   id " + cirugia.getId_cirugia()
                + " (activo=" + cirugiaBL.findById(cirugia.getId_cirugia()).isActivo() + ")");

        listarCirugias(cirugiaBL.findAll());

        equipoBL.delete(equipo.getId_equipo());
        bandejaBL.delete(bandeja.getId_bandeja());
    }

    private static void probarCotizaciones() throws BLException {
        titulo("COTIZACION");

        Cirugia cirugia = new Cirugia();
        cirugia.setFechaHoraInicio(LocalDateTime.of(2026, 11, 3, 10, 0));
        cirugia.setTipoProcedimiento("Clipaje de aneurisma " + RUN);
        cirugia.setDoctorNombre("Dr. Cotizado");
        cirugia.setEstado(EstadoCirugia.PROGRAMADA);
        cirugia.setCliente(clinicaDML());
        cirugia.setActivo(true);
        cirugiaBL.insert(cirugia);

        Cotizacion cotizacion = new Cotizacion(cirugia, 9500.00, LocalDateTime.of(2026, 10, 20, 12, 0));
        cotizacionBL.insert(cotizacion);
        System.out.println("Insertada:   " + describir(cotizacion));

        cotizacion = cotizacionBL.findById(cotizacion.getIdCotizacion());
        System.out.println("Recuperada:  " + describir(cotizacion));

        cotizacion.setPrecioPactado(9850.50);
        cotizacionBL.update(cotizacion);
        System.out.println("Actualizada: " + describir(cotizacionBL.findById(cotizacion.getIdCotizacion())));

        System.out.println("De la cirugía " + cirugia.getId_cirugia() + ": "
                + cotizacionBL.findByCirugia(cirugia.getId_cirugia()).size() + " cotización(es)");

        cotizacionBL.aceptar(cotizacion.getIdCotizacion());
        System.out.println("Aceptada:    " + describir(cotizacionBL.findById(cotizacion.getIdCotizacion())));

        cotizacionBL.delete(cotizacion.getIdCotizacion());
        System.out.println("Eliminada:   id " + cotizacion.getIdCotizacion()
                + " (activo=" + cotizacionBL.findById(cotizacion.getIdCotizacion()).isActivo() + ")");

        listarCotizaciones(cotizacionBL.findAll());

        cirugiaBL.delete(cirugia.getId_cirugia());
    }

    private static void probarReglasDeNegocio() {
        titulo("REGLAS DE NEGOCIO");

        try {
            Cirugia cirugia = cirugiaDemo();
            cirugia.setFechaHoraInicio(LocalDateTime.of(2026, 10, 15, 12, 0));
            cirugia.setFechaHoraFin(LocalDateTime.of(2026, 10, 15, 8, 0));
            cirugiaBL.insert(cirugia);
            System.out.println("Cirugía con inicio posterior al fin: NO se rechazó (falla la regla)");
        } catch (BLException ex) {
            System.out.println("Cirugía con inicio posterior al fin -> Rechazado: " + ex.getMessage());
        }

        try {
            Cirugia cirugia = cirugiaDemo();
            cirugia.setCliente(null);
            cirugiaBL.insert(cirugia);
            System.out.println("Cirugía sin cliente: NO se rechazó (falla la regla)");
        } catch (BLException ex) {
            System.out.println("Cirugía sin cliente -> Rechazado: " + ex.getMessage());
        }

        try {
            cirugiaBL.cancelar(ID_CIRUGIA_FINALIZADA_DML, " ");
            System.out.println("Cancelar sin motivo: NO se rechazó (falla la regla)");
        } catch (BLException ex) {
            System.out.println("Cancelar sin motivo -> Rechazado: " + ex.getMessage());
        }

        try {
            cirugiaBL.cancelar(ID_CIRUGIA_FINALIZADA_DML, "Motivo de prueba");
            System.out.println("Cancelar cirugía FINALIZADA: NO se rechazó (falla la regla)");
        } catch (BLException ex) {
            System.out.println("Cancelar cirugía FINALIZADA -> Rechazado: " + ex.getMessage());
        }

        try {
            Cotizacion cotizacion = new Cotizacion();
            cotizacion.setFechaEmision(LocalDateTime.now());
            cotizacionBL.insert(cotizacion);
            System.out.println("Cotización sin cirugía: NO se rechazó (falla la regla)");
        } catch (BLException ex) {
            System.out.println("Cotización sin cirugía -> Rechazado: " + ex.getMessage());
        }

        try {
            Cotizacion cotizacion = new Cotizacion();
            cotizacion.setPrecioPactado(-100.00);
            System.out.println("Cotización con precio negativo: NO se rechazó (falla la regla)");
        } catch (IllegalArgumentException ex) {
            System.out.println("Cotización con precio negativo -> Rechazado: " + ex.getMessage());
        }

        try {
            cotizacionBL.aceptar(ID_COTIZACION_ACEPTADA_DML);
            System.out.println("Aceptar cotización ya ACEPTADA: NO se rechazó (falla la regla)");
        } catch (BLException ex) {
            System.out.println("Aceptar cotización ya ACEPTADA -> Rechazado: " + ex.getMessage());
        }
    }

    private static ClinicaHospital clinicaDML() {
        ClinicaHospital clinica = new ClinicaHospital();
        clinica.setId_cliente(ID_CLINICA_DML);
        return clinica;
    }

    private static Cirugia cirugiaDemo() {
        Cirugia cirugia = new Cirugia();
        cirugia.setFechaHoraInicio(LocalDateTime.of(2026, 10, 15, 8, 0));
        cirugia.setTipoProcedimiento("Cirugía temporal " + RUN);
        cirugia.setEstado(EstadoCirugia.PROGRAMADA);
        cirugia.setCliente(clinicaDML());
        cirugia.setActivo(true);
        return cirugia;
    }

    private static void listarCirugias(List<Cirugia> cirugias) {
        System.out.println("Listado (" + cirugias.size() + "):");
        for (Cirugia cirugia : cirugias) {
            System.out.println("  " + describir(cirugia));
        }
    }

    private static void listarCotizaciones(List<Cotizacion> cotizaciones) {
        System.out.println("Listado (" + cotizaciones.size() + "):");
        for (Cotizacion cotizacion : cotizaciones) {
            System.out.println("  " + describir(cotizacion));
        }
    }

    private static void titulo(String nombre) {
        System.out.println();
        System.out.println("=== " + nombre + " ===");
    }

    private static String describir(Cirugia cirugia) {
        String cliente = "(sin cliente)";
        if (cirugia.getCliente() != null) {
            cliente = cirugia.getCliente().getNombre() != null
                    ? cirugia.getCliente().getNombre()
                    : "id " + cirugia.getCliente().getId_cliente();
        }
        String equipo = cirugia.getEquipo() != null ? cirugia.getEquipo().getNombre() : "(sin equipo)";
        String bandeja = cirugia.getBandejaInstrumental() != null
                ? cirugia.getBandejaInstrumental().getTipo()
                : "(sin bandeja)";
        String fin = cirugia.getFechaHoraFin() != null
                ? FORMATO_FECHA.format(cirugia.getFechaHoraFin())
                : "--";
        return String.format("[%d] %s  %s -> %s  %s  doctor=%s  cliente=%s  equipo=%s  bandeja=%s%s  activo=%s",
                cirugia.getId_cirugia(),
                cirugia.getTipoProcedimiento(),
                FORMATO_FECHA.format(cirugia.getFechaHoraInicio()),
                fin,
                cirugia.getEstado(),
                cirugia.getDoctorNombre(),
                cliente,
                equipo,
                bandeja,
                cirugia.getMotivoCancelacion() != null ? "  motivo=" + cirugia.getMotivoCancelacion() : "",
                cirugia.isActivo());
    }

    private static String describir(Cotizacion cotizacion) {
        String cirugia = cotizacion.getCirugia() != null
                ? cotizacion.getCirugia().getTipoProcedimiento()
                : "(sin cirugía)";
        return String.format("[%d] cirugía=%s  S/ %.2f  %s  emitida=%s  activo=%s",
                cotizacion.getIdCotizacion(),
                cirugia,
                cotizacion.getPrecioPactado(),
                cotizacion.getEstado(),
                FORMATO_FECHA.format(cotizacion.getFechaEmision()),
                cotizacion.isActivo());
    }
}
