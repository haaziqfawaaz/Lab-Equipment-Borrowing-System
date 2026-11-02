package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database connectivity using simple JDBC DriverManager.
 * Reads credentials from db.properties.
 */
public class DatabaseConnection {
    private static String url;
    private static String user;
    private static String password;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        Properties props = new Properties();
        // Look for db.properties in working directory or classpath
        try (InputStream in = new FileInputStream("db.properties")) {
            props.load(in);
            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");
        } catch (IOException e) {
            System.err.println("Warning: db.properties not found in working directory. Using default settings.");
            url = "jdbc:mysql://localhost:3306/lab_equipment_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            user = "root";
            password = "";
        }
    }

    /**
     * Obtains a new JDBC Connection using DriverManager.
     * @return active Connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Safely closes resources like Connection, Statement, or ResultSet.
     */
    public static void close(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
