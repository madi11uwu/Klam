package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;

import java.sql.SQLException;

public interface PacienteParticularDAO extends  DAO<PacienteParticular,Integer>{
    PacienteParticular findByDNI(String DNI) throws SQLException;

    /** Login de clientes: devuelve el paciente con su username y passwordHash, o null. */
    pe.edu.pucp.klam.modelo.clientes.PacienteParticular findByUsername(String username) throws SQLException;

    /** Fija username y password_hash de un paciente. */
    void asignarCredenciales(int id, String username, String passwordHash) throws SQLException;
}
