
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {

    // Database used by our Study Buddy application.
    private static final String DEFAULT_URL =
        "jdbc:mysql://127.0.0.1:3306/studybuddy";

    private static final String DEFAULT_USER =
        "studybuddy_app";

    private Database() {
    }

    // Opens a new connection to MySQL.
    public static Connection open() throws SQLException {

        String url = System.getenv().getOrDefault(
            "STUDY_DB_URL",
            DEFAULT_URL
        );

        String username = System.getenv().getOrDefault(
            "STUDY_DB_USER",
            DEFAULT_USER
        );

        String password =
            System.getenv("STUDY_DB_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new SQLException(
                "STUDY_DB_PASSWORD is not configured."
            );
        }

        // Load the MySQL JDBC driver.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (ClassNotFoundException exception) {
            throw new SQLException(
                "MySQL JDBC driver is missing.",
                exception
            );
        }

        // Connect to the database.
        DatabaseConnection databaseConnection = new DatabaseConnection();
        
        return DriverManager.getConnection(
            url,
            username,
            password
        );
    }
}
