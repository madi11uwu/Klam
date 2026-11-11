
package pe.edu.pucp.klam.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.klam.dao.SolicitudCirugiaDAO;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoSolicitudCirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.SolicitudCirugia;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.modelo.usuariosPermisos.Vendedor;

public class SolicitudCirugiaDAOImpl implements SolicitudCirugiaDAO {

    @Override
    public void insert(SolicitudCirugia solicitud) throws SQLException {
        if (solicitud == null || solicitud.getCliente() == null
                || solicitud.getClinicaDestino() == null
                || solicitud.getFechaHoraSolicitada() == null
                || solicitud.getFechaSolicitud() == null) {
            throw new IllegalArgumentException(
                    "Datos obligatorios de la solicitud incompletos");
        }

        String sql = "{call insertar_solicitud_cirugia(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();

        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.registerOutParameter("p_id", Types.INTEGER);

            Cliente cliente = solicitud.getCliente();

            DBManager.setIntONulo(cmd, "p_id_clinica_solicitante",
                    cliente instanceof ClinicaHospital
                            ? cliente.getIdCliente() : null);

            DBManager.setIntONulo(cmd, "p_id_paciente_solicitante",
                    cliente instanceof PacienteParticular
                            ? cliente.getIdCliente() : null);

            Vendedor vendedor = solicitud.getVendedorRegistrador();
            DBManager.setIntONulo(cmd, "p_id_vendedor_registrador",
                    vendedor != null ? vendedor.getIdUsuario() : null);

            cmd.setInt("p_id_clinica_hospital",
                    solicitud.getClinicaDestino().getIdCliente());

            cmd.setString("p_paciente_nombre",
                    solicitud.getPacienteNombre());

            cmd.setString("p_diagnostico",
                    solicitud.getDiagnostico());

            cmd.setString("p_tipo_procedimiento",
                    solicitud.getTipoProcedimiento());

            cmd.setString("p_doctor_nombre",
                    solicitud.getDoctorNombre());

            cmd.setTimestamp("p_fecha_hora_solicitada",
                    Timestamp.valueOf(solicitud.getFechaHoraSolicitada()));

            DBManager.setStringONulo(cmd, "p_observaciones",
                    solicitud.getObservaciones());

            cmd.setTimestamp("p_fecha_registro",
                    Timestamp.valueOf(solicitud.getFechaSolicitud()));

            cmd.execute();

            solicitud.setIdSolicitud(cmd.getInt("p_id"));

        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public SolicitudCirugia findById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de solicitud inválido");
        }

        List<SolicitudCirugia> encontrados =
                buscar(id, null, null, null, null);

        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    @Override
    public List<SolicitudCirugia> findAll() throws SQLException {
        return buscar(null, null, null, null, null);
    }

    @Override
    public List<SolicitudCirugia> listarPorEstado(
            EstadoSolicitudCirugia estado) throws SQLException {

        return buscar(null,
                estado != null ? estado.name() : null,
                null, null, null);
    }

    @Override
    public List<SolicitudCirugia> listarPorCliente(
            Cliente cliente) throws SQLException {

        if (cliente == null || cliente.getIdCliente() <= 0) {
            throw new IllegalArgumentException("Cliente inválido");
        }

        String tipo;

        if (cliente instanceof ClinicaHospital) {
            tipo = "CLINICA_HOSPITAL";
        } else if (cliente instanceof PacienteParticular) {
            tipo = "PACIENTE_PARTICULAR";
        } else {
            throw new IllegalArgumentException(
                    "Tipo de cliente no soportado");
        }

        return buscar(null, null, tipo, cliente.getIdCliente(), null);
    }

    @Override
    public List<SolicitudCirugia> listarPorVendedor(Vendedor vendedor) throws SQLException {
        if (vendedor == null || vendedor.getIdUsuario() <= 0) {
            throw new IllegalArgumentException("Vendedor inválido");
        }
        return buscar(null, null, null, null, vendedor.getIdUsuario());
    }

