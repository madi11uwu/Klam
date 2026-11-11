package pe.edu.pucp.klam.modelo.usuariosPermisos;

import pe.edu.pucp.klam.modelo.clientes.Cliente;

/** Identidad de acceso independiente del registro comercial del cliente. */
public class CuentaUsuario extends UsuarioPlataforma {
    private Cliente cliente;

    public CuentaUsuario() { super(); }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
}
