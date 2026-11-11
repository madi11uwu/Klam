package pe.edu.pucp.klam.rest.dto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pe.edu.pucp.klam.modelo.comunicaciones.Notificacion;

// Datos de presentación de un aviso interno (RF12). El modelo no persiste un "tipo" de aviso,
// así que la categoría y las referencias se derivan del título que genera la capa de negocio.
public class NotificacionDTO {
    private static final Pattern CIRUGIA = Pattern.compile("cirug[ií]a.*?#?(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SOLICITUD = Pattern.compile("solicitud.*?(\\d+)", Pattern.CASE_INSENSITIVE);

    private int id;
    private String titulo;
    private String mensaje;
    private String fechaHora;
    private boolean leida;
    private String categoria;
    private Integer idCirugia;
    private Integer idSolicitud;
    private String destinatario;
    private String perfil;

    public NotificacionDTO() {
    }

    public NotificacionDTO(Notificacion notificacion, String destinatario, String perfil) {
        String tit = notificacion.getTitulo() == null ? "" : notificacion.getTitulo();
        Integer cirugia = extraerId(CIRUGIA, tit);
        Integer solicitud = extraerId(SOLICITUD, tit);

        this.id = notificacion.getIdNotificacion();
        this.titulo = tit;
        this.mensaje = notificacion.getMensaje() == null ? "" : notificacion.getMensaje();
        this.fechaHora = notificacion.getFechaHora() == null ? null : notificacion.getFechaHora().toString();
        this.leida = notificacion.isEstadoLeida();
        this.idCirugia = cirugia;
        this.idSolicitud = solicitud;
        this.categoria = cirugia != null ? "Nueva cirugía" : solicitud != null ? "Nueva solicitud" : "Aviso";
        this.destinatario = destinatario;
        this.perfil = perfil;
    }

    private static Integer extraerId(Pattern patron, String titulo) {
        Matcher m = patron.matcher(titulo);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Integer getIdCirugia() { return idCirugia; }
    public void setIdCirugia(Integer idCirugia) { this.idCirugia = idCirugia; }

    public Integer getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Integer idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
}
