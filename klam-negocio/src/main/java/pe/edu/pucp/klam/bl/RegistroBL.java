package pe.edu.pucp.klam.bl;

import java.util.List;

public interface RegistroBL<T, ID> {
    List<T> findAll() throws BLException;
    T findById(ID id) throws BLException;
    void insert(T entidad) throws BLException;
    void update(T entidad) throws BLException;
    void delete(ID id) throws BLException;
    void validarDatosCliente(String nombre, String direccion, String correo, String telefono) throws BLException;
    void validarLongitud(String valor, int maximo, String campo) throws BLException;
    void validarId(ID id) throws BLException;
}
