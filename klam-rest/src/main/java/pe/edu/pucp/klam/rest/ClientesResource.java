package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.ClinicaHospitalBL;
import pe.edu.pucp.klam.bl.PacienteParticularBL;
import pe.edu.pucp.klam.bl.impl.ClinicaHospitalBLImpl;
import pe.edu.pucp.klam.bl.impl.PacienteParticularBLImpl;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.rest.dto.ClienteRegistroDTO;
import pe.edu.pucp.klam.rest.dto.ClienteResumenDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * GET /clientes?tipo=CLINICA_HOSPITAL|PACIENTE_PARTICULAR (sin tipo = ambos).
 * Lista clientes activos para los selectores de Blazor (solicitante, clínica destino, filtros).
 * POST /clientes: alta de cliente con usuario y contraseña (la BL exige ambos, RF01).
 * GET/PUT /clientes/{tipo}/{id}: detalle y modificación. Alta, detalle y modificación: solo ADMINISTRADOR.
 * Los integrantes 1 y 2 pueden agregar aquí sus consultas por cliente.
 */
@Path("/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClientesResource {

    private final ClinicaHospitalBL clinicaBL = new ClinicaHospitalBLImpl();
    private final PacienteParticularBL pacienteBL = new PacienteParticularBLImpl();

    @GET
    @RequiereRol
    public Response listar(@QueryParam("tipo") String tipo) {
        boolean todos = tipo == null || tipo.isBlank();
        if (!todos && !CirugiaBL.TIPO_CLIENTE_CLINICA.equals(tipo) && !CirugiaBL.TIPO_CLIENTE_PARTICULAR.equals(tipo)) {
            return Errores.error(Response.Status.BAD_REQUEST, "Tipo de cliente inválido: use "
                    + CirugiaBL.TIPO_CLIENTE_CLINICA + " o " + CirugiaBL.TIPO_CLIENTE_PARTICULAR);
        }
        try {
            List<ClienteResumenDTO> lista = new ArrayList<>();
            if (todos || CirugiaBL.TIPO_CLIENTE_CLINICA.equals(tipo)) {
                for (var c : clinicaBL.findAll()) {
                    ClienteResumenDTO d = new ClienteResumenDTO();
                    d.setTipoCliente(CirugiaBL.TIPO_CLIENTE_CLINICA);
                    d.setIdCliente(c.getIdCliente());
                    d.setNombre(c.getNombre());
                    d.setEmailContacto(c.getEmailContacto());
                    d.setTelefono(c.getTelefono());
                    d.setDocumento("RUC " + c.getRuc());
                    d.setActivo(c.isActivo());
                    lista.add(d);
                }
            }
            if (todos || CirugiaBL.TIPO_CLIENTE_PARTICULAR.equals(tipo)) {
                for (var c : pacienteBL.findAll()) {
                    ClienteResumenDTO d = new ClienteResumenDTO();
                    d.setTipoCliente(CirugiaBL.TIPO_CLIENTE_PARTICULAR);
                    d.setIdCliente(c.getIdCliente());
                    d.setNombre(c.getNombre());
                    d.setEmailContacto(c.getEmailContacto());
                    d.setTelefono(c.getTelefono());
                    d.setDocumento("DNI " + c.getDni());
                    d.setDni(c.getDni());
                    d.setActivo(c.isActivo());
                    lista.add(d);
                }
            }
            return Response.ok(lista).build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    @POST
    @RequiereRol("ADMINISTRADOR")
    public Response registrar(ClienteRegistroDTO dto) {
        if (dto == null) {
            return Errores.error(Response.Status.BAD_REQUEST, "El cuerpo del cliente es obligatorio");
        }
        try {
            ClienteResumenDTO creado = new ClienteResumenDTO();
            if (CirugiaBL.TIPO_CLIENTE_CLINICA.equals(dto.getTipoCliente())) {
                ClinicaHospital c = new ClinicaHospital();
                c.setNombre(dto.getNombre());
                c.setDireccion(dto.getDireccion());
                c.setEmailContacto(dto.getEmailContacto());
                c.setTelefono(dto.getTelefono());
                c.setRuc(dto.getRuc());
                c.setTieneConsignacion(dto.isTieneConsignacion());
                c.setPeriodoCredito(dto.getPeriodoCredito());
                c.setUsername(dto.getUsername());
                c.setPasswordHash(dto.getPassword());
                clinicaBL.insert(c);
                creado.setIdCliente(c.getIdCliente());
                creado.setNombre(c.getNombre());
                creado.setEmailContacto(c.getEmailContacto());
                creado.setTelefono(c.getTelefono());
                creado.setDocumento("RUC " + c.getRuc());
                creado.setActivo(c.isActivo());
            } else if (CirugiaBL.TIPO_CLIENTE_PARTICULAR.equals(dto.getTipoCliente())) {
                PacienteParticular c = new PacienteParticular();
                c.setNombre(dto.getNombre());
                c.setDireccion(dto.getDireccion());
                c.setEmailContacto(dto.getEmailContacto());
                c.setTelefono(dto.getTelefono());
                c.setDni(dto.getDni());
                c.setPagoConfirmado(dto.isPagoConfirmado());
                c.setUsername(dto.getUsername());
                c.setPasswordHash(dto.getPassword());
                pacienteBL.insert(c);
                creado.setIdCliente(c.getIdCliente());
                creado.setNombre(c.getNombre());
                creado.setEmailContacto(c.getEmailContacto());
                creado.setTelefono(c.getTelefono());
                creado.setDocumento("DNI " + c.getDni());
                creado.setDni(c.getDni());
                creado.setActivo(c.isActivo());
            } else {
                return Errores.error(Response.Status.BAD_REQUEST, "Tipo de cliente inválido: use "
                        + CirugiaBL.TIPO_CLIENTE_CLINICA + " o " + CirugiaBL.TIPO_CLIENTE_PARTICULAR);
            }
            creado.setTipoCliente(dto.getTipoCliente());
            return Response.created(java.net.URI.create("/clientes/" + creado.getTipoCliente() + "/" + creado.getIdCliente()))
                    .entity(creado).build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    @GET
    @Path("/{tipo}/{id}")
    @RequiereRol("ADMINISTRADOR")
    public Response detalle(@PathParam("tipo") String tipo, @PathParam("id") int id) {
        try {
            ClienteRegistroDTO d = new ClienteRegistroDTO();
            d.setTipoCliente(tipo);
            if (CirugiaBL.TIPO_CLIENTE_CLINICA.equals(tipo)) {
                ClinicaHospital c = clinicaBL.findById(id);
                if (c == null) {
                    return Errores.error(Response.Status.NOT_FOUND, "No existe la clínica con id " + id);
                }
                d.setIdCliente(c.getIdCliente());
                d.setNombre(c.getNombre());
                d.setDireccion(c.getDireccion());
                d.setEmailContacto(c.getEmailContacto());
                d.setTelefono(c.getTelefono());
                d.setRuc(c.getRuc());
                d.setTieneConsignacion(c.isTieneConsignacion());
                d.setPeriodoCredito(c.getPeriodoCredito());
                d.setActivo(c.isActivo());
            } else if (CirugiaBL.TIPO_CLIENTE_PARTICULAR.equals(tipo)) {
                PacienteParticular c = pacienteBL.findById(id);
                if (c == null) {
                    return Errores.error(Response.Status.NOT_FOUND, "No existe el paciente con id " + id);
                }
                d.setIdCliente(c.getIdCliente());
                d.setNombre(c.getNombre());
                d.setDireccion(c.getDireccion());
                d.setEmailContacto(c.getEmailContacto());
                d.setTelefono(c.getTelefono());
                d.setDni(c.getDni());
                d.setPagoConfirmado(c.isPagoConfirmado());
                d.setActivo(c.isActivo());
            } else {
                return Errores.error(Response.Status.BAD_REQUEST, "Tipo de cliente inválido");
            }
            return Response.ok(d).build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    /** Modifica los datos del cliente. Usuario y contraseña no se tocan aquí. */
    @PUT
    @Path("/{tipo}/{id}")
    @RequiereRol("ADMINISTRADOR")
    public Response modificar(@PathParam("tipo") String tipo, @PathParam("id") int id, ClienteRegistroDTO dto) {
        if (dto == null) {
            return Errores.error(Response.Status.BAD_REQUEST, "El cuerpo del cliente es obligatorio");
        }
        try {
            if (CirugiaBL.TIPO_CLIENTE_CLINICA.equals(tipo)) {
                ClinicaHospital c = new ClinicaHospital();
                c.setIdCliente(id);
                c.setNombre(dto.getNombre());
                c.setDireccion(dto.getDireccion());
                c.setEmailContacto(dto.getEmailContacto());
                c.setTelefono(dto.getTelefono());
                c.setRuc(dto.getRuc());
                c.setTieneConsignacion(dto.isTieneConsignacion());
                c.setPeriodoCredito(dto.getPeriodoCredito());
                c.setActivo(dto.isActivo());
                clinicaBL.update(c);
            } else if (CirugiaBL.TIPO_CLIENTE_PARTICULAR.equals(tipo)) {
                PacienteParticular c = new PacienteParticular();
                c.setIdCliente(id);
                c.setNombre(dto.getNombre());
                c.setDireccion(dto.getDireccion());
                c.setEmailContacto(dto.getEmailContacto());
                c.setTelefono(dto.getTelefono());
                c.setDni(dto.getDni());
                c.setPagoConfirmado(dto.isPagoConfirmado());
                c.setActivo(dto.isActivo());
                pacienteBL.update(c);
            } else {
                return Errores.error(Response.Status.BAD_REQUEST, "Tipo de cliente inválido");
            }
            return Response.noContent().build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }
}
