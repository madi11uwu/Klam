package pe.edu.pucp.klam.rest.dto;

import pe.edu.pucp.klam.modelo.usuariosPermisos.Administrador;
import pe.edu.pucp.klam.modelo.usuariosPermisos.TecnicoInstrumentista;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;

public class UsuarioDTO {
    private Integer id;
    private String nombres;
    private String apellidos;
    private String nombreCompleto;
    private String username;
    private String email;
    private String rol;
    private boolean activo;
    private String especialidad;
    private Double comisionAcumulada;
    private String iniciales;
    private Integer idCliente;
    private String tipoCliente;

    public UsuarioDTO() {
    }

    public UsuarioDTO(UsuarioPlataforma u) {
        if (u != null) {
            this.id = u.getIdUsuario();
            this.nombres = u.getNombres();
            this.apellidos = u.getApellidos();
            this.nombreCompleto = u.getNombreCompleto();
            this.username = u.getUsername();
            this.email = u.getEmail();
            this.activo = u.isActivo();

            // Mapeo amigable de rol
            String r = u.getRol() != null ? u.getRol().toUpperCase() : "";
            if ("ADMINISTRADOR".equals(r)) {
                this.rol = "Administrador";
            } else if ("VENDEDOR".equals(r)) {
                this.rol = "Vendedor";
            } else if ("TECNICO_INSTRUMENTISTA".equals(r) || "INSTRUMENTISTA".equals(r)) {
                this.rol = "Instrumentista";
            } else if ("CLINICA_HOSPITAL".equals(r)) {
                this.rol = "Clínica / Hospital";
                this.tipoCliente = "CLINICA_HOSPITAL";
            } else if ("PACIENTE_PARTICULAR".equals(r)) {
                this.rol = "Paciente particular";
                this.tipoCliente = "PACIENTE_PARTICULAR";
            } else {
                this.rol = u.getRol();
            }
            if (u instanceof pe.edu.pucp.klam.modelo.usuariosPermisos.CuentaUsuario c && c.getCliente() != null) {
                this.idCliente = c.getCliente().getIdCliente();
            }

            if (u instanceof TecnicoInstrumentista t) {
                this.especialidad = t.getEspecialidad();
            } else if (u instanceof Vendedor v) {
                this.comisionAcumulada = v.getComisionAcumulada();
            }

            // Calcular iniciales para el avatar
            String n = u.getNombres() != null && !u.getNombres().isBlank() ? u.getNombres().trim() : "";
            String a = u.getApellidos() != null && !u.getApellidos().isBlank() ? u.getApellidos().trim() : "";
            if (!n.isEmpty() && !a.isEmpty()) {
                this.iniciales = ("" + n.charAt(0) + a.charAt(0)).toUpperCase();
            } else if (!n.isEmpty()) {
                this.iniciales = n.substring(0, Math.min(2, n.length())).toUpperCase();
            } else if (u.getUsername() != null && !u.getUsername().isEmpty()) {
                this.iniciales = u.getUsername().substring(0, Math.min(2, u.getUsername().length())).toUpperCase();
            } else {
                this.iniciales = "U";
            }
        }
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public Double getComisionAcumulada() {
        return comisionAcumulada;
    }

    public void setComisionAcumulada(Double comisionAcumulada) {
        this.comisionAcumulada = comisionAcumulada;
    }

    public String getIniciales() {
        return iniciales;
    }

    public void setIniciales(String iniciales) {
        this.iniciales = iniciales;
    }
}
