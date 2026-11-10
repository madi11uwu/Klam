package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;

import java.sql.SQLException;

public interface ClinicaHospitalDAO extends DAO<ClinicaHospital,Integer>{
    ClinicaHospital findByRUC(String ruc) throws SQLException;

    /** Login de clientes: devuelve la clínica con su username y passwordHash, o null. */
    ClinicaHospital findByUsername(String username) throws SQLException;

    /** Fija username y password_hash de una clínica. */
    void asignarCredenciales(int id, String username, String passwordHash) throws SQLException;
}
