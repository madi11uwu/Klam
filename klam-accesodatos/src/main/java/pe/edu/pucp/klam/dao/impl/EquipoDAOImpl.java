//package pe.edu.pucp.klam.dao.impl;
//
//import pe.edu.pucp.klam.dao.EquipoDAO;
//import pe.edu.pucp.klam.modelo.agendaoperaciones.Equipo;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.List;
//
//public class EquipoDAOImpl implements EquipoDAO {
//
//    @Override
//    public List<Equipo> findAll() throws SQLException {
//        String sql=
//                """
//                SELECT id, nombre, activo
//                FROM equipo
//                """;
////        try (
////                Connection conn=DBManager.getInstance().getConnection();
////                PreparedStatement cmd= conn.prepareStatement(sql);
////                ResultSet rs=cmd.executeQuery()) {
////                while(rs.next()){
////                    Equipo equipo=new Equipo();
////                    Equipo.setId(rs.getInt("id"));
////                    Equipo.setId(rs.getInt("id"));
////                    Equipo.setId(rs.getInt("id"));
////                }
////        }
////    }
//
//
//    @Override
//    public Equipo findById(int id) {
//        return null;
//    }
//
//    @Override
//    public void insert(Equipo equipo) {
//
//    }
//
//    @Override
//    public void update(Equipo equipo) {
//
//    }
//
//    @Override
//    public void delete(int id) {
//
//    }
//}
