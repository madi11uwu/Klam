package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.dao.ClinicaHospitalDAO;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClinicaHospitalDAOImpl implements ClinicaHospitalDAO {
    @Override
    public List<ClinicaHospital> findAll()
            throws SQLException {

        String sql =
                "{call listar_clinicas_hospitales()}";

        try (
                Connection conn =
                        DBManager.getInstance().getConnection();

                CallableStatement cmd =
                        conn.prepareCall(sql);

                ResultSet rs =
                        cmd.executeQuery()
        ) {

            List<ClinicaHospital> clinicas =
                    new ArrayList<>();

            while (rs.next()) {
                clinicas.add(mapear(rs,new ClinicaHospital()));
            }

            return clinicas;
        }
    }

    @Override
    public ClinicaHospital findById(Integer id)
            throws SQLException {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El id no puede ser nulo"
            );
        }

        String sql ="{call buscar_clinica_hospital_por_id(?)}";

        try (
                Connection conn =DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)
        ) {

            cmd.setInt("p_id", id);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs,new ClinicaHospital()) : null;
            }
        }
    }

    @Override
    public ClinicaHospital findByRUC(String ruc)
            throws SQLException {

        if (ruc == null) {
            throw new IllegalArgumentException(
                    "El RUC no puede ser nulo"
            );
        }

        String sql =
                "{call buscar_clinica_hospital_por_ruc(?)}";

        try (
                Connection conn =DBManager.getInstance().getConnection();
                CallableStatement cmd =conn.prepareCall(sql)
        ) {

            cmd.setString("p_ruc", ruc);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs,new ClinicaHospital()) : null;
            }
        }
    }

    @Override
    public void insert(ClinicaHospital clinica)
            throws SQLException {

        if (clinica == null) {
            throw new IllegalArgumentException(
                    "La clínica no puede ser nula"
            );
        }

        String sql ="{call insertar_clinica_hospital(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (
                Connection conn =DBManager.getInstance().getConnection();
                CallableStatement cmd =conn.prepareCall(sql)
        ) {
            cmd.setString(
                    "p_nombre",
                    clinica.getNombre()
            );
            cmd.setString(
                    "p_direccion",
                    clinica.getDireccion()
            );
            cmd.setString(
                    "p_email_contacto",
                    clinica.getEmailContacto()
            );
            cmd.setString("p_telefono",clinica.getTelefono());
            cmd.setBoolean("p_activo",clinica.isActivo());
            cmd.setString("p_ruc",clinica.getRuc());
            cmd.setBoolean("p_tiene_consignacion",clinica.isTieneConsignacion());
            cmd.setString("p_periodo_credito",clinica.getPeriodoCredito());
            cmd.registerOutParameter("p_id", Types.INTEGER);
            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo insertar la clínica"
                );
            }
            clinica.setId_cliente(cmd.getInt("p_id"));
        }
    }

    @Override
    public void update(ClinicaHospital clinica)
            throws SQLException {

        if (clinica == null) {
            throw new IllegalArgumentException(
                    "La clínica no puede ser nula"
            );
        }

        String sql =
                "{call modificar_clinica_hospital(" +
                        "?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (
                Connection conn =
                        DBManager.getInstance().getConnection();

                CallableStatement cmd =
                        conn.prepareCall(sql)
        ) {

            cmd.setString(
                    "p_nombre",
                    clinica.getNombre()
            );

            cmd.setString(
                    "p_direccion",
                    clinica.getDireccion()
            );

            cmd.setString(
                    "p_email_contacto",
                    clinica.getEmailContacto()
            );

            cmd.setString(
                    "p_telefono",
                    clinica.getTelefono()
            );

            cmd.setBoolean(
                    "p_activo",
                    clinica.isActivo()
            );

            cmd.setString(
                    "p_ruc",
                    clinica.getRuc()
            );

            cmd.setBoolean(
                    "p_tiene_consignacion",
                    clinica.isTieneConsignacion()
            );

            cmd.setString(
                    "p_periodo_credito",
                    clinica.getPeriodoCredito()
            );

            cmd.setInt(
                    "p_id",
                    clinica.getId_cliente()
            );

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo actualizar la clínica"
                );
            }
        }
    }

    @Override
    public void delete(Integer id)
            throws SQLException {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El id no puede ser nulo"
            );
        }

        String sql =
                "{call eliminar_clinica_hospital(?)}";

        try (
                Connection conn =
                        DBManager.getInstance().getConnection();

                CallableStatement cmd =
                        conn.prepareCall(sql)
        ) {

            cmd.setInt("p_id", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo eliminar la clínica"
                );
            }
        }
    }

    private ClinicaHospital mapear(ResultSet rs,ClinicaHospital clinica)
            throws SQLException {

        clinica.setId_cliente(
                rs.getInt("id_cliente")
        );

        clinica.setNombre(
                rs.getString("nombre")
        );

        clinica.setDireccion(
                rs.getString("direccion")
        );

        clinica.setEmailContacto(
                rs.getString("email_contacto")
        );

        clinica.setTelefono(
                rs.getString("telefono")
        );

        clinica.setActivo(
                rs.getBoolean("activo")
        );

        clinica.setRuc(
                rs.getString("ruc")
        );

        clinica.setTieneConsignacion(
                rs.getBoolean("tiene_consignacion")
        );

        clinica.setPeriodoCredito(
                rs.getString("periodo_credito")
        );

        return clinica;
    }
}
