package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.dao.CirugiaDAO;
import pe.edu.pucp.klam.dao.impl.inventario.BandejaInstrumentalDAOImpl;
import pe.edu.pucp.klam.dao.impl.inventario.EquipoDAOImpl;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CirugiaDAOImpl implements CirugiaDAO {
    @Override
    public List<Cirugia> findAll() throws SQLException {
        String sql = "{call listar_cirugias()}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql);
            ResultSet rs = cmd.executeQuery()) {

            List<Cirugia> cirugias = new ArrayList<>();
            while (rs.next()) {
                cirugias.add(mapear(rs, new Cirugia()));
            }
            return cirugias;
        }
    }

    @Override
    public Cirugia findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_cirugia_por_id(?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Cirugia()) : null;
            }
        }
    }

    @Override
    public List<Cirugia> findByEstado(EstadoCirugia estado) throws SQLException {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }

        String sql = "{call listar_cirugias_por_estado(?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setString("p_estado", estado.name());

            try (ResultSet rs = cmd.executeQuery()) {
                List<Cirugia> cirugias = new ArrayList<>();
                while (rs.next()) {
                    cirugias.add(mapear(rs, new Cirugia()));
                }
                return cirugias;
            }
        }
    }

    @Override
    public List<Cirugia> findByRangoFechas(LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("El rango de fechas no puede ser nulo");
        }

        String sql = "{call listar_cirugias_por_rango_fechas(?, ?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setTimestamp("p_desde", Timestamp.valueOf(desde));
            cmd.setTimestamp("p_hasta", Timestamp.valueOf(hasta));

            try (ResultSet rs = cmd.executeQuery()) {
                List<Cirugia> cirugias = new ArrayList<>();
                while (rs.next()) {
                    cirugias.add(mapear(rs, new Cirugia()));
                }
                return cirugias;
            }
        }
    }

    @Override
    public void insert(Cirugia cirugia) throws SQLException {
        if (cirugia == null) {
            throw new IllegalArgumentException("La cirugia no puede ser nula");
        }

        String sql = "{call insertar_cirugia(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setTimestamp("p_fecha_hora_inicio", Timestamp.valueOf(cirugia.getFechaHoraInicio()));
            if (cirugia.getFechaHoraFin() != null) {
                cmd.setTimestamp("p_fecha_hora_fin", Timestamp.valueOf(cirugia.getFechaHoraFin()));
            } else {
                cmd.setNull("p_fecha_hora_fin", Types.TIMESTAMP);
            }
            cmd.setString("p_tipo_procedimiento", cirugia.getTipoProcedimiento());
            cmd.setString("p_doctor_nombre", cirugia.getDoctorNombre());
            cmd.setString("p_motivo_cancelacion", cirugia.getMotivoCancelacion());
            cmd.setString("p_estado", cirugia.getEstado().name());

            if (cirugia.getEquipo() != null) {
                cmd.setInt("p_id_equipo", cirugia.getEquipo().getIdEquipo());
            } else {
                cmd.setNull("p_id_equipo", Types.INTEGER);
            }

            if (cirugia.getBandejaInstrumental() != null) {
                cmd.setInt("p_id_bandeja", cirugia.getBandejaInstrumental().getIdBandeja());
            } else {
                cmd.setNull("p_id_bandeja", Types.INTEGER);
            }

            asignarCliente(cmd, cirugia);
            cmd.setBoolean("p_activo", cirugia.isActivo());
            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la cirugia");
            }

            cirugia.setIdCirugia(cmd.getInt("p_id"));
        }
    }

    @Override
    public void update(Cirugia cirugia) throws SQLException {
        if (cirugia == null) {
            throw new IllegalArgumentException("La cirugia no puede ser nula");
        }

        String sql = "{call modificar_cirugia(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setTimestamp("p_fecha_hora_inicio", Timestamp.valueOf(cirugia.getFechaHoraInicio()));
            if (cirugia.getFechaHoraFin() != null) {
                cmd.setTimestamp("p_fecha_hora_fin", Timestamp.valueOf(cirugia.getFechaHoraFin()));
            } else {
                cmd.setNull("p_fecha_hora_fin", Types.TIMESTAMP);
            }
            cmd.setString("p_tipo_procedimiento", cirugia.getTipoProcedimiento());
            cmd.setString("p_doctor_nombre", cirugia.getDoctorNombre());
            cmd.setString("p_motivo_cancelacion", cirugia.getMotivoCancelacion());
            cmd.setString("p_estado", cirugia.getEstado().name());

            if (cirugia.getEquipo() != null) {
                cmd.setInt("p_id_equipo", cirugia.getEquipo().getIdEquipo());
            } else {
                cmd.setNull("p_id_equipo", Types.INTEGER);
            }

            if (cirugia.getBandejaInstrumental() != null) {
                cmd.setInt("p_id_bandeja", cirugia.getBandejaInstrumental().getIdBandeja());
            } else {
                cmd.setNull("p_id_bandeja", Types.INTEGER);
            }

            asignarCliente(cmd, cirugia);
            cmd.setBoolean("p_activo", cirugia.isActivo());
            cmd.setInt("p_id", cirugia.getIdCirugia());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la cirugia");
            }
        }
    }

    @Override
    public void cancelar(Integer id, String motivo) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call cancelar_cirugia(?, ?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);
            cmd.setString("p_motivo_cancelacion", motivo);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo cancelar la cirugia");
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call eliminar_cirugia(?)}";
        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar la cirugia");
            }
        }
    }

    protected Cirugia mapear(ResultSet rs, Cirugia cirugia) throws SQLException {
        cirugia.setIdCirugia(rs.getInt("id_cirugia"));
        cirugia.setFechaHoraInicio(rs.getTimestamp("fecha_hora_inicio").toLocalDateTime());

        Timestamp fechaHoraFin = rs.getTimestamp("fecha_hora_fin");
        cirugia.setFechaHoraFin(fechaHoraFin != null ? fechaHoraFin.toLocalDateTime() : null);

        cirugia.setTipoProcedimiento(rs.getString("tipo_procedimiento"));
        cirugia.setDoctorNombre(rs.getString("doctor_nombre"));
        cirugia.setMotivoCancelacion(rs.getString("motivo_cancelacion"));
        cirugia.setEstado(Enum.valueOf(EstadoCirugia.class, rs.getString("estado")));
        cirugia.setActivo(rs.getBoolean("activo"));

        mapearEquipo(rs, cirugia);
        mapearBandeja(rs, cirugia);
        mapearCliente(rs, cirugia);

        return cirugia;
    }

    // En la BD el cliente se guarda en una de dos columnas segun su tipo concreto
    private void asignarCliente(CallableStatement cmd, Cirugia cirugia) throws SQLException {
        if (cirugia.getCliente() instanceof ClinicaHospital clinica) {
            cmd.setInt("p_id_clinica_hospital", clinica.getIdCliente());
        } else {
            cmd.setNull("p_id_clinica_hospital", Types.INTEGER);
        }

        if (cirugia.getCliente() instanceof PacienteParticular paciente) {
            cmd.setInt("p_id_paciente_particular", paciente.getIdCliente());
        } else {
            cmd.setNull("p_id_paciente_particular", Types.INTEGER);
        }
    }

    private void mapearEquipo(ResultSet rs, Cirugia cirugia) throws SQLException {
        int idEquipo = rs.getInt("id_equipo");
        if (!rs.wasNull()) {
            cirugia.setEquipo(new EquipoDAOImpl().findById(idEquipo));
        }
        else {
            cirugia.setEquipo(null);
        }
    }

    private void mapearBandeja(ResultSet rs, Cirugia cirugia) throws SQLException {
        int idBandeja = rs.getInt("id_bandeja");
        if (!rs.wasNull()) {
            cirugia.setBandejaInstrumental(new BandejaInstrumentalDAOImpl().findById(idBandeja));
        }
        else {
            cirugia.setBandejaInstrumental(null);
        }
    }

    private void mapearCliente(ResultSet rs, Cirugia cirugia) throws SQLException {
        int idClinica = rs.getInt("id_clinica_hospital");
        if (!rs.wasNull()) {
            cirugia.setCliente(new ClinicaHospitalDAOImpl().findById(idClinica));
            return;
        }

        int idPaciente = rs.getInt("id_paciente_particular");
        if (!rs.wasNull()) {
            cirugia.setCliente(new PacienteParticularDAOImpl().findById(idPaciente));
        }
        else {
            cirugia.setCliente(null);
        }
    }
}
