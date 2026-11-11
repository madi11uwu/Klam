package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.ClinicaHospitalBL;
import pe.edu.pucp.klam.bl.PacienteParticularBL;
import pe.edu.pucp.klam.bl.impl.ClinicaHospitalBLImpl;
import pe.edu.pucp.klam.bl.impl.PacienteParticularBLImpl;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.rest.mapper.ClienteMapper;
import pe.edu.pucp.klam.bl.UsuarioPlataformaBL;
import pe.edu.pucp.klam.bl.impl.UsuarioPlataformaBLImpl;

@Path("/clientes")
@Produces("application/json")
@Consumes("application/json")
public class ClientesResource {
    private final PacienteParticularBL pacientes = new PacienteParticularBLImpl();
    private final ClinicaHospitalBL clinicas = new ClinicaHospitalBLImpl();

    @GET
    public Response listar(@QueryParam("tipoCliente") String tipoCliente) {
        String tipo = tipoCliente == null || tipoCliente.isBlank()
                ? null : tipoCliente.trim().toUpperCase(Locale.ROOT);
        if (tipo != null && !ClienteMapper.TIPO_CLINICA.equals(tipo)
                && !ClienteMapper.TIPO_PARTICULAR.equals(tipo))
            return error(Response.Status.BAD_REQUEST, "Tipo de cliente no válido: " + tipoCliente);
        try {
            Comparator<Cliente> porNombre = Comparator.comparing(
                    c -> c.getNombre() == null ? "" : c.getNombre(), String.CASE_INSENSITIVE_ORDER);
            List<Cliente> clientes = new ArrayList<>();
            if (tipo == null || ClienteMapper.TIPO_CLINICA.equals(tipo)) {
                List<Cliente> lista = new ArrayList<>(clinicas.findAll());
                lista.sort(porNombre);
                clientes.addAll(lista);
            }
            if (tipo == null || ClienteMapper.TIPO_PARTICULAR.equals(tipo)) {
                List<Cliente> lista = new ArrayList<>(pacientes.findAll());
                lista.sort(porNombre);
                clientes.addAll(lista);
            }
            return Response.ok(ClienteMapper.toDTOs(clientes)).build();
        } catch (BLException e) { return fallo(e); }
    }

    @GET @Path("/particulares")
    public Response buscarParticular(@QueryParam("dni") String dni) {
        try {
            PacienteParticular p = pacientes.findByDNI(dni);
            return p == null ? Response.ok(List.of()).build() : Response.ok(List.of(particular(p))).build();
        } catch (BLException e) { return fallo(e); }
    }

    @POST @Path("/particulares")
    public Response registrarParticular(PacienteParticular datos) {
        if (datos == null) return error(Response.Status.BAD_REQUEST, "Datos del paciente obligatorios");
        try {
            if (pacientes.findByDNI(datos.getDni()) != null) return error(Response.Status.CONFLICT, "Ya existe el DNI");
            PacienteParticular p = new PacienteParticular();
            p.setNombre(datos.getNombre()); p.setDireccion(datos.getDireccion());
            p.setEmailContacto(datos.getEmailContacto()); p.setTelefono(datos.getTelefono());
            p.setDni(datos.getDni()); p.setPagoConfirmado(false);
            pacientes.insert(p);
            return Response.status(Response.Status.CREATED).entity(particular(p)).build();
        } catch (BLException e) { return fallo(e); }
    }

    @GET @Path("/clinicas")
    public Response buscarClinicas(@QueryParam("ruc") String ruc, @QueryParam("nombre") String nombre) {
        if ((ruc == null) == (nombre == null)) return error(Response.Status.BAD_REQUEST, "Indique RUC o nombre");
        try {
            if (ruc != null) {
                ClinicaHospital c = clinicas.findByRUC(ruc);
                return c == null ? Response.ok(List.of()).build() : Response.ok(List.of(clinica(c))).build();
            }
            return Response.ok(clinicas.findByNombre(nombre).stream().map(this::clinica).toList()).build();
        } catch (BLException e) { return fallo(e); }
    }

    @POST @Path("/clinicas")
    public Response registrarClinica(ClinicaHospital datos) {
        if (datos == null) return error(Response.Status.BAD_REQUEST, "Datos de la clínica obligatorios");
        try {
            if (clinicas.findByRUC(datos.getRuc()) != null) return error(Response.Status.CONFLICT, "Ya existe el RUC");
            ClinicaHospital c = new ClinicaHospital();
            c.setNombre(datos.getNombre()); c.setDireccion(datos.getDireccion());
            c.setEmailContacto(datos.getEmailContacto()); c.setTelefono(datos.getTelefono());
            c.setRuc(datos.getRuc()); c.setTieneConsignacion(datos.isTieneConsignacion());
            c.setPeriodoCredito(datos.getPeriodoCredito());
            clinicas.insert(c);
            return Response.status(Response.Status.CREATED).entity(clinica(c)).build();
        } catch (BLException e) { return fallo(e); }
    }

    private Response fallo(BLException e) {
        return error(e.getCause() == null ? Response.Status.BAD_REQUEST : Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
    }
    private Response error(Response.Status status, String message) {
        return Response.status(status).entity(Map.of("error", message)).build();
    }
    private Map<String,Object> particular(PacienteParticular p) {
        // HashMap: Map.of no acepta valores nulos (un paciente sin DNI tumbaba la respuesta)
        Map<String,Object> datos = new HashMap<>();
        datos.put("idCliente", p.getIdCliente());
        datos.put("nombre", p.getNombre());
        datos.put("dni", p.getDni());
        return datos;
    }
    private Map<String,Object> clinica(ClinicaHospital c) {
        Map<String,Object> datos = new HashMap<>();
        datos.put("idCliente", c.getIdCliente());
        datos.put("nombre", c.getNombre());
        datos.put("ruc", c.getRuc());
        return datos;
    }
    /** Habilita acceso sin volver a registrar al cliente comercial. */
    @POST @Path("/{tipo}/{id}/cuenta")
    public Response habilitarCuenta(@PathParam("tipo") String tipo, @PathParam("id") int id,
                                   Map<String, String> datos) {
        if (datos == null || datos.get("userName") == null)
            return error(Response.Status.BAD_REQUEST, "Datos de cuenta obligatorios");
        try {
            UsuarioPlataformaBL usuarios = new UsuarioPlataformaBLImpl();
            int idCuenta = usuarios.habilitarCuentaCliente(tipo, id,
                    datos.get("userName"), datos.get("password"));
            return Response.status(Response.Status.CREATED).entity(Map.of(
                    "id", idCuenta, "userName", datos.get("userName").trim(),
                    "tipoCliente", tipo, "idCliente", id)).build();
        } catch (BLException ex) { return fallo(ex); }
    }
}
