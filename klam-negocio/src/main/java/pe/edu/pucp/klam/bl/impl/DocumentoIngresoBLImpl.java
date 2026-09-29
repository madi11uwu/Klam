package pe.edu.pucp.klam.bl.impl;

import java.sql.SQLException;
import java.util.List;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.DocumentoIngresoBL;
import pe.edu.pucp.klam.dao.DocumentoIngresoDAO;
import pe.edu.pucp.klam.dao.impl.DocumentoIngresoDAOImpl;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.DocumentoIngreso;
import pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso.EstadoValidacionDocumento;

public class DocumentoIngresoBLImpl implements DocumentoIngresoBL {

    private final DocumentoIngresoDAO documentoDAO = new DocumentoIngresoDAOImpl();

    @Override
    public void insertar(DocumentoIngreso doc) throws BLException {
        validarDocumento(doc);
        try {
            if (doc.getEstadoValidacion() == null) {
                doc.setEstadoValidacion(EstadoValidacionDocumento.PENDIENTE);
            }
            documentoDAO.insert(doc);
        } catch (SQLException e) {
            throw new BLException("Error al registrar el documento de ingreso en la base de datos", e);
        }
    }

    @Override
    public void actualizar(DocumentoIngreso doc) throws BLException {
        if (doc == null || doc.getId_documento() <= 0) {
            throw new BLException("El ID del documento de ingreso debe ser valido para actualizar");
        }
        validarDocumento(doc);
        try {
            documentoDAO.update(doc);
        } catch (SQLException e) {
            throw new BLException("Error al actualizar el documento de ingreso", e);
        }
    }

    @Override
    public void eliminar(Integer id) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("Debe proporcionar un ID valido de documento de ingreso para eliminar");
        }
        try {
            documentoDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("Error al eliminar el documento de ingreso", e);
        }
    }

    @Override
    public DocumentoIngreso obtenerPorId(Integer id) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("Debe proporcionar un ID valido de documento de ingreso");
        }
        try {
            return documentoDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("Error al consultar el documento de ingreso", e);
        }
    }

    @Override
    public List<DocumentoIngreso> listarTodos() throws BLException {
        try {
            return documentoDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("Error al listar los documentos de ingreso", e);
        }
    }

    @Override
    public void cambiarEstadoValidacion(Integer idDocumento, EstadoValidacionDocumento nuevoEstado) throws BLException {
        if (idDocumento == null || idDocumento <= 0) {
            throw new BLException("El ID del documento debe ser valido");
        }
        if (nuevoEstado == null) {
            throw new BLException("El nuevo estado de validacion no puede ser nulo");
        }

        DocumentoIngreso doc = obtenerPorId(idDocumento);
        if (doc == null) {
            throw new BLException("No existe un documento de ingreso registrado con el ID proporcionado");
        }

        doc.setEstadoValidacion(nuevoEstado);
        actualizar(doc);
    }

    private void validarDocumento(DocumentoIngreso doc) throws BLException {
        if (doc == null) {
            throw new BLException("El documento de ingreso no puede ser nulo");
        }
        if (doc.getTipoDocumento() == null) {
            throw new BLException("El tipo de documento de ingreso es obligatorio");
        }
        if (doc.getArchivoPath() == null || doc.getArchivoPath().isBlank()) {
            throw new BLException("La ruta del archivo de respaldo es obligatoria");
        }
    }
}