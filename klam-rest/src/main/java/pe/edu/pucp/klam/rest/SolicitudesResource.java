//
//package pe.edu.pucp.klam.rest;
//
//import jakarta.ws.rs.*;
//import jakarta.ws.rs.core.MediaType;
//import jakarta.ws.rs.core.Response;
//
//import pe.edu.pucp.klam.bl.BLException;
//import pe.edu.pucp.klam.bl.SolicitudCirugiaBL;
//import pe.edu.pucp.klam.bl.impl.SolicitudCirugiaBLImpl;
//import pe.edu.pucp.klam.rest.dto.RechazoSolicitudDTO;
//
//import java.sql.SQLException;
//import java.util.Map;
//
//@Path("/solicitudes")
//@Produces(MediaType.APPLICATION_JSON)
//@Consumes(MediaType.APPLICATION_JSON)
//public class SolicitudesResource {
//
//    private final SolicitudCirugiaBL solicitudBL;
//    public SolicitudesResource() {
//        solicitudBL = new SolicitudCirugiaBLImpl();
//    }
//
//    @GET
//    public Response listar(@QueryParam("estado") String estado) {
//        try {
//            return Response.ok(
//                    solicitudBL.listarSolicitudes(estado)
//            ).build();
//        } catch (BLException ex) {
//            return responderError(ex);
//        }
//    }
//
//    @GET
//    @Path("/{id}")
//    public Response buscarPorId(@PathParam("id") int id) {
//        try {
//            var solicitud = solicitudBL.findSolicitudById(id);
//
//            if (solicitud == null) {
//                return error(
//                        Response.Status.NOT_FOUND,
//                        "Solicitud no encontrada"
//                );
//            }
//
//            return Response.ok(solicitud).build();
//        } catch (BLException ex) {
//            return responderError(ex);
//        }
//    }
//
//    @POST
//    @Path("/{id}/aprobacion")
//    public Response aprobar(@PathParam("id") int id) {
//        try {
//            if (solicitudBL.findSolicitudById(id) == null) {
//                return error(
//                        Response.Status.NOT_FOUND,
//                        "Solicitud no encontrada"
//                );
//            }
//
//            solicitudBL.aprobarSolicitud(
//                    id, RESPONSABLE_DEMO
//            );
//
//            return Response.noContent().build();
//
//        } catch (BLException ex) {
//            return responderError(ex);
//        }
//    }
//
//    @POST
//    @Path("/{id}/rechazo")
//    public Response rechazar(
//            @PathParam("id") int id,
//            RechazoSolicitudDTO datos) {
//
//        if (datos == null ||
//                datos.getMotivo() == null ||
//                datos.getMotivo().isBlank()) {
//
//            return error(
//                    Response.Status.BAD_REQUEST,
//                    "El motivo de rechazo es obligatorio"
//            );
//        }
//
//        try {
//            if (solicitudBL.findSolicitudById(id) == null) {
//                return error(
//                        Response.Status.NOT_FOUND,
//                        "Solicitud no encontrada"
//                );
//            }
//
//            solicitudBL.rechazarSolicitud(
//                    id,
//                    datos.getMotivo(),
//                    RESPONSABLE_DEMO
//            );
//
//            return Response.noContent().build();
//
//        } catch (BLException ex) {
//            return responderError(ex);
//        }
//    }
//
//    @POST
//    @Path("/{id}/reenvio")
//    public Response reenviar(@PathParam("id") int id) {
//        try {
//            if (solicitudBL.findSolicitudById(id) == null) {
//                return error(
//                        Response.Status.NOT_FOUND,
//                        "Solicitud no encontrada"
//                );
//            }
//
//            solicitudBL.reenviarSolicitud(id);
//
//            return Response.noContent().build();
//
//        } catch (BLException ex) {
//            return responderError(ex);
//        }
//    }
//
//    private Response responderError(BLException ex) {
//        Throwable causa = ex;
//
//        while (causa != null) {
//            if (causa instanceof SQLException) {
//                return error(
//                        Response.Status.INTERNAL_SERVER_ERROR,
//                        "Error al acceder a la base de datos"
//                );
//            }
//            causa = causa.getCause();
//        }
//
//        // Errores de validación y estado de negocio.
//        return error(
//                Response.Status.BAD_REQUEST,
//                ex.getMessage()
//        );
//    }
//
//    private Response error(
//            Response.Status estado, String mensaje) {
//        return Response.status(estado)
//                .entity(Map.of("error", mensaje))
//                .build();
//    }
//}
