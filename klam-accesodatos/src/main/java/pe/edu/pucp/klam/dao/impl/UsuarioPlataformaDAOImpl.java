package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.modelo.usuariosPermisos.UsuarioPlataforma;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class UsuarioPlataformaDAOImpl<T extends UsuarioPlataforma> {

    protected T mapear(ResultSet rs, T usuario) throws SQLException {
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setEmail(rs.getString("email"));
        usuario.setNombres(rs.getString("nombres"));
        usuario.setApellidos(rs.getString("apellidos"));
        usuario.setRol(rs.getString("rol"));
        usuario.setActivo(rs.getBoolean("activo"));
        return usuario;
    }
}