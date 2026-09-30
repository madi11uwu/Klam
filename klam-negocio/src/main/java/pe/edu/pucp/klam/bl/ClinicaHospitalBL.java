package pe.edu.pucp.klam.bl;

import pe.edu.pucp.klam.modelo.clientes.ClinicaHospital;

public interface ClinicaHospitalBL extends BaseBL<ClinicaHospital, Integer> {
    void validarDatosCliente(String nombre, String direccion, String correo, String telefono) throws BLException;
    void validarLongitud(String valor, int maximo, String campo) throws BLException;
    void validarId(Integer ID) throws BLException;
}
