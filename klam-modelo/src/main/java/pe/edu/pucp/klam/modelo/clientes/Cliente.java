package pe.edu.pucp.klam.modelo.clientes;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;

public abstract class Cliente {
    protected String id_cliente;
    protected String nombre;
    protected String direccion;
    protected String emailContacto;
    protected String telefono;
    protected boolean activo; // true = Activo, false = Inactivo (Eliminación lógica)
    protected List<Cirugia> cirugias;

    public Cliente() {
        this.activo = true;
        this.cirugias = new ArrayList<>();
    }

    // Getters y Setters
    public String getId_cliente() { return id_cliente; }
    public void setId_cliente(String id_cliente) { this.id_cliente = id_cliente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEmailContacto() { return emailContacto; }
    public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /** Eliminacion logica: el cliente se conserva aunque se dé de baja. */
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    /** Cirugias del cliente. Se devuelve una copia para no exponer la lista. */
    public List<Cirugia> getCirugias() { return new ArrayList<>(cirugias); }

    public void setCirugias(List<Cirugia> cirugias) {
        this.cirugias = (cirugias == null) ? new ArrayList<>() : new ArrayList<>(cirugias);
    }

    public void agregarCirugia(Cirugia cirugia) {
        if (cirugia == null) {
            throw new IllegalArgumentException("La cirugia no puede ser nula");
        }
        this.cirugias.add(cirugia);
    }
}