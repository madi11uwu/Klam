package pe.edu.pucp.klam.rest;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Administrador;

class SesionesRestTest {
    @Test
    void tokenEsUnicoNoSePuedeAlterarYSePuedeRevocar() {
        Administrador admin = new Administrador();
        admin.setIdUsuario(1);
        admin.setUsername("admin.prueba");
        SesionesRest.Credencial primera = SesionesRest.emitir(admin);
        SesionesRest.Credencial segunda = SesionesRest.emitir(admin);

        assertNotEquals(primera.token(), segunda.token());
        assertEquals("ADMINISTRADOR",
                SesionesRest.validar("Bearer " + primera.token()).rol());
        assertNull(SesionesRest.validar("Bearer " + primera.token() + "alterado"));
        assertNull(SesionesRest.validar("Bearer 1:ADMINISTRADOR"));
        SesionesRest.revocar("Bearer " + primera.token());
        assertNull(SesionesRest.validar("Bearer " + primera.token()));
        assertNotNull(SesionesRest.validar("Bearer " + segunda.token()));
        SesionesRest.revocar("Bearer " + segunda.token());
    }

    @Test
    void tokenExpiradoSeRechaza() {
        Administrador admin = new Administrador();
        admin.setIdUsuario(1);
        admin.setUsername("admin.prueba");
        SesionesRest.Credencial credencial = SesionesRest.emitir(admin, Duration.ZERO);

        assertNull(SesionesRest.validar("Bearer " + credencial.token()));
    }
    @Test
    void tokenClienteConservaElVinculoSinConfundirCuentaConCliente() throws Exception {
        var paciente = new pe.edu.pucp.klam.modelo.clientes.PacienteParticular();
        paciente.setIdCliente(42);
        var cuenta = new pe.edu.pucp.klam.modelo.usuariosPermisos.CuentaUsuario();
        cuenta.setIdUsuario(17);
        cuenta.setUsername("paciente.laboratorio");
        cuenta.setRol("PACIENTE_PARTICULAR");
        cuenta.setCliente(paciente);
        var credencial = SesionesRest.emitir(cuenta);
        var sesion = SesionesRest.validar("Bearer " + credencial.token());
        assertEquals(17, sesion.idUsuario());
        assertEquals(42, sesion.idCliente());
        assertEquals("PACIENTE_PARTICULAR", sesion.tipoCliente());
        var metodo = SolicitudesClienteResource.class.getDeclaredMethod("clienteDe", SesionesRest.Sesion.class);
        metodo.setAccessible(true);
        var clienteEnSolicitud = (pe.edu.pucp.klam.modelo.clientes.Cliente) metodo.invoke(null, sesion);
        assertEquals(42, clienteEnSolicitud.getIdCliente());
        SesionesRest.revocar("Bearer " + credencial.token());

        var administrador = new Administrador();
        administrador.setIdUsuario(42);
        administrador.setUsername("admin.laboratorio");
        var tokenAdmin = SesionesRest.emitir(administrador);
        var sesionAdmin = SesionesRest.validar("Bearer " + tokenAdmin.token());
        assertNull(sesionAdmin.idCliente());
        SesionesRest.revocar("Bearer " + tokenAdmin.token());
    }
}
