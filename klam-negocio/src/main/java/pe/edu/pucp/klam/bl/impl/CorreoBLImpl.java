package pe.edu.pucp.klam.bl.impl;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import pe.edu.pucp.klam.bl.CorreoBL;

public class CorreoBLImpl implements CorreoBL {

    private static final Logger LOG = Logger.getLogger(CorreoBLImpl.class.getName());

    // Un solo hilo en segundo plano: la respuesta REST no espera al servidor de correo
    private static final ExecutorService ENVIOS = Executors.newSingleThreadExecutor(tarea -> {
        Thread hilo = new Thread(tarea, "klam-correo");
        hilo.setDaemon(true);
        return hilo;
    });

    @Override
    public void enviar(String para, String asunto, String mensaje) {
        if (para == null || para.isBlank()) {
            return;
        }
        ENVIOS.submit(() -> enviarAhora(para.trim(), asunto, mensaje));
    }

    private void enviarAhora(String para, String asunto, String mensaje) {
        try {
            // Busca src/main/resources/correo.properties (como db.properties en DBManager)
            ResourceBundle correo = ResourceBundle.getBundle("correo");
            String host = correo.getString("correo.host").trim();
            String puerto = correo.getString("correo.puerto").trim();
            String usuario = correo.getString("correo.usuario").trim();
            String password = correo.getString("correo.password").trim();
            if (usuario.isEmpty() || password.isEmpty()) {
                LOG.info("Correo no configurado en correo.properties: no se envió el aviso a " + para);
                return;
            }

            Properties propiedades = new Properties();
            propiedades.put("mail.smtp.host", host);
            propiedades.put("mail.smtp.port", puerto);
            propiedades.put("mail.smtp.auth", "true");
            propiedades.put("mail.smtp.starttls.enable", "true");
            // Evita "Could not convert socket to TLS" con Gmail en JDK recientes
            propiedades.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            propiedades.put("mail.smtp.ssl.trust", host);
            propiedades.put("mail.smtp.connectiontimeout", "10000");
            propiedades.put("mail.smtp.timeout", "10000");

            Session sesion = Session.getInstance(propiedades, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(usuario, password);
                }
            });

            MimeMessage correoSalida = new MimeMessage(sesion);
            correoSalida.setFrom(new InternetAddress(usuario, "BIOKLAM"));
            correoSalida.setRecipients(Message.RecipientType.TO, InternetAddress.parse(para));
            correoSalida.setSubject(asunto, "UTF-8");
            correoSalida.setText(mensaje, "UTF-8");
            Transport.send(correoSalida);
            LOG.info("Correo enviado a " + para + ": " + asunto);
        } catch (MissingResourceException ex) {
            LOG.warning("Falta correo.properties o una de sus claves: no se envió el aviso a " + para);
        } catch (MessagingException | UnsupportedEncodingException ex) {
            // El mensaje de error de SMTP no incluye la contraseña
            LOG.log(Level.WARNING, "No se pudo enviar el correo a " + para + ": " + ex.getMessage());
        }
    }
}
