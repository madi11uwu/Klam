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
}
