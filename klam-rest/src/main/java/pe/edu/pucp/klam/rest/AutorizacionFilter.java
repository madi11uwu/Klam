package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.util.Arrays;

/** Verifica token y rol en los endpoints marcados con @RequiereRol: 401 sin sesión, 403 sin permiso. */
@Provider
@RequiereRol
public class AutorizacionFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext contexto) {
        SesionesActivas.Sesion sesion = SesionesActivas.desdeEncabezado(contexto.getHeaderString("Authorization"));
        if (sesion == null) {
            contexto.abortWith(Errores.error(Response.Status.UNAUTHORIZED,
                    "Debe iniciar sesión para realizar esta acción"));
            return;
        }
        RequiereRol exigido = resourceInfo.getResourceMethod().getAnnotation(RequiereRol.class);
        if (exigido == null) {
            exigido = resourceInfo.getResourceClass().getAnnotation(RequiereRol.class);
        }
        if (exigido != null && exigido.value().length > 0
                && !Arrays.asList(exigido.value()).contains(sesion.rol())) {
            contexto.abortWith(Errores.error(Response.Status.FORBIDDEN,
                    "Su perfil no tiene permiso para realizar esta acción"));
        }
    }
}
