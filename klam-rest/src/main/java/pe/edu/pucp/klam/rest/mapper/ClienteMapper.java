package pe.edu.pucp.klam.rest.mapper;

import pe.edu.pucp.klam.modelo.clientes.Cliente;
import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;
import pe.edu.pucp.klam.modelo.clientes.PacienteParticular;
import pe.edu.pucp.klam.rest.dto.ClienteDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Cliente es abstracta con dos subclases (ClinicaHospital y PacienteParticular). En JSON viajan
 * como un único ClienteDTO con el discriminador "tipoCliente"; los campos de la otra subclase
 * quedan con su valor por defecto. No incluye la lista de cirugías para evitar referencias circulares.
 */
public final class ClienteMapper {
    public static final String TIPO_CLINICA = "CLINICA_HOSPITAL";
    public static final String TIPO_PARTICULAR = "PACIENTE_PARTICULAR";

    private ClienteMapper() {
    }

    public static ClienteDTO toDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setIdCliente(cliente.getIdCliente());
        dto.setActivo(cliente.isActivo());
        dto.setNombre(cliente.getNombre());
        dto.setDireccion(cliente.getDireccion());
        dto.setEmailContacto(cliente.getEmailContacto());
        dto.setTelefono(cliente.getTelefono());
        if (cliente instanceof ClinicaHospital) {
            ClinicaHospital clinica = (ClinicaHospital) cliente;
            dto.setTipoCliente(TIPO_CLINICA);
            dto.setRuc(clinica.getRuc());
            dto.setTieneConsignacion(clinica.isTieneConsignacion());
            dto.setPeriodoCredito(clinica.getPeriodoCredito());
        } else if (cliente instanceof PacienteParticular) {
            PacienteParticular paciente = (PacienteParticular) cliente;
            dto.setTipoCliente(TIPO_PARTICULAR);
            dto.setDni(paciente.getDni());
            dto.setPagoConfirmado(paciente.isPagoConfirmado());
        }
        return dto;
    }

    public static List<ClienteDTO> toDTOs(List<Cliente> clientes) {
        List<ClienteDTO> dtos = new ArrayList<>();
        for (Cliente cliente : clientes) {
            dtos.add(toDTO(cliente));
        }
        return dtos;
    }

    /** @throws IllegalArgumentException si tipoCliente falta o no es reconocido (el Resource responde 400). */
    public static Cliente toModelo(ClienteDTO dto) {
        String tipo = dto.getTipoCliente();
        Cliente cliente;
        if (TIPO_CLINICA.equals(tipo)) {
            ClinicaHospital clinica = new ClinicaHospital();
            clinica.setRuc(dto.getRuc());
            clinica.setTieneConsignacion(dto.isTieneConsignacion());
            clinica.setPeriodoCredito(dto.getPeriodoCredito());
            cliente = clinica;
        } else if (TIPO_PARTICULAR.equals(tipo)) {
            PacienteParticular paciente = new PacienteParticular();
            paciente.setDni(dto.getDni());
            paciente.setPagoConfirmado(dto.isPagoConfirmado());
            cliente = paciente;
        } else {
            throw new IllegalArgumentException("Tipo de cliente no válido: " + tipo);
        }
        cliente.setIdCliente(dto.getIdCliente());
        cliente.setActivo(dto.isActivo());
        cliente.setNombre(dto.getNombre());
        cliente.setDireccion(dto.getDireccion());
        cliente.setEmailContacto(dto.getEmailContacto());
        cliente.setTelefono(dto.getTelefono());
        return cliente;
    }
}
