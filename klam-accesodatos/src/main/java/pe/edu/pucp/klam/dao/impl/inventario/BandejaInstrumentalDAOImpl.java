package pe.edu.pucp.klam.dao.impl.inventario;

import pe.edu.pucp.klam.dao.BandejaInstrumentalDAO;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BandejaInstrumentalDAOImpl implements BandejaInstrumentalDAO {
    @Override
    public List<BandejaInstrumental> findAll() throws SQLException {
        String sql = "{call listar_bandejas()}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            List<BandejaInstrumental> bandejaInstrumentales = new ArrayList<>();
            while (rs.next()) {
                bandejaInstrumentales.add(mapear(rs));
            }
            return bandejaInstrumentales;
        }
    }

    @Override
    public BandejaInstrumental findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        String sql = "{call buscar_bandeja_por_id(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", id);


            try(ResultSet rs= cmd.executeQuery()){
                return rs.next() ? mapear(rs):null;
            }
        }
    }

    @Override
    public void insert(BandejaInstrumental bandejaInstrumental) throws SQLException {
        if(bandejaInstrumental==null){
            throw new IllegalArgumentException("La bandeja instrumental no puede ser nula");
        }
        Connection conn= TransactionsManager.getConnection();
        String sql = "{call insertar_bandeja(?,?,?)}";
        try(CallableStatement cmd=conn.prepareCall(sql)){

            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.setString("p_tipo", bandejaInstrumental.getTipo());
            cmd.setBoolean("p_esterilizado", bandejaInstrumental.isEsterilizado());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la bandeja instrumental");
            }
            bandejaInstrumental.setIdBandeja(cmd.getInt("p_id"));

            BandejaConsumibleDAO bandejaConsumibleDAO=new BandejaConsumibleDAOImpl();
            bandejaConsumibleDAO.insertConsumibles(bandejaInstrumental.getIdBandeja(),
                    bandejaInstrumental.getConsumibles(), bandejaInstrumental.getConsumiblesConsumidos());

        }
    }

    @Override
    public void update(BandejaInstrumental bandejaInstrumental) throws SQLException {
        if (bandejaInstrumental == null) {
            throw new IllegalArgumentException("La bandeja instrumental no puede ser nula");
        }
        Connection conn = TransactionsManager.getConnection();
        String sql = "{call modificar_bandeja(?,?,?,?)}";


        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", bandejaInstrumental.getIdBandeja());
            cmd.setString("p_tipo", bandejaInstrumental.getTipo());
            cmd.setBoolean("p_esterilizado", bandejaInstrumental.isEsterilizado());
            cmd.setBoolean("p_activo", bandejaInstrumental.isActivo());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la bandeja instrumental");
            }
            BandejaConsumibleDAO bandejaConsumibleDAO = new BandejaConsumibleDAOImpl();
            bandejaConsumibleDAO.deleteConsumibles(bandejaInstrumental.getIdBandeja());
            bandejaConsumibleDAO.insertConsumibles(bandejaInstrumental.getIdBandeja(),
                    bandejaInstrumental.getConsumibles(), bandejaInstrumental.getConsumiblesConsumidos());
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if(id==null){
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        Connection conn=TransactionsManager.getConnection();
        String sql="{call eliminar_bandeja(?)}";
        try(CallableStatement cmd= conn.prepareCall(sql)){
            cmd.setInt("p_id",id);

            if(cmd.executeUpdate()==0){
                throw new SQLException("No se pudo eliminar la bandeja instrumental");
            }
        }
    }

    private BandejaInstrumental mapear(ResultSet rs) throws SQLException {
        BandejaInstrumental b = new BandejaInstrumental();
        b.setIdBandeja(rs.getInt("id_bandeja"));
        b.setTipo(rs.getString("tipo"));
        b.setEsterilizado(rs.getBoolean("esterilizado"));
        b.setActivo(rs.getBoolean("activo"));

        mapearConsumibles(b);
        return b;
    }
    private void mapearConsumibles(BandejaInstrumental bandeja) throws SQLException {
        BandejaConsumibleDAO consumibleDAO = new BandejaConsumibleDAOImpl();
        bandeja.setConsumibles(consumibleDAO.findDespachadosByBandejaId(bandeja.getIdBandeja()));
        bandeja.setConsumiblesConsumidos(consumibleDAO.findConsumidosByBandejaId(bandeja.getIdBandeja()));
    }
}
