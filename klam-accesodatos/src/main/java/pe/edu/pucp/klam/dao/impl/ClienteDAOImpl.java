package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.modelo.clientes.Cliente;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class ClienteDAOImpl<T extends Cliente> {
    protected T mapear(ResultSet rs, T cliente) throws SQLException {
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setDireccion(rs.getString("direccion"));
        cliente.setEmailContacto(rs.getString("email_contacto"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setActivo(rs.getBoolean("activo"));
        return cliente;
    }

    /** Un username vacío se guarda como NULL (la columna es UNIQUE y admite varios NULL). */
    protected static String nullSiVacio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
