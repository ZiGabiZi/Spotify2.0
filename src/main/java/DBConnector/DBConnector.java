package DBConnector;

import java.sql.*;
import lombok.Getter;

@Getter
public class DBConnector {
    private static DBConnector instance = null;
    private Connection connection;

    public static DBConnector getInstance() {
        if (instance == null) instance = new DBConnector();
        return instance;
    }

    public Connection connectToDatabase(String url, String user, String password)
            throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, user, password);
        }
        System.out.println(connection);
        return connection;
    }
}
