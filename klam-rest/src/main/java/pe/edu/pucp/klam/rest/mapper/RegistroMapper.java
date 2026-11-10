package pe.edu.pucp.klam.rest.mapper;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.CirugiaBL;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Cirugia;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
import pe.edu.pucp.klam.modelo.agendaoperaciones.EstadoCirugia;
import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.rest.dto.CirugiaRequestDTO;
import pe.edu.pucp.klam.rest.dto.SolicitudCirugiaRequestDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Solo traduce DTO -> modelo. No aplica reglas de negocio: obligatorios, longitudes,
 * DNI y existencia del cliente los valida la BL. Lo único que falla aquí es un
 * formato ilegible (tipo de cliente o fecha), que la BL no llega a ver.
 */
public final class RegistroMapper {

    private RegistroMapper() {
    }

    /** RF05: arma la Cirugia "datos" que espera SolicitudCirugiaBL.registrarSolicitud. */
    public static Cirugia toDatosSolicitud(String tipoCliente, int idCliente, SolicitudCirugiaRequestDTO dto)
            throws BLException {
        Cirugia datos = new Cirugia();
        datos.setCliente(crearCliente(tipoCliente, idCliente, dto.getDni()));
        datos.setDiagnostico(dto.getDiagnostico());
        datos.setTipoProcedimiento(dto.getTipoProcedimiento());
        datos.setDoctorNombre(dto.getDoctorNombre());
        datos.setFechaHoraInicio(parseFecha(dto.getFechaHoraSolicitada(), "La fecha y hora solicitadas"));
        return datos;
    }

    /** Clínica destino de RF05 (null = la sede del propio cliente clínica). */
    public static ClinicaHospital toClinicaDestino(Integer idClinicaDestino) {
        if (idClinicaDestino == null) {
            return null;
        }
        ClinicaHospital clinica = new ClinicaHospital();
        clinica.setIdCliente(idClinicaDestino);
        return clinica;
    }

    /** RF09: arma la Cirugia nueva (siempre PROGRAMADA). */
    public static Cirugia toCirugia(CirugiaRequestDTO dto) throws BLException {
        if (dto.getIdCliente() == null) {
            throw new BLException("El cliente es obligatorio");
        }
        Cirugia cirugia = new Cirugia();
        cirugia.setCliente(crearCliente(dto.getTipoCliente(), dto.getIdCliente(), dto.getDni()));
        cirugia.setDiagnostico(dto.getDiagnostico());
        cirugia.setTipoProcedimiento(dto.getTipoProcedimiento());
        cirugia.setDoctorNombre(dto.getDoctorNombre());
        cirugia.setFechaHoraInicio(parseFecha(dto.getFechaHoraInicio(), "La fecha y hora de inicio"));
        if (dto.getFechaHoraFin() != null && !dto.getFechaHoraFin().isBlank()) {
            cirugia.setFechaHoraFin(parseFecha(dto.getFechaHoraFin(), "La fecha y hora de fin"));
        }
        if (dto.getIdEquipo() != null) {
            Equipo equipo = new Equipo();
            equipo.setIdEquipo(dto.getIdEquipo());
            cirugia.setEquipo(equipo);
        }
        cirugia.setEstado(EstadoCirugia.PROGRAMADA);
        return cirugia;
    }

    /** Cliente solo con id y tipo (para consultas por cliente). */
    public static Cliente toCliente(String tipoCliente, int idCliente) throws BLException {
        return crearCliente(tipoCliente, idCliente, null);
    }

    private static Cliente crearCliente(String tipoCliente, int idCliente, String dni) throws BLException {
        if (CirugiaBL.TIPO_CLIENTE_CLINICA.equals(tipoCliente)) {
            ClinicaHospital clinica = new ClinicaHospital();
            clinica.setIdCliente(idCliente);
            return clinica;
        }
        if (CirugiaBL.TIPO_CLIENTE_PARTICULAR.equals(tipoCliente)) {
            PacienteParticular paciente = new PacienteParticular();
            paciente.setIdCliente(idCliente);
            paciente.setDni(dni);
            return paciente;
        }
        throw new BLException("Tipo de cliente inválido: use " + CirugiaBL.TIPO_CLIENTE_CLINICA
                + " o " + CirugiaBL.TIPO_CLIENTE_PARTICULAR);
    }

    private static LocalDateTime parseFecha(String texto, String campo) throws BLException {
        if (texto == null || texto.isBlank()) {
            return null; // la BL informa que es obligatoria
        }
        try {
            return LocalDateTime.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new BLException(campo + " no tiene formato válido (use yyyy-MM-ddTHH:mm)");
        }
    }
}
