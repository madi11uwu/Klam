package pe.edu.pucp.klam.bl;

/**
 * Envío de correos automáticos (RF11; reutilizable, p. ej. para avisar credenciales en RF01).
 * La cuenta SMTP se configura en correo.properties, que no se sube al repositorio.
 */
public interface CorreoBL {

    /**
     * Envía un correo de texto en segundo plano. Nunca lanza excepción: si el correo no está
     * configurado o el servidor SMTP falla, solo queda registrado en el log de GlassFish.
     */
    void enviar(String para, String asunto, String mensaje);
}
