package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.bl.DocumentoIngresoBL;
import pe.edu.pucp.klam.bl.SolicitudCirugiaBL;
import pe.edu.pucp.klam.bl.impl.CirugiaBLImpl;
import pe.edu.pucp.klam.bl.impl.DocumentoIngresoBLImpl;
import pe.edu.pucp.klam.bl.impl.SolicitudCirugiaBLImpl;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.TipoDocumentoIngreso;
import pe.edu.pucp.klam.rest.dto.AccionDocumentoDTO;

// Quien opera (idUsuario, rol, responsable) llega desde la sesión de Blazor como parámetros de consulta.
@Path("/documentos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentosResource {

    private final DocumentoIngresoBL documentoBL = new DocumentoIngresoBLImpl();
    private final SolicitudCirugiaBL solicitudBL = new SolicitudCirugiaBLImpl();
    private final CirugiaBL cirugiaBL = new CirugiaBLImpl();

    @GET
    @Path("/solicitud/{idSolicitud}")
    public Response listarPorSolicitud(@PathParam("idSolicitud") int idSolicitud) {
        if (idSolicitud <= 0) return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        try {
            if (solicitudBL.findSolicitudById(idSolicitud) == null) return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            return Response.ok(respuestaDocumentos(documentoBL.listarPorSolicitud(idSolicitud))).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/solicitud/{idSolicitud}/propios")
    public Response listarPropios(@PathParam("idSolicitud") int idSolicitud,
                                  @QueryParam("idUsuario") int idUsuario, @QueryParam("rol") String rol) {
        if (idSolicitud <= 0) return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        try {
            return Response.ok(respuestaDocumentos(documentoBL.listarPresentadosPorUsuario(
                    idSolicitud, idUsuario, rol))).build();
        } catch (SecurityException ex) {
            return error(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/propios")
    public Response listarPropios(@QueryParam("idUsuario") int idUsuario, @QueryParam("rol") String rol) {
        try {
            return Response.ok(respuestaDocumentos(documentoBL.listarPresentadosPorUsuario(
                    null, idUsuario, rol))).build();
        } catch (SecurityException ex) {
            return error(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/solicitud/{idSolicitud}/historial")
    public Response consultarHistorial(@PathParam("idSolicitud") int idSolicitud) {
        if (idSolicitud <= 0) return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        try {
            if (solicitudBL.findSolicitudById(idSolicitud) == null) return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            return Response.ok(documentoBL.consultarHistorialPorSolicitud(idSolicitud)).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/solicitud/{idSolicitud}/verificacion")
    public Response verificarDocumentos(@PathParam("idSolicitud") int idSolicitud) {
        if (idSolicitud <= 0) return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        try {
            if (solicitudBL.findSolicitudById(idSolicitud) == null) return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            return Response.ok(Map.of("documentacionCompleta", documentoBL.verificarDocumentosObligatoriosValidados(idSolicitud))).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/cirugia/{idCirugia}")
    public Response listarPorCirugia(@PathParam("idCirugia") int idCirugia) {
        if (idCirugia <= 0) return error(Response.Status.BAD_REQUEST, "El ID de la cirugía no es válido");
        try {
            if (cirugiaBL.findById(idCirugia) == null) return error(Response.Status.NOT_FOUND, "No existe la cirugía");
            return Response.ok(respuestaDocumentos(documentoBL.listarPorCirugia(idCirugia))).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/solicitud/{idSolicitud}/archivo")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response registrarArchivo(@PathParam("idSolicitud") int idSolicitud,
            @FormParam("tipoDocumento") String tipoTexto, @FormParam("archivo") EntityPart archivo,
            @QueryParam("idUsuario") int idUsuario, @QueryParam("rol") String rol,
            @QueryParam("responsable") String responsable) {
        if (idSolicitud <= 0 || tipoTexto == null) return error(Response.Status.BAD_REQUEST, "Solicitud y tipo de documento obligatorios");
        TipoDocumentoIngreso tipo;
        try {
            tipo = TipoDocumentoIngreso.valueOf(tipoTexto);
        } catch (IllegalArgumentException ex) {
            return error(Response.Status.BAD_REQUEST, "Tipo de documento no válido");
        }
        try {
            var solicitud = solicitudBL.findSolicitudById(idSolicitud);
            if (solicitud == null) return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            if ("VENDEDOR".equals(rol)
                    && (solicitud.getVendedorRegistrador() == null
                    || solicitud.getVendedorRegistrador().getIdUsuario() != idUsuario)) {
                return error(Response.Status.FORBIDDEN, "El vendedor solo puede adjuntar documentos a sus solicitudes");
            }
            String clave = ArchivoDocumentoStorage.guardar(archivo);
            try {
                DocumentoIngreso doc = documentoBL.registrarDocumentoSolicitud(idSolicitud, tipo, clave,
                        idUsuario, rol, responsable);
                return Response.status(Response.Status.CREATED)
                        .entity(Map.of("idDocumento", doc.getIdDocumento(), "version", doc.getVersion())).build();
            } catch (RuntimeException ex) {
                limpiarArchivo(clave, ex);
                throw ex;
            }
        } catch (IllegalArgumentException ex) {
            return error(Response.Status.BAD_REQUEST, ex.getMessage());
        } catch (SecurityException ex) {
            return error(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        } catch (IOException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo almacenar el archivo");
        }
    }

    @GET
    @Path("/{id}/archivo")
    public Response descargarArchivo(@PathParam("id") int id,
                                     @QueryParam("idUsuario") int idUsuario, @QueryParam("rol") String rol) {
        if (id <= 0) return error(Response.Status.BAD_REQUEST, "El ID del documento no es válido");
        try {
            DocumentoIngreso doc = documentoBL.findById(id);
            if (doc == null || !doc.isActivo()) return error(Response.Status.NOT_FOUND, "No existe el documento");
            if (!"ADMINISTRADOR".equals(rol) && !documentoBL.esPresentadorOriginal(id, idUsuario, rol)) {
                return error(Response.Status.FORBIDDEN, "No puede consultar este archivo");
            }
            byte[] contenido = ArchivoDocumentoStorage.leer(doc.getArchivoPath());
            return Response.ok(contenido, ArchivoDocumentoStorage.tipoContenido(doc.getArchivoPath()))
                    .header("Content-Disposition", "attachment; filename=\"documento-" + id
                            + ArchivoDocumentoStorage.extension(doc.getArchivoPath()) + "\"")
                    .header("X-Content-Type-Options", "nosniff").build();
        } catch (NoSuchFileException ex) {
            return error(Response.Status.NOT_FOUND, "El archivo físico no está disponible");
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (IOException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo leer el archivo");
        }
    }

    @POST
    @Path("/{id}/validacion")
    public Response validarDocumento(@PathParam("id") int id, @QueryParam("responsable") String responsable) {
        if (id <= 0) return error(Response.Status.BAD_REQUEST, "El ID del documento no es válido");
        try {
            DocumentoIngreso doc = documentoBL.findById(id);
            if (doc == null || !doc.isActivo()) return error(Response.Status.NOT_FOUND, "No existe el documento");
            documentoBL.validarDocumento(id, responsable);
            return Response.noContent().build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        }
    }

    @POST
    @Path("/{id}/rechazo")
    public Response rechazarDocumento(@PathParam("id") int id, @QueryParam("responsable") String responsable,
                                      AccionDocumentoDTO datos) {
        if (id <= 0) return error(Response.Status.BAD_REQUEST, "El ID del documento no es válido");
        if (datos == null || datos.getMotivo() == null || datos.getMotivo().isBlank()
                || datos.getMotivo().length() > 200) {
            return error(Response.Status.BAD_REQUEST, "El motivo de rechazo debe tener entre 1 y 200 caracteres");
        }
        try {
            DocumentoIngreso doc = documentoBL.findById(id);
            if (doc == null || !doc.isActivo()) return error(Response.Status.NOT_FOUND, "No existe el documento");
            documentoBL.rechazarDocumento(id, datos.getMotivo(), datos.isHabilitarResubida(), responsable);
            return Response.noContent().build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        }
    }

    @POST
    @Path("/{id}/resubida")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response resubirDocumento(@PathParam("id") int id, @FormParam("archivo") EntityPart archivo,
                                     @QueryParam("idUsuario") int idUsuario, @QueryParam("rol") String rol,
                                     @QueryParam("responsable") String responsable) {
        if (id <= 0) return error(Response.Status.BAD_REQUEST, "El ID del documento no es válido");
        try {
            DocumentoIngreso anterior = documentoBL.findById(id);
            if (anterior == null || !anterior.isActivo()) return error(Response.Status.NOT_FOUND, "No existe el documento");
            documentoBL.verificarResubida(id, idUsuario, rol);
            String clave = ArchivoDocumentoStorage.guardar(archivo);
            try {
                DocumentoIngreso nuevo = documentoBL.resubirDocumento(id, clave,
                        idUsuario, rol, responsable);
                return Response.status(Response.Status.CREATED)
                        .entity(Map.of("idDocumento", nuevo.getIdDocumento(), "version", nuevo.getVersion())).build();
            } catch (RuntimeException ex) {
                limpiarArchivo(clave, ex);
                throw ex;
            }
        } catch (SecurityException ex) {
            return error(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return error(Response.Status.BAD_REQUEST, ex.getMessage());
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        } catch (IOException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo almacenar el archivo");
        }
    }

    private void limpiarArchivo(String clave, RuntimeException original) {
        try {
            ArchivoDocumentoStorage.eliminar(clave);
        } catch (IOException limpieza) {
            original.addSuppressed(limpieza);
        }
    }

    private List<Map<String, Object>> respuestaDocumentos(List<DocumentoIngreso> documentos) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (DocumentoIngreso doc : documentos) {
            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("idDocumento", doc.getIdDocumento());
            datos.put("idSolicitud", doc.getSolicitud() == null ? null : doc.getSolicitud().getIdSolicitud());
            datos.put("tipoDocumento", doc.getTipoDocumento());
            datos.put("estadoValidacion", doc.getEstadoValidacion());
            datos.put("version", doc.getVersion());
            datos.put("fechaCarga", doc.getFechaCarga() != null ? doc.getFechaCarga().toString() : null);
            datos.put("fechaValidacion", doc.getFechaValidacion() != null ? doc.getFechaValidacion().toString() : null);
            datos.put("motivoRechazo", doc.getMotivoRechazo());
            datos.put("habilitarResubida", doc.isHabilitarResubida());
            resultado.add(datos);
        }
        return resultado;
    }

    private Response fallo(BLException ex, Response.Status estadoNegocio) {
        return error(ex.getCause() == null ? estadoNegocio : Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(Map.of("error", mensaje)).build();
    }
}
