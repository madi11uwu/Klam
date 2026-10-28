package pe.edu.pucp.klam.dao.impl.inventario;

import pe.edu.pucp.klam.dao.BandejaInstrumentalDAO;
import pe.edu.pucp.klam.dao.transacciones.TransactionsManager;
import pe.edu.pucp.klam.db.DBManager;
import pe.edu.pucp.klam.modelo.agendaoperaciones.BandejaInstrumental;
import pe.edu.pucp.klam.modelo.agendaoperaciones.Consumible;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BandejaInstrumentalDAOImpl implements BandejaInstrumentalDAO {
    @Override
    public void asociarConsumible(int idBandeja, int idConsumible, int cantDespachada) throws SQLException {
        String sql = "{call asociar_consumible_bandeja(?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_bandeja", idBandeja);
            cmd.setInt("p_id_consumible", idConsumible);
            cmd.setInt("p_cantidad_despachada", cantDespachada);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo asociar el consumible " + idConsumible
                        + " a la bandeja " + idBandeja);
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public void actualizarCantidadDespachada(int idBandeja, int idConsumible, int cantDespachada) throws SQLException {
        String sql = "{call actualizar_cantidad_despachada(?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_bandeja", idBandeja);
            cmd.setInt("p_id_consumible", idConsumible);
            cmd.setInt("p_cantidad_despachada", cantDespachada);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("El consumible " + idConsumible + " no esta en la bandeja " + idBandeja);
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public void quitarConsumible(int idBandeja, int idConsumible) throws SQLException {
        String sql = "{call quitar_consumible_bandeja(?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_bandeja", idBandeja);
            cmd.setInt("p_id_consumible", idConsumible);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("El consumible " + idConsumible + " no esta en la bandeja " + idBandeja);
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public void registrarConsumoReal(int idBandeja, int idConsumible, int cantConsumida) throws SQLException {
        String sql = "{call registrar_consumo_bandeja(?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_bandeja", idBandeja);
            cmd.setInt("p_id_consumible", idConsumible);
            cmd.setInt("p_cantidad_consumida", cantConsumida);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo registrar el consumo del consumible " + idConsumible);
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public void cargarConsumiblesPorBandeja(BandejaInstrumental bandeja) throws SQLException {
        if (bandeja == null) {
            throw new IllegalArgumentException("La bandeja no puede ser nula");
        }
        String sql = "{call listar_consumibles_bandeja(?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_bandeja", bandeja.getIdBandeja());
            try (ResultSet rs = cmd.executeQuery()) {
                while (rs.next()) {
                    Consumible c = new Consumible(
                            rs.getInt("id_consumible"),
                            rs.getString("nombre_comercial"),
                            rs.getString("marca"),
                            rs.getString("medida")
                    );
                    c.setActivo(rs.getBoolean("activo"));

                    int despachada = rs.getInt("cantidad_despachada");
                    if (despachada > 0) {
                        bandeja.agregarConsumible(c, despachada);
                    }
                    bandeja.registrarConsumo(c, rs.getInt("cantidad_consumida"));
                }
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public boolean tieneConsumoRegistrado(int idBandeja) throws SQLException {
        String sql = "{call bandeja_tiene_consumo(?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_bandeja", idBandeja);
            cmd.registerOutParameter("p_tiene", Types.TINYINT);
            cmd.execute();
            return cmd.getInt("p_tiene") == 1;
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public List<BandejaInstrumental> listarPorProcedimiento(String procedimiento) throws SQLException {
        if (procedimiento == null || procedimiento.isBlank()) {
            throw new IllegalArgumentException("El procedimiento no puede ser nulo");
        }
        String sql = "{call listar_bandejas_por_procedimiento(?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_procedimiento", procedimiento);
            try (ResultSet rs = cmd.executeQuery()) {
                return mapearLista(rs);
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public List<BandejaInstrumental> listarDisponibles(String procedimiento) throws SQLException {
        String sql = "{call listar_bandejas_disponibles(?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            DBManager.setStringONulo(cmd, "p_procedimiento", procedimiento);
            try (ResultSet rs = cmd.executeQuery()) {
                return mapearLista(rs);
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public Integer buscarCirugiaAsignada(int idBandeja) throws SQLException {
        String sql = "{call buscar_cirugia_de_bandeja(?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_bandeja", idBandeja);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? rs.getInt("id_cirugia") : null;
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public List<Map<String, Object>> buscarConsumos(Integer idCirugia, String tipoCliente, Integer idCliente,
                                                    LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String sql = "{call buscar_consumos(?, ?, ?, ?, ?)}";
        Connection conn = TransactionsManager.obtenerConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            DBManager.setIntONulo(cmd, "p_id_cirugia", idCirugia);
            DBManager.setStringONulo(cmd, "p_tipo_cliente", tipoCliente);
            DBManager.setIntONulo(cmd, "p_id_cliente", idCliente);
            DBManager.setFechaONula(cmd, "p_desde", desde);
            DBManager.setFechaONula(cmd, "p_hasta", hasta);
            try (ResultSet rs = cmd.executeQuery()) {
                return DBManager.filasAMapas(rs);
            }
        } finally {
            TransactionsManager.liberar(conn);
        }
    }

    @Override
    public List<BandejaInstrumental> findAll() throws SQLException {
        String sql = "{call listar_bandejas()}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            return mapearLista(rs);
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
        String sql = "{call insertar_bandeja(?,?,?,?)}";
        try(CallableStatement cmd=conn.prepareCall(sql)){

            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.setString("p_tipo", bandejaInstrumental.getTipo());
            DBManager.setStringONulo(cmd, "p_procedimiento", bandejaInstrumental.getProcedimiento());
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
        String sql = "{call modificar_bandeja(?,?,?,?,?)}";


        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", bandejaInstrumental.getIdBandeja());
            cmd.setString("p_tipo", bandejaInstrumental.getTipo());
            DBManager.setStringONulo(cmd, "p_procedimiento", bandejaInstrumental.getProcedimiento());
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

    private List<BandejaInstrumental> mapearLista(ResultSet rs) throws SQLException {
        List<BandejaInstrumental> bandejas = new ArrayList<>();
        while (rs.next()) {
            bandejas.add(mapear(rs));
        }
        return bandejas;
    }

    private BandejaInstrumental mapear(ResultSet rs) throws SQLException {
        BandejaInstrumental b = new BandejaInstrumental();
        b.setIdBandeja(rs.getInt("id_bandeja"));
        b.setTipo(rs.getString("tipo"));
        b.setProcedimiento(rs.getString("procedimiento"));
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
