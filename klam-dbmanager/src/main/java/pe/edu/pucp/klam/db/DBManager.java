package pe.edu.pucp.klam.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DBManager {

    private static DBManager dbManager;
    private final String url;
    private final String usuario;
    private final String password;

    private DBManager() {
        // Busca src/main/resources/db.properties (sin la extension .properties)
        ResourceBundle db = ResourceBundle.getBundle("db");
        String hostname = db.getString("db.host");
        String puerto = db.getString("db.port");
        String esquema = db.getString("db.esquema");
        this.usuario = db.getString("db.user");
        this.password = db.getString("db.password");

        // useSSL=false + allowPublicKeyRetrieval=true evita el clasico
        // "Communications link failure" al conectarse a MySQL en AWS.
        this.url = "jdbc:mysql://" + hostname + ":" + puerto + "/" + esquema +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima&autoReconnect=true";
    }

    public Connection getConnection() {
        Connection con;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(this.url, this.usuario, this.password);
        } catch (Exception ex) {
            throw new RuntimeException("Error al conectarse con la BD: " + ex.getMessage(), ex);
        }
        return con;
    }

    public static DBManager getInstance() {
        if (dbManager == null) {
            dbManager = new DBManager();
        }
        return dbManager;
    }

    // ------------------------------------------------------------------
    // Utilidades compartidas por los DAO (evitan repetir el mismo codigo JDBC)
    // ------------------------------------------------------------------

    /** Asigna un INT que puede ser nulo a un parametro con nombre. */
    public static void setIntONulo(CallableStatement cmd, String nombre, Integer valor) throws SQLException {
        if (valor != null) {
            cmd.setInt(nombre, valor);
        } else {
            cmd.setNull(nombre, Types.INTEGER);
        }
    }

    /** Asigna un VARCHAR que puede ser nulo (o vacio, que se trata como nulo) a un parametro con nombre. */
    public static void setStringONulo(CallableStatement cmd, String nombre, String valor) throws SQLException {
        if (valor != null && !valor.isBlank()) {
            cmd.setString(nombre, valor);
        } else {
            cmd.setNull(nombre, Types.VARCHAR);
        }
    }

    /** Asigna un DATETIME que puede ser nulo a un parametro con nombre. */
    public static void setFechaONula(CallableStatement cmd, String nombre, LocalDateTime valor) throws SQLException {
        if (valor != null) {
            cmd.setTimestamp(nombre, Timestamp.valueOf(valor));
        } else {
            cmd.setNull(nombre, Types.TIMESTAMP);
        }
    }

    /**
     * Convierte todas las filas del ResultSet en mapas. Las claves son el nombre
     * de la columna en camelCase (id_cirugia -> idCirugia) y los DATETIME se
     * entregan como LocalDateTime. Se usa en las consultas de pantalla, para las
     * que no se permite crear un DTO.
     */
    public static List<Map<String, Object>> filasAMapas(ResultSet rs) throws SQLException {
        List<Map<String, Object>> filas = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        while (rs.next()) {
            Map<String, Object> fila = new LinkedHashMap<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                Object valor = rs.getObject(i);
                if (valor instanceof Timestamp ts) {
                    valor = ts.toLocalDateTime();
                }
                fila.put(aCamelCase(meta.getColumnLabel(i)), valor);
            }
            filas.add(fila);
        }
        return filas;
    }

    private static String aCamelCase(String columna) {
        StringBuilder sb = new StringBuilder();
        boolean mayuscula = false;
        for (char c : columna.toCharArray()) {
            if (c == '_') {
                mayuscula = true;
            } else {
                sb.append(mayuscula ? Character.toUpperCase(c) : c);
                mayuscula = false;
            }
        }
        return sb.toString();
    }
}
