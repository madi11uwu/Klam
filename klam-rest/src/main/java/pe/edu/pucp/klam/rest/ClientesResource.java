package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.ClinicaHospitalBL;
import pe.edu.pucp.klam.bl.PacienteParticularBL;
import pe.edu.pucp.klam.bl.impl.ClinicaHospitalBLImpl;
import pe.edu.pucp.klam.bl.impl.PacienteParticularBLImpl;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.rest.mapper.ClienteMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Catálogo de clientes para los filtros y selectores (RF17, RF05, RF09).
 *
 * GET /api/v1/clientes                              -> clínicas/hospitales y pacientes particulares activos
 * GET /api/v1/clientes?tipoCliente=CLINICA_HOSPITAL -> solo clínicas y hospitales
 * GET /api/v1/clientes?tipoCliente=PACIENTE_PARTICULAR
 *
 * Los ids de clínicas y pacientes viven en tablas distintas: un id solo identifica al cliente
 * junto con su tipoCliente. Cada elemento lo incluye.
 */
@Path("/clientes")
@Produces("application/json")
public class ClientesResource {
    private final ClinicaHospitalBL clinicaBL;
    private final PacienteParticularBL pacienteBL;

    public ClientesResource() {
        this(new ClinicaHospitalBLImpl(), new PacienteParticularBLImpl());
    }

    // Visibilidad de paquete: el contenedor REST solo usa el constructor público sin argumentos.
    ClientesResource(ClinicaHospitalBL clinicaBL, PacienteParticularBL pacienteBL) {
        this.clinicaBL = clinicaBL;
        this.pacienteBL = pacienteBL;
    }

    @GET
    public Response listar(@QueryParam("tipoCliente") String tipoCliente) {
        try {
            String tipo = tipoCliente == null || tipoCliente.isBlank()
                    ? null : tipoCliente.trim().toUpperCase(Locale.ROOT);
            if (tipo != null && !ClienteMapper.TIPO_CLINICA.equals(tipo)
                    && !ClienteMapper.TIPO_PARTICULAR.equals(tipo)) {
                return error(Response.Status.BAD_REQUEST, "Tipo de cliente no válido: " + tipoCliente);
            }

            Comparator<Cliente> porNombre = Comparator.comparing(
                    (Cliente c) -> c.getNombre() == null ? "" : c.getNombre(), String.CASE_INSENSITIVE_ORDER);
            List<Cliente> clientes = new ArrayList<>();
            if (tipo == null || ClienteMapper.TIPO_CLINICA.equals(tipo)) {
                List<Cliente> clinicas = new ArrayList<>(clinicaBL.findAll());
                clinicas.sort(porNombre);
                clientes.addAll(clinicas);
            }
            if (tipo == null || ClienteMapper.TIPO_PARTICULAR.equals(tipo)) {
                List<Cliente> pacientes = new ArrayList<>(pacienteBL.findAll());
                pacientes.sort(porNombre);
                clientes.addAll(pacientes);
            }
            return Response.ok(ClienteMapper.toDTOs(clientes)).build();
        } catch (BLException ex) {
            return error(ex.getCause() == null ? Response.Status.BAD_REQUEST
                    : Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private static Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(Map.of("error", mensaje)).build();
    }
}
