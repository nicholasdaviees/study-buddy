package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT userID, name, email, password FROM users WHERE email = ?";

        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email); 

            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) { 
                    return null;
                }

                return new User(
                    results.getLong("userID"),
                    results.getString("name"),
                    results.getString("email"),
                    results.getString("password")
                );
            }
        }
    }

    public User create(String name, String email, String password)
            throws SQLException {
        String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";

        try (Connection connection = Database.open(); 
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, password);
            if (statement.executeUpdate() != 1) {throw new SQLException("Failed to create the user.");}

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {return new User(keys.getLong(1), name, email, password);}
            }
        }
        throw new SQLException("Could not retrieve the new user ID.");
    }

    public void saveUser(String name, String email, String password)
        throws SQLException {

        String sql =
            "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";

        try (Connection connection = Database.open();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, password);

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Failed to save the user.");
            }
        }
    }


    public User getUserByID(long userID) throws SQLException {
        String sql =
            "SELECT userID, name, email, password FROM users WHERE userID = ?";

        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userID);

            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return null;
                }

                return new User(
                    results.getLong("userID"),
                    results.getString("name"),
                    results.getString("email"),
                    results.getString("password")
                );
            }
        }
    }

    public boolean deleteUserByID(long userID) throws SQLException {
        String sql = "DELETE FROM users WHERE userID = ?";

        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userID);
            return statement.executeUpdate() == 1;
        }
    }
}
