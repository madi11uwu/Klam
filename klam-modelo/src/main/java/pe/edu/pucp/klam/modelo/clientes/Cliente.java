package pe.edu.pucp.klam.modelo.clientes;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;

public abstract class Cliente {
    private int id_cliente;
    private String nombre;
    private String direccion;
    private String emailContacto;
    private String telefono;
    private boolean activo; // true = Activo, false = Inactivo (Eliminación lógica)
    private List<Cirugia> cirugias;

    public Cliente(final Cliente cliente) {
        if (cliente==null){
            throw new IllegalArgumentException("Cliente no puede ser nulo");
        }
        setId_cliente(cliente.getId_cliente());
        setNombre(cliente.getNombre());
        setDireccion(cliente.getDireccion());
        setEmailContacto(cliente.getEmailContacto());
        setTelefono(cliente.getTelefono());
        setActivo(cliente.isActivo());
        setCirugias(cliente.getCirugias());
    }
    public Cliente (){activo=true;}
    public Cliente(int id, String nombre, String direccion, String emailContacto, String telefono) {
        this.id_cliente = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.emailContacto = emailContacto;
        this.telefono = telefono;
    }

    // Getters y Setters
    public int getId_cliente() { return id_cliente; }
    public void setId_cliente(int id_cliente) { this.id_cliente = id_cliente; }

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