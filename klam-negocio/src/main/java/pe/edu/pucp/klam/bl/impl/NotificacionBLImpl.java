package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.NotificacionBL;
import pe.edu.pucp.klam.dao.AdministradorDAO;
import pe.edu.pucp.klam.dao.NotificacionDAO;
import pe.edu.pucp.klam.dao.TecnicoInstrumentistaDAO;
import pe.edu.pucp.klam.dao.impl.AdministradorDAOImpl;
import pe.edu.pucp.klam.dao.impl.NotificacionDAOImpl;
import pe.edu.pucp.klam.dao.impl.TecnicoInstrumentistaDAOImpl;
import pe.edu.pucp.klam.modelo.comunicaciones.Notificacion;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Administrador;
import pe.edu.pucp.klam.modelo.usuariosPermisos.TecnicoInstrumentista;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificacionBLImpl implements NotificacionBL {

    private static final Logger LOG = Logger.getLogger(NotificacionBLImpl.class.getName());
    private static final int TIMEOUT_SMTP_MS = 10_000;

    private final NotificacionDAO notificacionDAO = new NotificacionDAOImpl();
    private final AdministradorDAO administradorDAO = new AdministradorDAOImpl();
    private final TecnicoInstrumentistaDAO tecnicoDAO = new TecnicoInstrumentistaDAOImpl();

    @Override
    public List findAll() throws BLException {
        try {
            return notificacionDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las notificaciones", e);
        }
    }

    @Override
    public Notificacion findById(Integer id) throws BLException {
        if (id == null) throw new BLException("El ID no puede ser nulo");
        try {
            return notificacionDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la notificación con ID: " + id, e);
        }
    }

    @Override
    public void insert(Notificacion notificacion) throws BLException {
        validarDatos(notificacion);
        try {
            notificacionDAO.insert(notificacion);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la notificación", e);
        }
    }

    @Override
    public void update(Notificacion notificacion) throws BLException {
        validarDatos(notificacion);
        validarExiste(notificacion.getIdNotificacion());
        try {
            notificacionDAO.update(notificacion);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la notificación", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarExiste(id);
        try {
            notificacionDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar la notificación", e);
        }
    }

    @Override
    public void marcarLeida(Integer id, boolean leida) throws BLException {
        validarExiste(id);
        try {
            notificacionDAO.marcarLeida(id, leida);
        } catch (SQLException e) {
            throw new BLException("No se pudo cambiar el estado de lectura", e);
        }
    }

    @Override
    public List listarPorDestinatario(Integer idAdministrador, Integer idVendedor, Integer idTecnico) throws BLException {
        if (idAdministrador == null && idVendedor == null && idTecnico == null) {
            throw new BLException("Debe especificar al menos un destinatario");
        }
        try {
            return notificacionDAO.listarPorDestinatario(idAdministrador, idVendedor, idTecnico);
        } catch (SQLException e) {
            throw new BLException("No se pudieron listar las notificaciones", e);
        }
    }

    @Override
    public List listarNoLeidas() throws BLException {
        try {
            return notificacionDAO.listarNoLeidas();
        } catch (SQLException e) {
            throw new BLException("No se pudieron listar las notificaciones no leídas", e);
        }
    }

    private void validarDatos(Notificacion notificacion) throws BLException {
        if (notificacion == null) {
            throw new BLException("La notificación no puede ser nula");
        }
        if (notificacion.getTitulo() == null || notificacion.getTitulo().isBlank()) {
            throw new BLException("El título de la notificación es obligatorio");
        }
        if (notificacion.getMensaje() == null || notificacion.getMensaje().isBlank()) {
            throw new BLException("El mensaje de la notificación es obligatorio");
        }
        if (notificacion.getFechaHora() == null) {
            throw new BLException("La fecha y hora de la notificación son obligatorias");
        }
        if (notificacion.getDestinatario() == null || notificacion.getDestinatario().getIdUsuario() <= 0) {
            throw new BLException("El destinatario de la notificación es inválido u obligatorio");
        }
    }

    private void validarExiste(Integer id) throws BLException {
        if (id == null) throw new BLException("El ID no puede ser nulo");
        try {
            if (notificacionDAO.findById(id) == null) {
                throw new BLException("No existe una notificación con el ID " + id);
            }
        } catch (SQLException e) {
            throw new BLException("Error al verificar la notificación", e);
        }
    }

    // ------------------------------------------------------------------
    // Notificaciones automaticas internas
    // ------------------------------------------------------------------
    @Override
    public void notificarAdministradores(String titulo, String mensaje) throws BLException {
        try {
            List<Administrador> administradores = administradorDAO.findAll();
            notificarA(new ArrayList<UsuarioPlataforma>(administradores), titulo, mensaje);
        } catch (SQLException e) {
            throw new BLException("No se pudo notificar a los administradores", e);
        }
    }

    @Override
    public void notificarTecnicos(String titulo, String mensaje) throws BLException {
        try {
            List<TecnicoInstrumentista> tecnicos = tecnicoDAO.findAll();
            notificarA(new ArrayList<UsuarioPlataforma>(tecnicos), titulo, mensaje);
        } catch (SQLException e) {
            throw new BLException("No se pudo notificar a los técnicos instrumentistas", e);
        }
    }

    // Una notificacion por destinatario. Si hay una transaccion activa, se une a ella.
    private void notificarA(List<UsuarioPlataforma> destinatarios, String titulo, String mensaje)
            throws BLException, SQLException {
        for (UsuarioPlataforma destinatario : destinatarios) {
            if (!destinatario.isActivo()) {
                continue;
            }
            Notificacion n = new Notificacion();
            n.setFechaHora(LocalDateTime.now());
            n.setTitulo(recortar(titulo, 150));
            n.setMensaje(recortar(mensaje, 500));
            n.setEstadoLeida(false);
            n.setDestinatario(destinatario);
            validarDatos(n);
            notificacionDAO.insert(n);
        }
    }

    private String recortar(String texto, int maximo) {
        if (texto == null || texto.length() <= maximo) {
            return texto;
        }
        return texto.substring(0, maximo - 3) + "...";
    }

    // ------------------------------------------------------------------
    // Correo electronico (RF11)
    //
    // Configuracion (propiedad de sistema -Dklam.smtp.host=... o variable de entorno):
    //   klam.smtp.host / KLAM_SMTP_HOST            servidor SMTP (si falta, el correo queda desactivado)
    //   klam.smtp.port / KLAM_SMTP_PORT            puerto (por defecto 587)
    //   klam.smtp.user / KLAM_SMTP_USER            usuario (opcional)
    //   klam.smtp.password / KLAM_SMTP_PASSWORD    contrasena (opcional; nunca va en Git)
    //   klam.smtp.from / KLAM_SMTP_FROM            remitente (por defecto, el usuario)
    //   klam.smtp.starttls / KLAM_SMTP_STARTTLS    true (defecto) o false
    //   klam.smtp.ssl / KLAM_SMTP_SSL              true para TLS implicito (puerto 465)
    // ------------------------------------------------------------------
    @Override
    public boolean correoConfigurado() {
        return configuracion("host") != null;
    }

    @Override
    public boolean enviarCorreoAdministradores(String asunto, String cuerpo) throws BLException {
        if (!correoConfigurado()) {
            LOG.info("Correo no configurado (KLAM_SMTP_HOST): solo se genera la notificación interna");
            return false;
        }
        if (asunto == null || asunto.isBlank() || cuerpo == null || cuerpo.isBlank()) {
            throw new BLException("El asunto y el cuerpo del correo son obligatorios");
        }

        List<String> destinatarios = new ArrayList<>();
        try {
            for (Administrador a : administradorDAO.findAll()) {
                if (a.isActivo() && a.getEmail() != null && !a.getEmail().isBlank()) {
                    destinatarios.add(a.getEmail().trim());
                }
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo obtener los correos de los administradores", e);
        }
        if (destinatarios.isEmpty()) {
            throw new BLException("No hay administradores activos con correo registrado");
        }

        String host = configuracion("host");
        String usuario = configuracion("user");
        String remitente = configuracion("from") != null ? configuracion("from") : usuario;
        if (remitente == null) {
            throw new BLException("Configure el remitente del correo (KLAM_SMTP_FROM o KLAM_SMTP_USER)");
        }
        int puerto;
        try {
            puerto = configuracion("port") != null ? Integer.parseInt(configuracion("port")) : 587;
        } catch (NumberFormatException e) {
            throw new BLException("El puerto SMTP configurado no es válido", e);
        }
        boolean ssl = "true".equalsIgnoreCase(configuracion("ssl"));
        boolean starttls = !"false".equalsIgnoreCase(configuracion("starttls"));

        try {
            enviarPorSmtp(host, puerto, ssl, starttls, usuario, configuracion("password"),
                    remitente, destinatarios, asunto, cuerpo);
            return true;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "No se pudo enviar el correo a los administradores", e);
            throw new BLException("No se pudo enviar el correo: " + e.getMessage(), e);
        }
    }

    // Propiedad de sistema (klam.smtp.x) o, si no existe, variable de entorno (KLAM_SMTP_X)
    private String configuracion(String clave) {
        String valor = System.getProperty("klam.smtp." + clave);
        if (valor == null || valor.isBlank()) {
            valor = System.getenv("KLAM_SMTP_" + clave.toUpperCase());
        }
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    // Cliente SMTP minimo (sin librerias externas): EHLO, STARTTLS, AUTH LOGIN, MAIL, RCPT, DATA
    private void enviarPorSmtp(String host, int puerto, boolean ssl, boolean starttls, String usuario,
                               String password, String remitente, List<String> destinatarios,
                               String asunto, String cuerpo) throws IOException {
        Socket socket = ssl
                ? SSLSocketFactory.getDefault().createSocket(host, puerto)
                : new Socket();
        try {
            if (!ssl) {
                socket.connect(new InetSocketAddress(host, puerto), TIMEOUT_SMTP_MS);
            }
            socket.setSoTimeout(TIMEOUT_SMTP_MS);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            esperarCodigo(in, "220");

            String capacidades = comando(in, out, "EHLO klam", "250");
            if (!ssl && starttls) {
                if (!capacidades.toUpperCase().contains("STARTTLS")) {
                    throw new IOException("El servidor SMTP no soporta STARTTLS");
                }
                comando(in, out, "STARTTLS", "220");
                SSLSocket seguro = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault())
                        .createSocket(socket, host, puerto, true);
                seguro.setSoTimeout(TIMEOUT_SMTP_MS);
                seguro.startHandshake();
                socket = seguro;
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                comando(in, out, "EHLO klam", "250");
            }

            if (usuario != null && password != null) {
                comando(in, out, "AUTH LOGIN", "334");
                comando(in, out, base64(usuario), "334");
                comando(in, out, base64(password), "235");
            }

            comando(in, out, "MAIL FROM:<" + direccionSegura(remitente) + ">", "250");
            for (String destinatario : destinatarios) {
                comando(in, out, "RCPT TO:<" + direccionSegura(destinatario) + ">", "250");
            }
            comando(in, out, "DATA", "354");

            StringBuilder mensaje = new StringBuilder();
            mensaje.append("From: ").append(direccionSegura(remitente)).append("\r\n");
            mensaje.append("To: ").append(String.join(", ", destinatarios.stream().map(this::direccionSegura).toList())).append("\r\n");
            mensaje.append("Subject: =?UTF-8?B?").append(base64(asunto.replaceAll("[\\r\\n]+", " "))).append("?=\r\n");
            mensaje.append("MIME-Version: 1.0\r\n");
            mensaje.append("Content-Type: text/plain; charset=UTF-8\r\n");
            mensaje.append("Content-Transfer-Encoding: base64\r\n\r\n");
            mensaje.append(new String(Base64.getMimeEncoder(76, "\r\n".getBytes(StandardCharsets.US_ASCII))
                    .encode(cuerpo.getBytes(StandardCharsets.UTF_8)), StandardCharsets.US_ASCII));
            mensaje.append("\r\n.\r\n");
            out.write(mensaje.toString());
            out.flush();
            esperarCodigo(in, "250");

            comando(in, out, "QUIT", "221");
        } finally {
            socket.close();
        }
    }

    private String comando(BufferedReader in, BufferedWriter out, String comando, String codigoEsperado) throws IOException {
        out.write(comando + "\r\n");
        out.flush();
        return esperarCodigo(in, codigoEsperado);
    }

    // Lee una respuesta SMTP (puede tener varias lineas "250-...") y comprueba su codigo
    private String esperarCodigo(BufferedReader in, String codigoEsperado) throws IOException {
        StringBuilder respuesta = new StringBuilder();
        String linea;
        while ((linea = in.readLine()) != null) {
            respuesta.append(linea).append('\n');
            if (linea.length() < 4 || linea.charAt(3) != '-') {
                break;
            }
        }
        if (respuesta.length() == 0) {
            throw new IOException("El servidor SMTP cerró la conexión");
        }
        if (!respuesta.toString().startsWith(codigoEsperado)) {
            throw new IOException("Respuesta SMTP inesperada (se esperaba " + codigoEsperado + "): "
                    + respuesta.toString().trim());
        }
        return respuesta.toString();
    }

    private String base64(String texto) {
        return Base64.getEncoder().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    // Evita inyeccion de cabeceras o comandos SMTP
    private String direccionSegura(String direccion) {
        return direccion.replaceAll("[\\r\\n<>]", "");
    }
}
