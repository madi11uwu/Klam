package pe.edu.pucp.klam.dao.impl;
import pe.edu.pucp.klam.dao.PacienteParticularDAO;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteParticularDAOImpl implements PacienteParticularDAO {
        @Override
        public List<PacienteParticular> findAll()
                throws SQLException {

            String sql =
                    "{call listar_pacientes_particulares()}";

            try (
                    Connection conn = DBManager.getInstance().getConnection();
                    CallableStatement cmd = conn.prepareCall(sql);
                    ResultSet rs = cmd.executeQuery()
            ) {

                List<PacienteParticular> pacientes = new ArrayList<>();
                while (rs.next()) {
                    pacientes.add(mapear(rs,new PacienteParticular()));
                }
                return pacientes;
            }
        }

        @Override
        public PacienteParticular findById(Integer id)
                throws SQLException {

            if (id == null) {
                throw new IllegalArgumentException(
                        "El id no puede ser nulo"
                );
            }

            String sql = "{call buscar_paciente_particular_por_id(?)}";

            try (
                    Connection conn =
                            DBManager.getInstance().getConnection();

                    CallableStatement cmd =
                            conn.prepareCall(sql)
            ) {

                cmd.setInt("p_id", id);

                try (ResultSet rs = cmd.executeQuery()) {

                    return rs.next()
                            ? mapear(rs,new  PacienteParticular())
                            : null;
                }
            }
        }

        @Override
        public PacienteParticular findByDNI(String dni)
                throws SQLException {

            if (dni == null) {
                throw new IllegalArgumentException(
                        "El DNI no puede ser nulo"
                );
            }

            String sql =
                    "{call buscar_paciente_particular_por_dni(?)}";

            try (
                    Connection conn =
                            DBManager.getInstance().getConnection();

                    CallableStatement cmd =
                            conn.prepareCall(sql)
            ) {

                cmd.setString("p_dni", dni);

                try (ResultSet rs = cmd.executeQuery()) {

                    return rs.next()
                            ? mapear(rs,new PacienteParticular())
                            : null;
                }
            }
        }

        @Override
        public void insert(PacienteParticular paciente)
                throws SQLException {

            if (paciente == null) {
                throw new IllegalArgumentException(
                        "El paciente no puede ser nulo"
                );
            }

            String sql =
                    "{call insertar_paciente_particular(" +
                            "?, ?, ?, ?, ?, ?, ?, ?)}";

            try (
                    Connection conn =
                            DBManager.getInstance().getConnection();

                    CallableStatement cmd =
                            conn.prepareCall(sql)
            ) {

                cmd.setString(
                        "p_nombre",
                        paciente.getNombre()
                );

                cmd.setString(
                        "p_direccion",
                        paciente.getDireccion()
                );

                cmd.setString(
                        "p_email_contacto",
                        paciente.getEmailContacto()
                );

                cmd.setString(
                        "p_telefono",
                        paciente.getTelefono()
                );

                cmd.setBoolean(
                        "p_activo",
                        paciente.isActivo()
                );

                cmd.setString(
                        "p_dni",
                        paciente.getDni()
                );

                cmd.setBoolean(
                        "p_pago_confirmado",
                        paciente.isPagoConfirmado()
                );

                cmd.registerOutParameter(
                        "p_id",
                        Types.INTEGER
                );

                if (cmd.executeUpdate() == 0) {
                    throw new SQLException(
                            "No se pudo insertar el paciente"
                    );
                }

                paciente.setId_cliente(
                        cmd.getInt("p_id")
                );
            }
        }

        @Override
        public void update(PacienteParticular paciente)
                throws SQLException {

            if (paciente == null) {
                throw new IllegalArgumentException(
                        "El paciente no puede ser nulo"
                );
            }

            String sql =
                    "{call modificar_paciente_particular(" +
                            "?, ?, ?, ?, ?, ?, ?, ?)}";

            try (
                    Connection conn =
                            DBManager.getInstance().getConnection();

                    CallableStatement cmd =
                            conn.prepareCall(sql)
            ) {

                cmd.setString(
                        "p_nombre",
                        paciente.getNombre()
                );

                cmd.setString(
                        "p_direccion",
                        paciente.getDireccion()
                );

                cmd.setString(
                        "p_email_contacto",
                        paciente.getEmailContacto()
                );

                cmd.setString(
                        "p_telefono",
                        paciente.getTelefono()
                );

                cmd.setBoolean(
                        "p_activo",
                        paciente.isActivo()
                );

                cmd.setString(
                        "p_dni",
                        paciente.getDni()
                );

                cmd.setBoolean(
                        "p_pago_confirmado",
                        paciente.isPagoConfirmado()
                );

                cmd.setInt(
                        "p_id",
                        paciente.getId_cliente()
                );

                if (cmd.executeUpdate() == 0) {
                    throw new SQLException(
                            "No se pudo actualizar el paciente"
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
                    "{call eliminar_paciente_particular(?)}";

            try (
                    Connection conn =
                            DBManager.getInstance().getConnection();

                    CallableStatement cmd =
                            conn.prepareCall(sql)
            ) {

                cmd.setInt("p_id", id);

                if (cmd.executeUpdate() == 0) {
                    throw new SQLException(
                            "No se pudo eliminar el paciente"
                    );
                }
            }
        }

        private PacienteParticular mapear(ResultSet rs,PacienteParticular paciente)
                throws SQLException {

            paciente.setId_cliente(
                    rs.getInt("id_cliente")
            );

            paciente.setNombre(
                    rs.getString("nombre")
            );

            paciente.setDireccion(
                    rs.getString("direccion")
            );

            paciente.setEmailContacto(
                    rs.getString("email_contacto")
            );

            paciente.setTelefono(
                    rs.getString("telefono")
            );

            paciente.setActivo(
                    rs.getBoolean("activo")
            );

            paciente.setDni(
                    rs.getString("dni")
            );

            paciente.setPagoConfirmado(
                    rs.getBoolean("pago_confirmado")
            );
            return paciente;
        }
}

