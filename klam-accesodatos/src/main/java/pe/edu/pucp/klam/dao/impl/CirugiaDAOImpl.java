package pe.edu.pucp.klam.dao.impl;

import pe.edu.pucp.klam.dao.CirugiaDAO;
import pe.edu.pucp.klam.dao.impl.inventario.BandejaInstrumentalDAOImpl;
import pe.edu.pucp.klam.dao.impl.inventario.EquipoDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
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
import java.util.Map;

/**
 * Todas las operaciones usan la conexion de la transaccion activa (si la hay),
 * de modo que la BL pueda modificar la cirugia y registrar la auditoria en una
 * misma transaccion. Sin transaccion activa usan una conexion propia.
 */
public class CirugiaDAOImpl implements CirugiaDAO {
    @Override
    public List<Cirugia> findAll() throws SQLException {
        return buscar(null, null, null, null, null, null);
    }

    @Override
    public Cirugia findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_cirugia_por_id(?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", id);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Cirugia()) : null;
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public List<Cirugia> findByEstado(EstadoCirugia estado) throws SQLException {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        return buscar(null, null, null, null, null, estado);
    }

    @Override
    public List<Cirugia> findByRangoFechas(LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("El rango de fechas no puede ser nulo");
        }
        return buscar(desde, hasta, null, null, null, null);
    }

