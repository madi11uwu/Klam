package pe.edu.pucp.klam.modelo.clientes;

import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class Cliente {
    private String id_cliente;
    private String nombre;
    private String direccion;
    private String emailContacto;
    private String telefono;
    private String estado;
    private List<Cirugia> cirugias;

    public Cliente(){
        cirugias=new ArrayList<>();
    }
    public Cliente(final Cliente cliente){
        if(cliente==null){
            throw new IllegalArgumentException("Cliente no puede ser nulo");
        }
        setId_cliente(cliente.getId_cliente());
        setNombre(cliente.getNombre());
        setDireccion(cliente.getDireccion());
        setEmailContacto(cliente.getEmailContacto());
        setTelefono(cliente.getTelefono());
        setEstado(cliente.getEstado());
        setCirugias(cliente.getCirugias());
    }

    public List<Cirugia> getCirugias() {
        return Collections.unmodifiableList(cirugias);
    }

    public void setCirugias(List<Cirugia> cirugias) {
        this.cirugias = (cirugias !=null) ? new ArrayList<>(cirugias) : new ArrayList<>();
    }

    public String getId_cliente() {
        return id_cliente;
    }

    public void setId_cliente(String id_cliente) {
        if(id_cliente == null || id_cliente.isEmpty()){
            throw new IllegalArgumentException("id_cliente no puede ser nulo o vacío");
        }
        this.id_cliente = id_cliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre==null || nombre.isEmpty()){
            throw new IllegalArgumentException("nombre no puede ser nulo o vacío");
        }
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        if(direccion == null || direccion.isEmpty()){
            throw new IllegalArgumentException("direccion no puede ser nulo o vacío");
        }
        this.direccion = direccion;
    }

    public String getEmailContacto() {
        return emailContacto;
    }

    public void setEmailContacto(String emailContacto) {
        if(emailContacto==null || emailContacto.isEmpty()){
            throw new IllegalArgumentException("emailContacto no puede ser nulo o vacío");
        }
        this.emailContacto = emailContacto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        if(telefono==null || telefono.isEmpty()){
            throw new IllegalArgumentException("telefono no puede ser nulo o vacío");
        }
        this.telefono = telefono;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if(estado==null || estado.isEmpty()){
            throw new IllegalArgumentException("estado no puede ser nulo o vacío");
        }
        this.estado = estado;
    }
}