    private List<SolicitudCirugia> buscar(
            Integer id,
            String estado,
            String tipoCliente,
            Integer idCliente,
            Integer idVendedorRegistrador) throws SQLException {

        String sql = "{call buscar_solicitudes(?, ?, ?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();

        try (CallableStatement cmd = conn.prepareCall(sql)) {

            DBManager.setIntONulo(cmd, "p_id_solicitud", id);
            DBManager.setStringONulo(cmd, "p_estado", estado);
            DBManager.setStringONulo(cmd, "p_tipo_cliente", tipoCliente);
            DBManager.setIntONulo(cmd, "p_id_cliente", idCliente);
            DBManager.setIntONulo(cmd, "p_id_vendedor_registrador", idVendedorRegistrador);

            try (ResultSet rs = cmd.executeQuery()) {
                List<SolicitudCirugia> lista = new ArrayList<>();

                while (rs.next()) {
                    lista.add(mapear(rs));
                }

                return lista;
            }

        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    private SolicitudCirugia mapear(ResultSet rs) throws SQLException {

        SolicitudCirugia solicitud = new SolicitudCirugia();

        solicitud.setIdSolicitud(rs.getInt("id_solicitud"));

        int idVendedor = rs.getInt("id_vendedor_registrador");
        if (!rs.wasNull()) {
            Vendedor vendedor = new Vendedor();
            vendedor.setIdUsuario(idVendedor);
            solicitud.setVendedorRegistrador(vendedor);
        }

        int idClinica = rs.getInt("id_clinica_solicitante");
        boolean esClinica = !rs.wasNull();

        Cliente cliente;

        if (esClinica) {
            ClinicaHospital clinica = new ClinicaHospital();
            clinica.setIdCliente(idClinica);
            cliente = clinica;
        } else {
            int idPaciente = rs.getInt("id_paciente_solicitante");

            if (rs.wasNull()) {
                throw new SQLException(
                        "Solicitud sin cliente solicitante");
            }

            PacienteParticular paciente = new PacienteParticular();
            paciente.setIdCliente(idPaciente);
            paciente.setDni(rs.getString("dni"));
            cliente = paciente;
        }

        cliente.setNombre(rs.getString("cliente_nombre"));
        solicitud.setCliente(cliente);

        ClinicaHospital destino = new ClinicaHospital();
        destino.setIdCliente(rs.getInt("id_clinica_hospital"));
        destino.setNombre(rs.getString("clinica_nombre"));
        solicitud.setClinicaDestino(destino);

        solicitud.setPacienteNombre(rs.getString("paciente_nombre"));
        solicitud.setDiagnostico(rs.getString("diagnostico"));
        solicitud.setTipoProcedimiento(
                rs.getString("tipo_procedimiento"));
        solicitud.setDoctorNombre(rs.getString("doctor_nombre"));

        Timestamp fecha = rs.getTimestamp("fecha_hora_solicitada");

        solicitud.setFechaHoraSolicitada(
                fecha != null ? fecha.toLocalDateTime() : null);

        solicitud.setObservaciones(rs.getString("observaciones"));

        solicitud.setEstado(EstadoSolicitudCirugia.valueOf(
                rs.getString("estado_solicitud")));

        solicitud.setMotivoRechazo(rs.getString("motivo_rechazo"));

        Timestamp registro = rs.getTimestamp("fecha_registro");

        solicitud.setFechaSolicitud(
                registro != null ? registro.toLocalDateTime() : null);

        Timestamp revision = rs.getTimestamp("fecha_revision");

        solicitud.setFechaRespuesta(
                revision != null ? revision.toLocalDateTime() : null);

        return solicitud;
    }

    @Override
    public void cambiarEstado(
            SolicitudCirugia solicitud) throws SQLException {

        if (solicitud == null
                || solicitud.getIdSolicitud() <= 0
                || solicitud.getEstado() == null) {
            throw new IllegalArgumentException(
                    "Solicitud o estado inválido");
        }

        String sql = "{call cambiar_estado_solicitud(?, ?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();

        try (CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", solicitud.getIdSolicitud());

            cmd.setString("p_estado",
                    solicitud.getEstado().name());

            DBManager.setStringONulo(cmd, "p_motivo_rechazo",
                    solicitud.getMotivoRechazo());

            DBManager.setFechaONula(cmd, "p_fecha_revision",
                    solicitud.getFechaRespuesta());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo cambiar el estado de la solicitud");
            }

        } finally {
            TransactionsManager.liberar(conn);
        }
    }
}
