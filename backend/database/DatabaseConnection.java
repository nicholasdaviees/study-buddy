package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Manages JDBC connections
public class DatabaseConnection {

    // Opens a new connection.
    public Connection openConnection(
            String jdbcUrl,
            String username,
            String password
    ) throws SQLException {

        // DriverManager connects Java to MySQL
        return DriverManager.getConnection(
            jdbcUrl,
            username,
            password
        );
    }

    // Closes database connection
    public void closeConnection(
            Connection databaseConnection
    ) throws SQLException {

        // Check that the connection exists.
        if (databaseConnection != null
                && !databaseConnection.isClosed()) {

            // Release the database connection.
            databaseConnection.close();
        }
    }
}

