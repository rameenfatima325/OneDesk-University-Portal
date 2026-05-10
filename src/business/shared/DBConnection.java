package business.shared;

import    java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static DBConnection instance;
    private Connection connection;

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;databaseName=OneDesk;" + "encrypt=true;trustServerCertificate=true;" + "user=sa;password=sajidaimtiaz325";

    private DBConnection() throws SQLException {
        this.connection = DriverManager.getConnection(URL);
    }

    public static synchronized DBConnection getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}