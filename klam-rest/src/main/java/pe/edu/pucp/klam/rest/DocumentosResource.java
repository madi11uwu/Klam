package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.DocumentoIngresoBL;
import pe.edu.pucp.klam.bl.SolicitudCirugiaBL;
import pe.edu.pucp.klam.bl.UsuarioPlataformaBL;
import pe.edu.pucp.klam.bl.impl.CirugiaBLImpl;
import pe.edu.pucp.klam.bl.impl.DocumentoIngresoBLImpl;
import pe.edu.pucp.klam.bl.impl.SolicitudCirugiaBLImpl;
import pe.edu.pucp.klam.bl.impl.UsuarioPlataformaBLImpl;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;
import pe.edu.pucp.klam.rest.dto.AccionDocumentoDTO;

@Path("/documentos")
@Produces("application/json")
@Consumes("application/json")
public class DocumentosResource {

    private final DocumentoIngresoBL documentoBL = new DocumentoIngresoBLImpl();
    private final SolicitudCirugiaBL solicitudBL = new SolicitudCirugiaBLImpl();
    private final CirugiaBL cirugiaBL = new CirugiaBLImpl();
    private final UsuarioPlataformaBL usuarioBL = new UsuarioPlataformaBLImpl();

    @GET
    @Path("/solicitud/{idSolicitud}")
    public Response listarPorSolicitud(@PathParam("idSolicitud") int idSolicitud) {
        if (idSolicitud <= 0) {
            return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        }
        try {
            if (solicitudBL.findSolicitudById(idSolicitud) == null) {
                return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            }
            return Response.ok(respuestaDocumentos(documentoBL.listarPorSolicitud(idSolicitud))).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/solicitud/{idSolicitud}/historial")
    public Response consultarHistorial(@PathParam("idSolicitud") int idSolicitud) {
        if (idSolicitud <= 0) {
            return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        }
        try {
            if (solicitudBL.findSolicitudById(idSolicitud) == null) {
                return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            }
            return Response.ok(documentoBL.consultarHistorialPorSolicitud(idSolicitud)).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/solicitud/{idSolicitud}/verificacion")
    public Response verificarDocumentos(@PathParam("idSolicitud") int idSolicitud) {
        if (idSolicitud <= 0) {
            return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        }
        try {
            if (solicitudBL.findSolicitudById(idSolicitud) == null) {
                return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            }
            boolean completo = documentoBL.verificarDocumentosObligatoriosValidados(idSolicitud);
            return Response.ok(Map.of("documentacionCompleta", completo)).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/cirugia/{idCirugia}")
    public Response listarPorCirugia(@PathParam("idCirugia") int idCirugia) {
        if (idCirugia <= 0) {
            return error(Response.Status.BAD_REQUEST, "El ID de la cirugía no es válido");
        }
        try {
            if (cirugiaBL.findById(idCirugia) == null) {
                return error(Response.Status.NOT_FOUND, "No existe la cirugía");
            }
            return Response.ok(respuestaDocumentos(documentoBL.listarPorCirugia(idCirugia))).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/{id}/validacion")
    public Response validarDocumento(@PathParam("id") int id, AccionDocumentoDTO datos) {
        if (id <= 0) {
            return error(Response.Status.BAD_REQUEST, "El ID del documento no es válido");
        }
        if (sinCredenciales(datos)) {
            return error(Response.Status.UNAUTHORIZED, "Credenciales obligatorias");
        }
        UsuarioPlataforma usuario;
        try {
            usuario = usuarioBL.autenticar(datos.getUsername(), datos.getPassword());
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
        if (usuario == null) {
            return error(Response.Status.UNAUTHORIZED, "Credenciales incorrectas");
        }
        if (!"ADMINISTRADOR".equals(usuario.getRol())) {
            return error(Response.Status.FORBIDDEN, "No tiene permisos para validar documentos");
        }
        try {
            DocumentoIngreso doc = documentoBL.findById(id);
            if (doc == null || !doc.isActivo()) {
                return error(Response.Status.NOT_FOUND, "No existe el documento");
            }
            documentoBL.validarDocumento(id, usuario.getUsername());
            return Response.noContent().build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        }
    }

    @POST
    @Path("/{id}/rechazo")
    public Response rechazarDocumento(@PathParam("id") int id, AccionDocumentoDTO datos) {
        if (id <= 0) {
            return error(Response.Status.BAD_REQUEST, "El ID del documento no es válido");
        }
        if (sinCredenciales(datos)) {
            return error(Response.Status.UNAUTHORIZED, "Credenciales obligatorias");
        }
        UsuarioPlataforma usuario;
        try {
            usuario = usuarioBL.autenticar(datos.getUsername(), datos.getPassword());
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
        if (usuario == null) {
            return error(Response.Status.UNAUTHORIZED, "Credenciales incorrectas");
        }
        if (!"ADMINISTRADOR".equals(usuario.getRol())) {
            return error(Response.Status.FORBIDDEN, "No tiene permisos para rechazar documentos");
        }
        if (datos.getMotivo() == null || datos.getMotivo().isBlank()
                || datos.getMotivo().length() > 200) {
            return error(Response.Status.BAD_REQUEST, "El motivo de rechazo debe tener entre 1 y 200 caracteres");
        }
        try {
            DocumentoIngreso doc = documentoBL.findById(id);
            if (doc == null || !doc.isActivo()) {
                return error(Response.Status.NOT_FOUND, "No existe el documento");
            }
            documentoBL.rechazarDocumento(id, datos.getMotivo(), usuario.getUsername());
            return Response.noContent().build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        }
    }

    // La BL recibe una ruta; este endpoint no carga ni almacena archivos físicos.
    @POST
    @Path("/{id}/resubida")
    public Response resubirDocumento(@PathParam("id") int id, AccionDocumentoDTO datos) {
        if (id <= 0) {
            return error(Response.Status.BAD_REQUEST, "El ID del documento no es válido");
        }
        if (sinCredenciales(datos)) {
            return error(Response.Status.UNAUTHORIZED, "Credenciales obligatorias");
        }
        UsuarioPlataforma usuario;
        try {
            usuario = usuarioBL.autenticar(datos.getUsername(), datos.getPassword());
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
        if (usuario == null) {
            return error(Response.Status.UNAUTHORIZED, "Credenciales incorrectas");
        }
        if (!"ADMINISTRADOR".equals(usuario.getRol())
                && !"VENDEDOR".equals(usuario.getRol())) {
            return error(Response.Status.FORBIDDEN, "No tiene permisos para resubir documentos");
        }
        if (datos.getArchivoPath() == null || datos.getArchivoPath().isBlank()
                || datos.getArchivoPath().length() > 255) {
            return error(Response.Status.BAD_REQUEST, "La ruta del archivo debe tener entre 1 y 255 caracteres");
        }
        try {
            DocumentoIngreso anterior = documentoBL.findById(id);
            if (anterior == null || !anterior.isActivo()) {
                return error(Response.Status.NOT_FOUND, "No existe el documento");
            }
            DocumentoIngreso nuevo = documentoBL.resubirDocumento(
                    id, datos.getArchivoPath(), usuario.getUsername());
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("idDocumento", nuevo.getIdDocumento(), "version", nuevo.getVersion()))
                    .build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        }
    }

    private boolean sinCredenciales(AccionDocumentoDTO datos) {
        return datos == null || datos.getUsername() == null || datos.getUsername().isBlank()
                || datos.getPassword() == null || datos.getPassword().isBlank();
    }

    private List<Map<String, Object>> respuestaDocumentos(List<DocumentoIngreso> documentos) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (DocumentoIngreso doc : documentos) {
            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("idDocumento", doc.getIdDocumento());
            datos.put("tipoDocumento", doc.getTipoDocumento());
            datos.put("estadoValidacion", doc.getEstadoValidacion());
            datos.put("version", doc.getVersion());
            datos.put("fechaCarga", doc.getFechaCarga() != null
                    ? doc.getFechaCarga().toString() : null);
            datos.put("fechaValidacion", doc.getFechaValidacion() != null
                    ? doc.getFechaValidacion().toString() : null);
            datos.put("motivoRechazo", doc.getMotivoRechazo());
            resultado.add(datos);
        }
        return resultado;
    }

    private Response fallo(BLException ex, Response.Status estadoNegocio) {
        return error(ex.getCause() == null ? estadoNegocio : Response.Status.INTERNAL_SERVER_ERROR,
                ex.getMessage());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(Map.of("error", mensaje)).build();
    }
}
