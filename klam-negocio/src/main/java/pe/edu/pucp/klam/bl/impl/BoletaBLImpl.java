package pe.edu.pucp.klam.bl.impl;

import pe.edu.pucp.klam.bl.BLException;
import pe.edu.pucp.klam.bl.BoletaBL;
import pe.edu.pucp.klam.dao.BoletaDAO;
import pe.edu.pucp.klam.dao.NotaCreditoDAO;
import pe.edu.pucp.klam.dao.impl.finanzas.BoletaDAOImpl;
import pe.edu.pucp.klam.dao.impl.finanzas.NotaCreditoDAOImpl;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.Boleta;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.DocumentoFacturacion;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.EstadoPago;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaDocumento;
import pe.edu.pucp.klam.modelo.documentacionfinanzas.NotaCredito;

import java.sql.SQLException;
import java.util.List;

public class BoletaBLImpl implements BoletaBL {

    private final BoletaDAO boletaDAO = new BoletaDAOImpl();
    private final NotaCreditoDAO notaCreditoDAO = new NotaCreditoDAOImpl();

    @Override
    public List<Boleta> findAll() throws BLException {
        try {
            return boletaDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las boletas", e);
        }
    }

    @Override
    public Boleta findById(Integer id) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("El id de la boleta no es valido");
        }
        try {
            return boletaDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la boleta", e);
        }
    }

    @Override
    public void insert(Boleta boleta) throws BLException {
        validar(boleta);
        if (boleta.getEstadoPago() == EstadoPago.ANULADO) {
            throw new BLException("No se puede registrar una boleta ya anulada");
        }
        boleta.setActivo(true);

        TransactionsManager.iniciar();
        try {
            boletaDAO.insert(boleta);
            TransactionsManager.commit();
        } catch (SQLException e) {
            revertir();
            throw new BLException("No se pudo registrar la boleta", e);
        } catch (RuntimeException e) {
            revertir();
            throw e;
        }
    }

    @Override
    public void update(Boleta boleta) throws BLException {
        if (boleta == null || boleta.getIdDocumento() <= 0) {
            throw new BLException("La boleta a actualizar debe tener un id valido");
        }

        // No se confia en lo que llega de la App: se recarga el estado real de la BD.
        Boleta real = findById(boleta.getIdDocumento());
        if (real == null) {
            throw new BLException("No existe una boleta con id " + boleta.getIdDocumento());
        }
        if (!real.isActivo()) {
            throw new BLException("La boleta esta dada de baja y no se puede modificar");
        }
        if (real.estaAnulado()) {
            throw new BLException("Una boleta anulada no se puede modificar; emita una nota de credito");
        }

        validar(boleta);

        if (real.getEstadoPago() == EstadoPago.PAGADO
                && boleta.getEstadoPago() == EstadoPago.PENDIENTE) {
            throw new BLException("Una boleta pagada no vuelve a estado pendiente");
        }

        double acreditado = montoAcreditado(boleta.getIdDocumento());
        if (boleta.getMontoBase() < acreditado) {
            throw new BLException(String.format(
                    "El monto de la boleta (S/ %.2f) no puede ser menor a lo ya acreditado por notas de credito (S/ %.2f)",
                    boleta.getMontoBase(), acreditado));
        }

        boleta.setActivo(true); // la baja se hace solo con delete()

        TransactionsManager.iniciar();
        try {
            boletaDAO.update(boleta);
            TransactionsManager.commit();
        } catch (SQLException e) {
            revertir();
            throw new BLException("No se pudo actualizar la boleta", e);
        } catch (RuntimeException e) {
            revertir();
            throw e;
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("El id de la boleta no es valido");
        }
        Boleta real = findById(id);
        if (real == null) {
            throw new BLException("No existe una boleta con id " + id);
        }
        if (!real.isActivo()) {
            throw new BLException("La boleta ya esta dada de baja");
        }
        if (!notasDeCredito(id).isEmpty()) {
            throw new BLException("No se puede dar de baja una boleta que tiene notas de credito asociadas");
        }

        TransactionsManager.iniciar();
        try {
            boletaDAO.delete(id);
            TransactionsManager.commit();
        } catch (SQLException e) {
            revertir();
            throw new BLException("No se pudo dar de baja la boleta", e);
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

    private void validar(Boleta boleta) throws BLException {
        if (boleta == null) {
            throw new BLException("La boleta no puede ser nula");
        }
        if (boleta.getCirugia() == null || boleta.getCirugia().getId_cirugia() <= 0) {
            throw new BLException("La boleta debe estar asociada a una cirugia existente");
        }
        if (boleta.getFechaEmision() == null) {
            throw new BLException("La fecha de emision es obligatoria");
        }
        String dni = boleta.getDniReceptor();
        if (dni == null ) {
            throw new BLException("El DNI del receptor es obligatorio");
        }
        if (boleta.getEstadoPago() == null) {
            throw new BLException("El estado de pago es obligatorio");
        }

        boleta.setTasaIgv(DocumentoFacturacion.TASA_IGV_POR_DEFECTO);

        List<LineaDocumento> lineas = boleta.getLineas();
        if (lineas.isEmpty()) {
            throw new BLException("La boleta debe tener al menos una linea");
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

        boleta.calcularMontoTotal();
    }

    private List<NotaCredito> notasDeCredito(int idBoleta) throws BLException {
        try {
            return notaCreditoDAO.findByBoletaId(idBoleta);
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar las notas de credito de la boleta", e);
        }
    }

    private double montoAcreditado(int idBoleta) throws BLException {
        double total = 0.0;
        for (NotaCredito nota : notasDeCredito(idBoleta)) {
            total += nota.getMontoTotal();
        }
        return total;
    }
}