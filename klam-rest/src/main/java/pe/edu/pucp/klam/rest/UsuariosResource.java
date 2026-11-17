package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.UsuarioPlataformaBL;
import pe.edu.pucp.klam.bl.impl.UsuarioPlataformaBLImpl;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Administrador;
import pe.edu.pucp.klam.modelo.usuariosPermisos.TecnicoInstrumentista;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;
import pe.edu.pucp.klam.rest.dto.MetricasUsuariosDTO;
import pe.edu.pucp.klam.rest.dto.UsuarioDTO;
import pe.edu.pucp.klam.rest.dto.UsuarioFormDTO;

import java.net.URI;
import java.util.List;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuariosResource {

    private final UsuarioPlataformaBL usuarioBL = new UsuarioPlataformaBLImpl();

    @Context
    private HttpHeaders headers;

    @Context
    private UriInfo uriInfo;

    private SesionesRest.Sesion sesion() {
        return SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
    }

    private Response requerirAdministrador() {
        SesionesRest.Sesion s = sesion();
        if (s == null) {
            return Errores.error(Response.Status.UNAUTHORIZED, "Sesión inválida o expirada");
        }
        if (!"ADMINISTRADOR".equals(s.rol())) {
            return Errores.error(Response.Status.FORBIDDEN, "No tiene permisos para gestionar usuarios");
        }
        return null;
    }

    /**
     * Listado general de usuarios para el directorio del equipo (RF03).
     * Exclusivo para administradores.
     */
    @GET
    public Response listar(
            @QueryParam("rol") String rol,
            @QueryParam("estado") String estado,
            @QueryParam("busqueda") String busqueda) {

        Response auth = requerirAdministrador();
        if (auth != null) return auth;

        try {
            List<UsuarioPlataforma> lista = usuarioBL.listar(rol, estado, busqueda);
            List<UsuarioDTO> dtos = lista.stream().map(UsuarioDTO::new).toList();
            return Response.ok(dtos).build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    /**
     * Métricas numéricas reales para las tarjetas de la pantalla de usuarios (RF03).
     */
    @GET
    @Path("/metricas")
    public Response metricas() {
        Response auth = requerirAdministrador();
        if (auth != null) return auth;

        try {
            List<UsuarioPlataforma> todos = usuarioBL.listar(null, null, null);
            int total = todos.size();
            int activas = (int) todos.stream().filter(UsuarioPlataforma::isActivo).count();
            int inactivas = total - activas;
            int roles = 3; // Administrador, Vendedor, Instrumentista

            return Response.ok(new MetricasUsuariosDTO(total, activas, inactivas, roles)).build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    /**
     * Consulta de instrumentistas activos (reutilizable en la selección de responsable de cirugía - RF02 transversal).
     * Accesible por Administrador y Vendedor.
     */
    @GET
    @Path("/instrumentistas-activos")
    public Response instrumentistasActivos() {
        SesionesRest.Sesion s = sesion();
        if (s == null) {
            return Errores.error(Response.Status.UNAUTHORIZED, "Sesión inválida o expirada");
        }
        if (!"ADMINISTRADOR".equals(s.rol()) && !"VENDEDOR".equals(s.rol()) && !"TECNICO_INSTRUMENTISTA".equals(s.rol())) {
            return Errores.error(Response.Status.FORBIDDEN, "No tiene permisos para consultar instrumentistas");
        }

        try {
            List<TecnicoInstrumentista> tecnicos = usuarioBL.listarInstrumentistasActivos();
            List<UsuarioDTO> dtos = tecnicos.stream().map(UsuarioDTO::new).toList();
            return Response.ok(dtos).build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    /**
     * Detalle de un usuario por ID.
     */
    @GET
    @Path("/{id}")
    public Response obtenerPorId(
            @PathParam("id") int id,
            @QueryParam("rol") String rol) {

        Response auth = requerirAdministrador();
        if (auth != null) return auth;

        try {
            UsuarioPlataforma u = usuarioBL.buscarPorIdYRol(id, rol);
            if (u == null) {
                return Errores.error(Response.Status.NOT_FOUND, "Usuario no encontrado con ID: " + id);
            }
            return Response.ok(new UsuarioDTO(u)).build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    /**
     * Registra un nuevo usuario en la base de datos (RF03).
     */
    @POST
    public Response crear(UsuarioFormDTO dto) {
        Response auth = requerirAdministrador();
        if (auth != null) return auth;

        if (dto == null) {
            return Errores.error(Response.Status.BAD_REQUEST, "Los datos del usuario son obligatorios");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            return Errores.error(Response.Status.BAD_REQUEST, "La contraseña es obligatoria para nuevos usuarios");
        }

        try {
            UsuarioPlataforma entidad = construirEntidad(dto);
            UsuarioPlataforma creado = usuarioBL.crearUsuario(entidad, dto.getPassword());
            UsuarioDTO respuesta = new UsuarioDTO(creado);
            URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(respuesta.getId())).build();
            return Response.created(uri).entity(respuesta).build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    /**
     * Actualiza los datos de un usuario existente (RF03).
     */
    @PUT
    @Path("/{id}")
    public Response modificar(
            @PathParam("id") int id,
            @QueryParam("rol") String rol,
            UsuarioFormDTO dto) {

        Response auth = requerirAdministrador();
        if (auth != null) return auth;

        if (dto == null) {
            return Errores.error(Response.Status.BAD_REQUEST, "Los datos del usuario son obligatorios");
        }

        try {
            dto.setId(id);
            if (dto.getRol() == null || dto.getRol().isBlank()) {
                dto.setRol(rol);
            }
            UsuarioPlataforma entidad = construirEntidad(dto);
            UsuarioPlataforma actualizado = usuarioBL.modificarUsuario(entidad, dto.getPassword());
            return Response.ok(new UsuarioDTO(actualizado)).build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    /**
     * Baja lógica de un usuario (RF03). No elimina físicamente el registro para conservar histórico.
     */
    @DELETE
    @Path("/{id}")
    public Response eliminar(
            @PathParam("id") int id,
            @QueryParam("rol") String rol) {

        Response auth = requerirAdministrador();
        if (auth != null) return auth;

        try {
            usuarioBL.eliminarLogico(id, rol);
            return Response.noContent().build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    /**
     * Reactiva una cuenta desactivada previamente (RF03).
     */
    @POST
    @Path("/{id}/reactivar")
    public Response reactivar(
            @PathParam("id") int id,
            @QueryParam("rol") String rol) {

        Response auth = requerirAdministrador();
        if (auth != null) return auth;

        try {
            usuarioBL.reactivar(id, rol);
            return Response.noContent().build();
        } catch (BLException ex) {
            return Errores.desde(ex);
        }
    }

    private UsuarioPlataforma construirEntidad(UsuarioFormDTO dto) {
        String rolNorm = dto.getRol() != null ? dto.getRol().trim().toUpperCase() : "VENDEDOR";
        boolean activo = dto.getActivo() != null ? dto.getActivo() : true;

        if ("ADMINISTRADOR".equals(rolNorm) || "ADMIN".equals(rolNorm)) {
            Administrador a = new Administrador();
            poblarDatos(a, dto, activo, "ADMINISTRADOR");
            return a;
        } else if ("INSTRUMENTISTA".equals(rolNorm) || "TECNICO_INSTRUMENTISTA".equals(rolNorm)) {
            TecnicoInstrumentista t = new TecnicoInstrumentista();
            poblarDatos(t, dto, activo, "TECNICO_INSTRUMENTISTA");
            t.setEspecialidad(dto.getEspecialidad() != null ? dto.getEspecialidad().trim() : "");
            return t;
        } else {
            Vendedor v = new Vendedor();
            poblarDatos(v, dto, activo, "VENDEDOR");
            if (dto.getComisionAcumulada() != null) {
                v.setComisionAcumulada(dto.getComisionAcumulada());
            }
            return v;
        }
    }

    private void poblarDatos(UsuarioPlataforma entidad, UsuarioFormDTO dto, boolean activo, String rol) {
        if (dto.getId() != null) entidad.setIdUsuario(dto.getId());
        entidad.setNombres(dto.getNombres() != null ? dto.getNombres().trim() : "");
        entidad.setApellidos(dto.getApellidos() != null ? dto.getApellidos().trim() : "");
        entidad.setUsername(dto.getUsername() != null ? dto.getUsername().trim() : "");
        entidad.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : "");
        entidad.setActivo(activo);
        entidad.setRol(rol);
    }
}
