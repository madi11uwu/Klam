package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.ClinicaHospitalBL;
import pe.edu.pucp.klam.bl.PacienteParticularBL;
import pe.edu.pucp.klam.bl.impl.ClinicaHospitalBLImpl;
import pe.edu.pucp.klam.bl.impl.PacienteParticularBLImpl;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.bl.UsuarioPlataformaBL;
import pe.edu.pucp.klam.bl.impl.UsuarioPlataformaBLImpl;

@Path("/clientes")
@Produces("application/json")
@Consumes("application/json")
public class ClientesResource {
    private final PacienteParticularBL pacientes = new PacienteParticularBLImpl();
    private final ClinicaHospitalBL clinicas = new ClinicaHospitalBLImpl();
    @Context private HttpHeaders headers;

    @GET @Path("/particulares")
    public Response buscarParticular(@QueryParam("dni") String dni) {
        Response acceso = acceso(); if (acceso != null) return acceso;
        try {
            PacienteParticular p = pacientes.findByDNI(dni);
            return p == null ? Response.ok(List.of()).build() : Response.ok(List.of(particular(p))).build();
        } catch (BLException e) { return fallo(e); }
    }

    @POST @Path("/particulares")
    public Response registrarParticular(PacienteParticular datos) {
        Response acceso = acceso(); if (acceso != null) return acceso;
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
        Response acceso = acceso(); if (acceso != null) return acceso;
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
        Response acceso = acceso(); if (acceso != null) return acceso;
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

    private Response acceso() {
        SesionesRest.Sesion s = SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (s == null) return error(Response.Status.UNAUTHORIZED, "Sesión inválida o expirada");
        return "VENDEDOR".equals(s.rol()) ? null : error(Response.Status.FORBIDDEN, "No tiene permisos de vendedor");
    }
    private Response fallo(BLException e) {
        return error(e.getCause() == null ? Response.Status.BAD_REQUEST : Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
    }
    private Response error(Response.Status status, String message) {
        return Response.status(status).entity(Map.of("error", message)).build();
    }
    private Map<String,Object> particular(PacienteParticular p) {
        return Map.of("idCliente",p.getIdCliente(),"nombre",p.getNombre(),"dni",p.getDni());
    }
    private Map<String,Object> clinica(ClinicaHospital c) {
        return Map.of("idCliente",c.getIdCliente(),"nombre",c.getNombre(),"ruc",c.getRuc());
    }
    /** Habilita acceso sin volver a registrar al cliente comercial. */
    @POST @Path("/{tipo}/{id}/cuenta")
    public Response habilitarCuenta(@PathParam("tipo") String tipo, @PathParam("id") int id,
                                   Map<String, String> datos) {
        SesionesRest.Sesion sesion = SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (sesion == null) return error(Response.Status.UNAUTHORIZED, "Sesión inválida o expirada");
        if (!"ADMINISTRADOR".equals(sesion.rol()))
            return error(Response.Status.FORBIDDEN, "Solo el administrador puede habilitar cuentas");
        if (datos == null) return error(Response.Status.BAD_REQUEST, "Datos de cuenta obligatorios");
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
