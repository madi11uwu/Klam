package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.FacturaBL;
import pe.edu.pucp.klam.dao.FacturaDAO;
import pe.edu.pucp.klam.dao.NotaCreditoDAO;
import pe.edu.pucp.klam.dao.impl.finanzas.FacturaDAOImpl;
import pe.edu.pucp.klam.dao.impl.finanzas.NotaCreditoDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.EstadoPago;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Factura;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaDocumento;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.NotaCredito;

import java.sql.SQLException;
import java.util.List;

public class FacturaBLImpl implements FacturaBL {

    private final FacturaDAO facturaDAO = new FacturaDAOImpl();
    private final NotaCreditoDAO notaCreditoDAO = new NotaCreditoDAOImpl();

    @Override
    public List<Factura> findAll() throws BLException {
        try {
            return facturaDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las facturas", e);
        }
    }

    @Override
    public Factura findById(Integer id) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("El id de la factura no es valido");
        }
        try {
            return facturaDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la factura", e);
        }
    }

    @Override
    public void insert(Factura factura) throws BLException {
        validar(factura);
        if (factura.getEstadoPago() == EstadoPago.ANULADO) {
            throw new BLException("No se puede registrar una factura ya anulada");
        }
        factura.setActivo(true);

        TransactionsManager.iniciar();
        try {
            facturaDAO.insert(factura);
            TransactionsManager.commit();
        } catch (SQLException e) {
            revertir();
            throw new BLException("No se pudo registrar la factura", e);
        } catch (RuntimeException e) {
            revertir();
            throw e;
        }
    }

    @Override
    public void update(Factura factura) throws BLException {
        if (factura == null || factura.getIdDocumento() <= 0) {
            throw new BLException("La factura a actualizar debe tener un id valido");
        }

        Factura real = findById(factura.getIdDocumento());
        if (real == null) {
            throw new BLException("No existe una factura con id " + factura.getIdDocumento());
        }
        if (!real.isActivo()) {
            throw new BLException("La factura esta dada de baja y no se puede modificar");
        }
        if (real.estaAnulado()) {
            throw new BLException("Una factura anulada no se puede modificar; emita una nota de credito");
        }

        validar(factura);

        if (real.getEstadoPago() == EstadoPago.PAGADO
                && factura.getEstadoPago() == EstadoPago.PENDIENTE) {
            throw new BLException("Una factura pagada no vuelve a estado pendiente");
        }

        double acreditado = montoAcreditado(factura.getIdDocumento());
        if (factura.getMontoBase() < acreditado) {
            throw new BLException(String.format(
                    "El monto de la factura (S/ %.2f) no puede ser menor a lo ya acreditado por notas de credito (S/ %.2f)",
                    factura.getMontoBase(), acreditado));
        }

        factura.setActivo(true);

        TransactionsManager.iniciar();
        try {
            facturaDAO.update(factura);
            TransactionsManager.commit();
        } catch (SQLException e) {
            revertir();
            throw new BLException("No se pudo actualizar la factura", e);
        } catch (RuntimeException e) {
            revertir();
            throw e;
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("El id de la factura no es valido");
        }
        Factura real = findById(id);
        if (real == null) {
            throw new BLException("No existe una factura con id " + id);
        }
        if (!real.isActivo()) {
            throw new BLException("La factura ya esta dada de baja");
        }
        if (!notasDeCredito(id).isEmpty()) {
            throw new BLException("No se puede dar de baja una factura que tiene notas de credito asociadas");
        }

        TransactionsManager.iniciar();
        try {
            facturaDAO.delete(id);
            TransactionsManager.commit();
        } catch (SQLException e) {
            revertir();
            throw new BLException("No se pudo dar de baja la factura", e);
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

    private void validar(Factura factura) throws BLException {
        if (factura == null) {
            throw new BLException("La factura no puede ser nula");
        }
        if (factura.getCirugia() == null || factura.getCirugia().getIdCirugia() <= 0) {
            throw new BLException("La factura debe estar asociada a una cirugia existente");
        }
        if (factura.getFechaEmision() == null) {
            throw new BLException("La fecha de emision es obligatoria");
        }
        String ruc = factura.getRucReceptor();
        if (ruc == null) {
            throw new BLException("El RUC del receptor es obligatorio");
        }
        if (factura.getEstadoPago() == null) {
            throw new BLException("El estado de pago es obligatorio");
        }
        if (factura.getTasaIgv() < 0 || factura.getTasaIgv() > 1) {
            throw new BLException("La tasa de IGV debe estar entre 0 y 1");
        }

        List<LineaDocumento> lineas = factura.getLineas();
        if (lineas.isEmpty()) {
            throw new BLException("La factura debe tener al menos una linea");
        }


        for (LineaDocumento linea : lineas) {
            if (linea.getCantidad() <= 0) {
                throw new BLException("Toda linea debe tener cantidad mayor que cero");
            }
            if (linea.getPrecioUnitario() < 0) {
                throw new BLException("Ninguna linea puede tener precio unitario negativo");
            }
            if (linea.getDescripcion() == null || linea.getDescripcion().isBlank()) {
                throw new BLException("Toda linea debe tener descripcion");
            }
        }


        factura.calcularMontoTotal();
    }

    private List<NotaCredito> notasDeCredito(int idFactura) throws BLException {
        try {
            return notaCreditoDAO.findByFacturaId(idFactura);
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar las notas de credito de la factura", e);
        }
    }

    private double montoAcreditado(int idFactura) throws BLException {
        double total = 0.0;
        for (NotaCredito nota : notasDeCredito(idFactura)) {
            total += nota.getMontoTotal();
        }
        return total;
    }

}
