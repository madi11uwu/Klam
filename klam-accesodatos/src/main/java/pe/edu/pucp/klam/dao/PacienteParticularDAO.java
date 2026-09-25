package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;

import java.sql.SQLException;

public interface PacienteParticularDAO extends  DAO<PacienteParticular,Integer>{
    PacienteParticular findByDNI(String DNI) throws SQLException;
}
