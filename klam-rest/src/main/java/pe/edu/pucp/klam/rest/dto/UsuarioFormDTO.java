package pe.edu.pucp.klam.rest.dto;

public class UsuarioFormDTO {
    private Integer id;
    private String nombres;
    private String apellidos;
    private String username;
    private String email;
    private String rol;
    private Boolean activo;
    private String especialidad;
    private String password;
    private Double comisionAcumulada;

    public UsuarioFormDTO() {
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Double getComisionAcumulada() {
        return comisionAcumulada;
    }

    public void setComisionAcumulada(Double comisionAcumulada) {
        this.comisionAcumulada = comisionAcumulada;
    }
}
