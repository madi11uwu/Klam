package pe.edu.pucp.klam.modelo.clientes;

import java.util.ArrayList;
import java.util.List;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;

/**
 * Todo cliente es un usuario de la plataforma (RF01): tiene username y contraseña.
 * Los datos comunes (id, nombre, email, activo) viven en UsuarioPlataforma; los getters/setters
 * históricos (getIdCliente, getNombre, getEmailContacto...) se conservan como alias para no
 * romper BL, DAO ni pruebas. {@code activo}: true = Activo, false = Inactivo (eliminación lógica).
 */
public abstract class Cliente extends UsuarioPlataforma {
    private String direccion;
    private String telefono;
    private List<Cirugia> cirugias = new ArrayList<>();

    public Cliente(final Cliente cliente) {
        super(requerir(cliente));
        setDireccion(cliente.getDireccion());
        setTelefono(cliente.getTelefono());
        setCirugias(cliente.getCirugias());
    }

    public Cliente() {
        super();
        setActivo(true);
    }

    public Cliente(int id, String nombre, String direccion, String emailContacto, String telefono) {
        super();
        setIdCliente(id);
        setNombre(nombre);
        this.direccion = direccion;
        setEmailContacto(emailContacto);
        this.telefono = telefono;
    }

    private static Cliente requerir(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no puede ser nulo");
        }
        return cliente;
    }

    // ---- Alias de los campos que ahora pertenecen a UsuarioPlataforma ----
    public int getIdCliente() { return getIdUsuario(); }
    public void setIdCliente(int idCliente) { setIdUsuario(idCliente); }

    /** Razón social o nombre del paciente (se guarda en "nombres"; "apellidos" queda vacío). */
    public String getNombre() { return getNombres(); }
    public void setNombre(String nombre) { setNombres(nombre); }

    public String getEmailContacto() { return getEmail(); }
    public void setEmailContacto(String emailContacto) { setEmail(emailContacto); }

    // ---- Datos propios del cliente ----
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

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
