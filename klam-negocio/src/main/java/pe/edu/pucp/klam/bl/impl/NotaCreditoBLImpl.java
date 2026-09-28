package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.NotaCreditoBL;
import pe.edu.pucp.klam.dao.NotaCreditoDAO;
import pe.edu.pucp.klam.dao.impl.finanzas.BoletaDAOImpl;
import pe.edu.pucp.klam.dao.impl.finanzas.FacturaDAOImpl;
import pe.edu.pucp.klam.dao.impl.finanzas.NotaCreditoDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Boleta;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.DocumentoFacturacion;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Factura;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.NotaCredito;

import java.sql.SQLException;
import java.util.List;

public class NotaCreditoBLImpl implements NotaCreditoBL {
    private final NotaCreditoDAO notaCreditoDAO = new NotaCreditoDAOImpl();

    @Override
    public List<NotaCredito> findByFacturaId(int idFactura) throws BLException {
        try {
            return notaCreditoDAO.findByFacturaId(idFactura);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las notas de credito de la factura", e);
        }

    }

    @Override
    public List<NotaCredito> findByBoletaId(int idBoleta) throws BLException {
        try {
            return notaCreditoDAO.findByBoletaId(idBoleta);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las notas de credito de la boleta", e);
        }

    }

    @Override
    public List<NotaCredito> findAll() throws BLException {
        try {
            return notaCreditoDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las notas de credito", e);
        }

    }

    @Override
    public NotaCredito findById(Integer id) throws BLException {
        try {
            return notaCreditoDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la nota de credito", e);
        }

    }

    @Override
    public void insert(NotaCredito nota) throws BLException {
        validarYCalcular(nota);

        TransactionsManager.iniciar();
        try {
            notaCreditoDAO.insert(nota);
            TransactionsManager.commit();
        } catch (SQLException e) {
            revertir();
            throw new BLException("No se pudo registrar la nota de credito", e);
        } catch (RuntimeException e) {
            revertir();
            throw e;
        }
    }

    @Override
    public void update(NotaCredito nota) throws BLException {
        if (nota == null || nota.getIdNotaCredito() <= 0) {
            throw new BLException("La nota de credito a actualizar debe tener un id valido");
        }
        if (findById(nota.getIdNotaCredito()) == null) {
            throw new BLException("No existe una nota de credito con id " + nota.getIdNotaCredito());
        }
        validarYCalcular(nota);

        TransactionsManager.iniciar();
        try {
            notaCreditoDAO.update(nota);
            TransactionsManager.commit();
        } catch (SQLException e) {
            revertir();
            throw new BLException("No se pudo actualizar la nota de credito", e);
        } catch (RuntimeException e) {
            revertir();
            throw e;
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("El id de la nota de credito no es valido");
        }
        if (findById(id) == null) {
            throw new BLException("No existe una nota de credito con id " + id);
        }

        TransactionsManager.iniciar();
        try {
            notaCreditoDAO.delete(id);
            TransactionsManager.commit();
        } catch (SQLException e) {
            revertir();
            throw new BLException("No se pudo eliminar la nota de credito", e);
        } catch (RuntimeException e) {
            revertir();
            throw e;
        }
    }

    private void revertir() {
        if (TransactionsManager.activa()) {
            TransactionsManager.rollback();
        }
    }

    private void validarYCalcular(NotaCredito nota) throws BLException {
        if (nota == null) {
            throw new BLException("La nota de credito no puede ser nula");
        }
        if (nota.getMotivo() == null || nota.getMotivo().isBlank()) {
            throw new BLException("El motivo de la nota de credito es obligatorio");
        }
        if (nota.getMotivo().length() > 255) {
            throw new BLException("El motivo no puede superar los 255 caracteres");
        }
        if (nota.getFechaEmision() == null) {
            throw new BLException("La fecha de emision es obligatoria");
        }
        if (nota.getLineas().isEmpty()) {
            throw new BLException("La nota de credito debe tener al menos una linea");
        }

        DocumentoFacturacion original = nota.getDocumentoOriginal();
        if (original == null || original.getIdDocumento() <= 0) {
            throw new BLException("La nota de credito debe corregir un documento existente");
        }

        DocumentoFacturacion real = recargarOriginal(original);
        if (real == null) {
            throw new BLException("No existe el documento original con id " + original.getIdDocumento());
        }
        if (!real.isActivo()) {
            throw new BLException("El documento original esta dado de baja");
        }
        nota.setDocumentoOriginal(real);

        double monto = nota.calcularMontoTotal();
        if (nota.excedeAlDocumentoOriginal()) {
            throw new BLException(String.format(
                    "El monto de la nota (S/ %.2f) excede el monto del documento original (S/ %.2f)",
                    monto, real.getMontoBase()));
        }

        double yaAcreditado = 0.0;
        for (NotaCredito previa : notasDelDocumento(real)) {
            if (previa.getIdNotaCredito() != nota.getIdNotaCredito()) {
                yaAcreditado += previa.getMontoTotal();
            }
        }
        if (yaAcreditado + monto > real.getMontoBase()) {
            throw new BLException(String.format(
                    "Las notas de credito acumuladas (S/ %.2f) exceden el monto del documento original (S/ %.2f)",
                    yaAcreditado + monto, real.getMontoBase()));
        }
    }

    private DocumentoFacturacion recargarOriginal(DocumentoFacturacion original) throws BLException {
        try {
            if (original instanceof Factura) {
                return new FacturaDAOImpl().findById(original.getIdDocumento());
            }
            if (original instanceof Boleta) {
                return new BoletaDAOImpl().findById(original.getIdDocumento());
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el documento original", e);
        }
        throw new BLException("Tipo de documento original no soportado");
    }

    private List<NotaCredito> notasDelDocumento(DocumentoFacturacion documento) throws BLException {
        return (documento instanceof Factura)
                ? findByFacturaId(documento.getIdDocumento())
                : findByBoletaId(documento.getIdDocumento());
    }

}