    @Override
    public List<Cirugia> buscar(LocalDateTime desde, LocalDateTime hasta, String tipoCliente,
                                Integer idCliente, Integer idEquipo, EstadoCirugia estado) throws SQLException {
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = prepararBusqueda(conn, desde, hasta, tipoCliente, idCliente, idEquipo, estado);
             ResultSet rs = cmd.executeQuery()) {
            List<Cirugia> cirugias = new ArrayList<>();
            while (rs.next()) {
                cirugias.add(mapear(rs, new Cirugia()));
            }
            return cirugias;
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public List<Map<String, Object>> buscarDetalle(LocalDateTime desde, LocalDateTime hasta, String tipoCliente,
                                                   Integer idCliente, Integer idEquipo,
                                                   EstadoCirugia estado) throws SQLException {
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = prepararBusqueda(conn, desde, hasta, tipoCliente, idCliente, idEquipo, estado);
             ResultSet rs = cmd.executeQuery()) {
            return DBManager.filasAMapas(rs);
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    // Unica llamada a buscar_cirugias: la comparten buscar(), buscarDetalle() y los findBy*
    private CallableStatement prepararBusqueda(Connection conn, LocalDateTime desde, LocalDateTime hasta,
                                               String tipoCliente, Integer idCliente, Integer idEquipo,
                                               EstadoCirugia estado) throws SQLException {
        CallableStatement cmd = conn.prepareCall("{call buscar_cirugias(?, ?, ?, ?, ?, ?)}");
        DBManager.setFechaONula(cmd, "p_desde", desde);
        DBManager.setFechaONula(cmd, "p_hasta", hasta);
        DBManager.setStringONulo(cmd, "p_tipo_cliente", tipoCliente);
        DBManager.setIntONulo(cmd, "p_id_cliente", idCliente);
        DBManager.setIntONulo(cmd, "p_id_equipo", idEquipo);
        DBManager.setStringONulo(cmd, "p_estado", estado != null ? estado.name() : null);
        return cmd;
    }

    @Override
    public void insert(Cirugia cirugia) throws SQLException {
        if (cirugia == null) {
            throw new IllegalArgumentException("La cirugia no puede ser nula");
        }

        String sql = "{call insertar_cirugia(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.registerOutParameter("p_id", Types.INTEGER);
            asignarDatos(cmd, cirugia);
            DBManager.setIntONulo(cmd, "p_id_solicitud_origen", cirugia.getIdSolicitudOrigen());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la cirugia");
            }

            cirugia.setIdCirugia(cmd.getInt("p_id"));
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public void update(Cirugia cirugia) throws SQLException {
        if (cirugia == null) {
            throw new IllegalArgumentException("La cirugia no puede ser nula");
        }

        String sql = "{call modificar_cirugia(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            asignarDatos(cmd, cirugia);
            cmd.setInt("p_id", cirugia.getIdCirugia());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la cirugia");
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    // Parametros comunes de insertar_cirugia y modificar_cirugia
    private void asignarDatos(CallableStatement cmd, Cirugia cirugia) throws SQLException {
        cmd.setTimestamp("p_fecha_hora_inicio", Timestamp.valueOf(cirugia.getFechaHoraInicio()));
        DBManager.setFechaONula(cmd, "p_fecha_hora_fin", cirugia.getFechaHoraFin());
        cmd.setString("p_tipo_procedimiento", cirugia.getTipoProcedimiento());
        cmd.setString("p_doctor_nombre", cirugia.getDoctorNombre());
        cmd.setString("p_diagnostico", cirugia.getDiagnostico());
        DBManager.setStringONulo(cmd, "p_motivo_cancelacion", cirugia.getMotivoCancelacion());
        cmd.setString("p_estado", cirugia.getEstado().name());
        DBManager.setIntONulo(cmd, "p_id_equipo",
                cirugia.getEquipo() != null ? cirugia.getEquipo().getIdEquipo() : null);
        DBManager.setIntONulo(cmd, "p_id_bandeja",
                cirugia.getBandejaInstrumental() != null ? cirugia.getBandejaInstrumental().getIdBandeja() : null);
        asignarCliente(cmd, cirugia);
        cmd.setBoolean("p_activo", cirugia.isActivo());
    }

    @Override
    public void cancelar(Integer id, String motivo) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call cancelar_cirugia(?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", id);
            cmd.setString("p_motivo_cancelacion", motivo);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo cancelar la cirugia");
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call eliminar_cirugia(?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar la cirugia");
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public boolean bandejaAsignadaAOtraCirugia(int idBandeja, Integer idCirugiaExcluir) throws SQLException {
        String sql = "{call bandeja_asignada_a_otra_cirugia(?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_bandeja", idBandeja);
            DBManager.setIntONulo(cmd, "p_id_cirugia_excluir", idCirugiaExcluir);
            cmd.registerOutParameter("p_asignada", Types.TINYINT);
            cmd.execute();
            return cmd.getInt("p_asignada") == 1;
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public boolean existeCruceEquipo(int idEquipo, LocalDateTime inicio, LocalDateTime fin,
                                     Integer idCirugiaExcluir) throws SQLException {
        String sql = "{call existe_cruce_equipo(?, ?, ?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_equipo", idEquipo);
            cmd.setTimestamp("p_fecha_hora_inicio", Timestamp.valueOf(inicio));
            DBManager.setFechaONula(cmd, "p_fecha_hora_fin", fin);
            DBManager.setIntONulo(cmd, "p_id_cirugia_excluir", idCirugiaExcluir);
            cmd.registerOutParameter("p_hay_cruce", Types.TINYINT);
            cmd.execute();
            return cmd.getInt("p_hay_cruce") == 1;
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public void cambiarEstado(int idCirugia, EstadoCirugia estado, LocalDateTime fechaHoraFin) throws SQLException {
        String sql = "{call cambiar_estado_cirugia(?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idCirugia);
            cmd.setString("p_estado", estado.name());
            DBManager.setFechaONula(cmd, "p_fecha_hora_fin", fechaHoraFin);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo cambiar el estado de la cirugia");
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public void marcarConsumoCompleto(int idCirugia) throws SQLException {
        String sql = "{call marcar_consumo_completo(?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idCirugia);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo marcar el consumo de la cirugia como completo");
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public Integer buscarCirugiaVigentePorSolicitud(int idSolicitud) throws SQLException {
        String sql = "{call buscar_cirugia_vigente_por_solicitud(?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_solicitud", idSolicitud);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? rs.getInt("id_cirugia") : null;
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    // ------------------------------------------------------------------
    // Solicitudes de cirugia
    // ------------------------------------------------------------------
    @Override
    public int insertSolicitud(Cirugia datos, int idClinicaDestino, String pacienteNombre,
                               String observaciones, LocalDateTime fechaRegistro) throws SQLException {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos de la solicitud no pueden ser nulos");
        }

        String sql = "{call insertar_solicitud_cirugia(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.registerOutParameter("p_id", Types.INTEGER);
            DBManager.setIntONulo(cmd, "p_id_clinica_solicitante",
                    datos.getCliente() instanceof ClinicaHospital c ? c.getIdCliente() : null);
            DBManager.setIntONulo(cmd, "p_id_paciente_solicitante",
                    datos.getCliente() instanceof PacienteParticular p ? p.getIdCliente() : null);
            cmd.setInt("p_id_clinica_hospital", idClinicaDestino);
            cmd.setString("p_paciente_nombre", pacienteNombre);
            cmd.setString("p_diagnostico", datos.getDiagnostico());
            cmd.setString("p_tipo_procedimiento", datos.getTipoProcedimiento());
            cmd.setString("p_doctor_nombre", datos.getDoctorNombre());
            cmd.setTimestamp("p_fecha_hora_solicitada", Timestamp.valueOf(datos.getFechaHoraInicio()));
            DBManager.setStringONulo(cmd, "p_observaciones", observaciones);
            cmd.setTimestamp("p_fecha_registro", Timestamp.valueOf(fechaRegistro));

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la solicitud de cirugia");
            }
            return cmd.getInt("p_id");
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public List<Map<String, Object>> buscarSolicitudes(Integer idSolicitud, String estado,
                                                       String tipoCliente, Integer idCliente) throws SQLException {
        String sql = "{call buscar_solicitudes(?, ?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            DBManager.setIntONulo(cmd, "p_id_solicitud", idSolicitud);
            DBManager.setStringONulo(cmd, "p_estado", estado);
            DBManager.setStringONulo(cmd, "p_tipo_cliente", tipoCliente);
            DBManager.setIntONulo(cmd, "p_id_cliente", idCliente);
            try (ResultSet rs = cmd.executeQuery()) {
                return DBManager.filasAMapas(rs);
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public void cambiarEstadoSolicitud(int idSolicitud, String estado, String motivoRechazo,
                                       LocalDateTime fechaRevision) throws SQLException {
        String sql = "{call cambiar_estado_solicitud(?, ?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idSolicitud);
            cmd.setString("p_estado", estado);
            DBManager.setStringONulo(cmd, "p_motivo_rechazo", motivoRechazo);
            DBManager.setFechaONula(cmd, "p_fecha_revision", fechaRevision);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo cambiar el estado de la solicitud " + idSolicitud);
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    // ------------------------------------------------------------------
    // Auditoria
    // ------------------------------------------------------------------
    @Override
    public int insertAuditoria(int idCirugia, LocalDateTime fechaHora, String accion, String responsable,
                               String detalle, String valoresAnteriores,
                               String valoresPosteriores) throws SQLException {
        String sql = "{call insertar_auditoria_cirugia(?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.setInt("p_id_cirugia", idCirugia);
            cmd.setTimestamp("p_fecha_hora", Timestamp.valueOf(fechaHora));
            cmd.setString("p_accion", accion);
            cmd.setString("p_responsable", responsable);
            DBManager.setStringONulo(cmd, "p_detalle", detalle);
            DBManager.setStringONulo(cmd, "p_valores_anteriores", valoresAnteriores);
            DBManager.setStringONulo(cmd, "p_valores_posteriores", valoresPosteriores);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo registrar la auditoria de la cirugia");
            }
            return cmd.getInt("p_id");
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public List<Map<String, Object>> buscarAuditoria(Integer idAuditoria, Integer idCirugia, LocalDateTime desde,
                                                     LocalDateTime hasta, String accion,
                                                     String responsable) throws SQLException {
        String sql = "{call buscar_auditoria_cirugia(?, ?, ?, ?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            DBManager.setIntONulo(cmd, "p_id_auditoria", idAuditoria);
            DBManager.setIntONulo(cmd, "p_id_cirugia", idCirugia);
            DBManager.setFechaONula(cmd, "p_desde", desde);
            DBManager.setFechaONula(cmd, "p_hasta", hasta);
            DBManager.setStringONulo(cmd, "p_accion", accion);
            DBManager.setStringONulo(cmd, "p_responsable", responsable);
            try (ResultSet rs = cmd.executeQuery()) {
                return DBManager.filasAMapas(rs);
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    // ------------------------------------------------------------------
    // Mapeo
    // ------------------------------------------------------------------
    protected Cirugia mapear(ResultSet rs, Cirugia cirugia) throws SQLException {
        cirugia.setIdCirugia(rs.getInt("id_cirugia"));
        cirugia.setFechaHoraInicio(rs.getTimestamp("fecha_hora_inicio").toLocalDateTime());

        Timestamp fechaHoraFin = rs.getTimestamp("fecha_hora_fin");
        cirugia.setFechaHoraFin(fechaHoraFin != null ? fechaHoraFin.toLocalDateTime() : null);

        cirugia.setTipoProcedimiento(rs.getString("tipo_procedimiento"));
        cirugia.setDoctorNombre(rs.getString("doctor_nombre"));
        cirugia.setDiagnostico(rs.getString("diagnostico"));
        cirugia.setMotivoCancelacion(rs.getString("motivo_cancelacion"));
        cirugia.setEstado(Enum.valueOf(EstadoCirugia.class, rs.getString("estado")));
        cirugia.setConsumoCompleto(rs.getBoolean("consumo_completo"));
        cirugia.setActivo(rs.getBoolean("activo"));

        int idSolicitud = rs.getInt("id_solicitud_origen");
        cirugia.setIdSolicitudOrigen(rs.wasNull() ? null : idSolicitud);

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
