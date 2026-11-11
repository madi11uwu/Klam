package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.HttpHeaders;
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

@Path("/documentos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentosResource {

    private final DocumentoIngresoBL documentoBL = new DocumentoIngresoBLImpl();
    private final SolicitudCirugiaBL solicitudBL = new SolicitudCirugiaBLImpl();
    private final CirugiaBL cirugiaBL = new CirugiaBLImpl();

    @Context
    private HttpHeaders headers;

    @GET
    @Path("/solicitud/{idSolicitud}")
    public Response listarPorSolicitud(@PathParam("idSolicitud") int idSolicitud) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
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
    public Response listarPropios(@PathParam("idSolicitud") int idSolicitud) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (idSolicitud <= 0) return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        if (!esAdministrador(sesion) && !"VENDEDOR".equals(sesion.rol()) && !esCliente(sesion)) return sinPermiso();
        try {
            return Response.ok(respuestaDocumentos(documentoBL.listarPresentadosPorUsuario(
                    idSolicitud, sesion.idUsuario(), sesion.rol()))).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/propios")
    public Response listarPropios() {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion) && !"VENDEDOR".equals(sesion.rol()) && !esCliente(sesion)) return sinPermiso();
        try {
            return Response.ok(respuestaDocumentos(documentoBL.listarPresentadosPorUsuario(
                    null, sesion.idUsuario(), sesion.rol()))).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/solicitud/{idSolicitud}/historial")
    public Response consultarHistorial(@PathParam("idSolicitud") int idSolicitud) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
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
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
        if (idSolicitud <= 0) return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        try {
            if (solicitudBL.findSolicitudById(idSolicitud) == null) return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            return Response.ok(Map.of("documentacionCompleta", documentoBL.verificarDocumentosObligatoriosValidados(idSolicitud))).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Documentos obligatorios de una solicitud y estado vigente de cada uno, para quien la presenta
     * (cliente dueño, vendedor que la registró) o el administrador. Permite saber qué falta subir
     * y qué documento rechazado tiene la resubida habilitada.
     */
    @GET
    @Path("/solicitud/{idSolicitud}/requisitos")
    public Response requisitos(@PathParam("idSolicitud") int idSolicitud) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (idSolicitud <= 0) return error(Response.Status.BAD_REQUEST, "El ID de la solicitud no es válido");
        try {
            var solicitud = solicitudBL.findSolicitudById(idSolicitud);
            if (solicitud == null) return error(Response.Status.NOT_FOUND, "No existe la solicitud");
            boolean duenio = esAdministrador(sesion)
                    || (esCliente(sesion) && esSolicitudDelCliente(solicitud, sesion))
                    || ("VENDEDOR".equals(sesion.rol()) && solicitud.getVendedorRegistrador() != null
                        && solicitud.getVendedorRegistrador().getIdUsuario() == sesion.idUsuario());
            if (!duenio) return sinPermiso();
            String tipoCliente = solicitud.getCliente() instanceof pe.edu.pucp.klam.modelo.clientes.ClinicaHospital
                    ? pe.edu.pucp.klam.bl.CirugiaBL.TIPO_CLIENTE_CLINICA : pe.edu.pucp.klam.bl.CirugiaBL.TIPO_CLIENTE_PARTICULAR;
            List<String> obligatorios = new ArrayList<>();
            for (var t : documentoBL.listarDocumentosObligatorios(tipoCliente)) obligatorios.add(t.name());
            // Versión vigente (la más alta) de cada tipo
            Map<String, DocumentoIngreso> vigentes = new LinkedHashMap<>();
            for (DocumentoIngreso d : documentoBL.listarPorSolicitud(idSolicitud)) {
                DocumentoIngreso actual = vigentes.get(d.getTipoDocumento().name());
                if (d.isActivo() && (actual == null || d.getVersion() > actual.getVersion())) vigentes.put(d.getTipoDocumento().name(), d);
            }
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("idSolicitud", idSolicitud);
            cuerpo.put("estadoSolicitud", solicitud.getEstado() == null ? null : solicitud.getEstado().name());
            cuerpo.put("obligatorios", obligatorios);
            cuerpo.put("documentos", respuestaDocumentos(new ArrayList<>(vigentes.values())));
            return Response.ok(cuerpo).build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/cirugia/{idCirugia}")
    public Response listarPorCirugia(@PathParam("idCirugia") int idCirugia) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
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
            @FormParam("tipoDocumento") String tipoTexto, @FormParam("archivo") EntityPart archivo) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion) && !"VENDEDOR".equals(sesion.rol()) && !esCliente(sesion)) return sinPermiso();
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
            if ("VENDEDOR".equals(sesion.rol())
                    && (solicitud.getVendedorRegistrador() == null
                    || solicitud.getVendedorRegistrador().getIdUsuario() != sesion.idUsuario())) {
                return error(Response.Status.FORBIDDEN, "El vendedor solo puede adjuntar documentos a sus solicitudes");
            }
            if (esCliente(sesion) && !esSolicitudDelCliente(solicitud, sesion)) {
                return error(Response.Status.FORBIDDEN, "Solo puede adjuntar documentos a sus propias solicitudes");
            }
            String clave = ArchivoDocumentoStorage.guardar(archivo);
            try {
                DocumentoIngreso doc = documentoBL.registrarDocumentoSolicitud(idSolicitud, tipo, clave,
                        sesion.idUsuario(), sesion.rol(), sesion.username());
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
    public Response descargarArchivo(@PathParam("id") int id) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (id <= 0) return error(Response.Status.BAD_REQUEST, "El ID del documento no es válido");
        try {
            DocumentoIngreso doc = documentoBL.findById(id);
            if (doc == null || !doc.isActivo()) return error(Response.Status.NOT_FOUND, "No existe el documento");
            if (!esAdministrador(sesion) && !documentoBL.esPresentadorOriginal(id, sesion.idUsuario(), sesion.rol())) {
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
    public Response validarDocumento(@PathParam("id") int id) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
        if (id <= 0) return error(Response.Status.BAD_REQUEST, "El ID del documento no es válido");
        try {
            DocumentoIngreso doc = documentoBL.findById(id);
            if (doc == null || !doc.isActivo()) return error(Response.Status.NOT_FOUND, "No existe el documento");
            documentoBL.validarDocumento(id, sesion.username());
            return Response.noContent().build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        }
    }

    @POST
    @Path("/{id}/rechazo")
    public Response rechazarDocumento(@PathParam("id") int id, AccionDocumentoDTO datos) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (!esAdministrador(sesion)) return sinPermiso();
        if (id <= 0) return error(Response.Status.BAD_REQUEST, "El ID del documento no es válido");
        if (datos == null || datos.getMotivo() == null || datos.getMotivo().isBlank()
                || datos.getMotivo().length() > 200) {
            return error(Response.Status.BAD_REQUEST, "El motivo de rechazo debe tener entre 1 y 200 caracteres");
        }
        try {
            DocumentoIngreso doc = documentoBL.findById(id);
            if (doc == null || !doc.isActivo()) return error(Response.Status.NOT_FOUND, "No existe el documento");
            documentoBL.rechazarDocumento(id, datos.getMotivo(), datos.isHabilitarResubida(), sesion.username());
            return Response.noContent().build();
        } catch (BLException ex) {
            return fallo(ex, Response.Status.CONFLICT);
        }
    }

    @POST
    @Path("/{id}/resubida")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response resubirDocumento(@PathParam("id") int id, @FormParam("archivo") EntityPart archivo) {
        SesionesRest.Sesion sesion = sesion();
        if (sesion == null) return noAutenticado();
        if (id <= 0) return error(Response.Status.BAD_REQUEST, "El ID del documento no es válido");
        try {
            DocumentoIngreso anterior = documentoBL.findById(id);
            if (anterior == null || !anterior.isActivo()) return error(Response.Status.NOT_FOUND, "No existe el documento");
            documentoBL.verificarResubida(id, sesion.idUsuario(), sesion.rol());
            String clave = ArchivoDocumentoStorage.guardar(archivo);
            try {
                DocumentoIngreso nuevo = documentoBL.resubirDocumento(id, clave,
                        sesion.idUsuario(), sesion.rol(), sesion.username());
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

    private SesionesRest.Sesion sesion() {
        return SesionesRest.validar(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
    }

    private static boolean esCliente(SesionesRest.Sesion sesion) {
        return ("CLINICA_HOSPITAL".equals(sesion.rol()) || "PACIENTE_PARTICULAR".equals(sesion.rol()))
                && sesion.idCliente() != null;
    }

    // La solicitud es del cliente si su solicitante coincide en tipo e id con el cliente de la sesión.
    private static boolean esSolicitudDelCliente(pe.edu.pucp.klam.modelo.agendaoperaciones.SolicitudCirugia solicitud,
                                                 SesionesRest.Sesion sesion) {
        var cliente = solicitud.getCliente();
        if (cliente == null || cliente.getIdCliente() != sesion.idCliente()) return false;
        boolean esClinica = cliente instanceof pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
        return esClinica == "CLINICA_HOSPITAL".equals(sesion.tipoCliente());
    }

    private boolean esAdministrador(SesionesRest.Sesion sesion) {
        return "ADMINISTRADOR".equals(sesion.rol());
    }

    private Response noAutenticado() {
        return error(Response.Status.UNAUTHORIZED, "Sesión inválida o expirada");
    }

    private Response sinPermiso() {
        return error(Response.Status.FORBIDDEN, "No tiene permisos para esta operación");
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
