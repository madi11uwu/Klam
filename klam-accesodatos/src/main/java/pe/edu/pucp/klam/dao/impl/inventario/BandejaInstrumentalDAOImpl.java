package pe.edu.pucp.klam.dao.impl.inventario;

import pe.edu.pucp.klam.dao.BandejaInstrumentalDAO;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;

import java.sql.*;
import java.util.*;

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

            BandejaInstrumental bandejaInstrumental;
            try(ResultSet rs= cmd.executeQuery()){
                if(!rs.next()) return null;
                bandejaInstrumental=mapear(rs);
            }
            cargarConsumibles(conn,bandejaInstrumental);
            return bandejaInstrumental;
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
            bandejaInstrumental.setId_bandeja(cmd.getInt("p_id"));

            insertarConsumibles(conn, bandejaInstrumental);

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
            cmd.setInt("p_id", bandejaInstrumental.getId_bandeja());
            cmd.setString("p_tipo", bandejaInstrumental.getTipo());
            cmd.setBoolean("p_esterilizado", bandejaInstrumental.isEsterilizado());
            cmd.setBoolean("p_activo", bandejaInstrumental.isActivo());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la bandeja instrumental");
            }

            eliminarConsumibles(conn, bandejaInstrumental.getId_bandeja());
            insertarConsumibles(conn, bandejaInstrumental);
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
        b.setId_bandeja(rs.getInt("id_bandeja"));
        b.setTipo(rs.getString("tipo"));
        b.setEsterilizado(rs.getBoolean("esterilizado"));
        b.setActivo(rs.getBoolean("activo"));
        return b;
    }

    private void insertarConsumibles(Connection conn, BandejaInstrumental bandeja) throws SQLException {
        String sql = "{call insertar_consumible_bandeja(?, ?, ?, ?)}";

        Map<Consumible, Integer> despachados = bandeja.getConsumibles();
        Map<Consumible, Integer> consumidos = bandeja.getConsumiblesConsumidos();

        // todos los consumibles que aparecen en cualquiera de los 2 maps
        Set<Consumible> todos = new HashSet<>(despachados.keySet());
        todos.addAll(consumidos.keySet());

        try (CallableStatement cmd = conn.prepareCall(sql)) {
            for (Consumible c : todos) {
                cmd.setInt("p_id_bandeja", bandeja.getId_bandeja());
                cmd.setInt("p_id_consumible", c.getId_consumible());
                cmd.setInt("p_cantidad_despachada", despachados.getOrDefault(c, 0));
                cmd.setInt("p_cantidad_consumida", consumidos.getOrDefault(c, 0));

                if (cmd.executeUpdate() == 0) {
                    throw new SQLException("No se pudo insertar el consumible " + c.getId_consumible());
                }
            }
        }
    }

    private void eliminarConsumibles(Connection conn, int idBandeja) throws SQLException {
        String sql = "{call eliminar_consumibles_bandeja(?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_bandeja", idBandeja);
            cmd.executeUpdate();   // sin validar 0: puede no tener consumibles
        }
    }

    private void cargarConsumibles(Connection conn, BandejaInstrumental bandeja) throws SQLException {
        String sql = "{call listar_consumibles_bandeja(?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_bandeja", bandeja.getId_bandeja());

            try (ResultSet rs = cmd.executeQuery()) {
                Map<Consumible, Integer> despachados = new HashMap<>();
                Map<Consumible, Integer> consumidos = new HashMap<>();

                while (rs.next()) {
                    Consumible c = new Consumible();
                    c.setId_consumible(rs.getInt("id_consumible"));
                    c.setNombreComercial(rs.getString("nombre_comercial"));
                    c.setMarca(rs.getString("marca"));
                    c.setMedida(rs.getString("medida"));
                    c.setActivo(rs.getBoolean("activo"));

                    despachados.put(c, rs.getInt("cantidad_despachada"));
                    int consumida = rs.getInt("cantidad_consumida");
                    if (consumida > 0) {
                        consumidos.put(c, consumida);
                    }
                }
                bandeja.setConsumibles(despachados);
                bandeja.setConsumiblesConsumidos(consumidos);
            }
        }
    }
}
