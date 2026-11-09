
package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.DocumentoIngresoBL;
import pe.edu.pucp.klam.bl.UsuarioPlataformaBL;

import pe.edu.pucp.klam.bl.impl.DocumentoIngresoBLImpl;
import pe.edu.pucp.klam.bl.impl.UsuarioPlataformaBLImpl;

import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;

@Path("/documentos")
@Produces("application/json")
@Consumes("application/json")
public class DocumentosResource {

    private final DocumentoIngresoBL documentoBL =
            new DocumentoIngresoBLImpl();

    private final UsuarioPlataformaBL usuarioBL =
            new UsuarioPlataformaBLImpl();

    // RF07 - Listar documentos de una solicitud
    @GET
    @Path("/solicitud/{idSolicitud}")
    public Response listarPorSolicitud(
            @PathParam("idSolicitud") int idSolicitud) {

        try {
            List<DocumentoIngreso> documentos =
                    documentoBL.listarPorSolicitud(idSolicitud);

            List<Map<String, Object>> resultado =
                    new ArrayList<>();

            for (DocumentoIngreso doc : documentos) {

                Map<String, Object> datos =
                        new LinkedHashMap<>();

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

            return Response.ok(resultado).build();

        } catch (BLException ex) {
            return Response.status(
                            Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        }
    }

    // RF07 - Consultar historial documental
    @GET
    @Path("/solicitud/{idSolicitud}/historial")
    public Response consultarHistorial(
            @PathParam("idSolicitud") int idSolicitud) {

        try {
            return Response.ok(
                    documentoBL.consultarHistorialPorSolicitud(idSolicitud)
            ).build();

        } catch (BLException ex) {
            return Response.status(
                            Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        }
    }

    // RF07 - Verificar documentos obligatorios
    @GET
    @Path("/solicitud/{idSolicitud}/verificacion")
    public Response verificarDocumentos(
            @PathParam("idSolicitud") int idSolicitud) {

        try {
            boolean completo =
                    documentoBL.verificarDocumentosObligatoriosValidados(
                            idSolicitud);

            return Response.ok(
                    Map.of("documentacionCompleta", completo)
            ).build();

        } catch (BLException ex) {
            return Response.status(
                            Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        }
    }

    // RF07 - Validar documento
    @POST
    @Path("/{id}/validacion")
    public Response validarDocumento(
            @PathParam("id") int id,
            Map<String, String> datos) {

        if (datos == null) {
            return Response.status(
                            Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Datos obligatorios"))
                    .build();
        }

        try {
            UsuarioPlataforma usuario = usuarioBL.autenticar(
                    datos.get("username"),
                    datos.get("password")
            );

            if (usuario == null) {
                return Response.status(
                                Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Credenciales incorrectas"))
                        .build();
            }

            if (!"ADMINISTRADOR".equals(usuario.getRol())) {
                return Response.status(
                                Response.Status.FORBIDDEN)
                        .entity(Map.of(
                                "error", "No tiene permisos para validar documentos"))
                        .build();
            }

            documentoBL.validarDocumento(
                    id,
                    usuario.getUsername()
            );

            return Response.noContent().build();

        } catch (BLException ex) {
            return Response.status(
                            Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        }
    }

    // RF07 - Rechazar documento
    @POST
    @Path("/{id}/rechazo")
    public Response rechazarDocumento(
            @PathParam("id") int id,
            Map<String, String> datos) {

        if (datos == null ||
                datos.get("motivo") == null ||
                datos.get("motivo").isBlank()) {

            return Response.status(
                            Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "error", "El motivo de rechazo es obligatorio"))
                    .build();
        }

        try {
            UsuarioPlataforma usuario = usuarioBL.autenticar(
                    datos.get("username"),
                    datos.get("password")
            );

            if (usuario == null) {
                return Response.status(
                                Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Credenciales incorrectas"))
                        .build();
            }

            if (!"ADMINISTRADOR".equals(usuario.getRol())) {
                return Response.status(
                                Response.Status.FORBIDDEN)
                        .entity(Map.of(
                                "error", "No tiene permisos para rechazar documentos"))
                        .build();
            }

            documentoBL.rechazarDocumento(
                    id,
                    datos.get("motivo"),
                    usuario.getUsername()
            );

            return Response.noContent().build();

        } catch (BLException ex) {
            return Response.status(
                            Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        }
    }
}
