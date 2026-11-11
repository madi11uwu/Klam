package pe.edu.pucp.klam.dao;

import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;

import java.sql.SQLException;

public interface ClinicaHospitalDAO extends DAO<ClinicaHospital,Integer>{
    ClinicaHospital findByRUC(String ruc) throws SQLException;
}
