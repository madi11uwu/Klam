package pe.edu.pucp.klam.db;

import java.sql.Connection;
import java.sql.DriverManager;
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
}

