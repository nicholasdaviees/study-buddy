
package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDao {

    // Find user by Email.
    public User findByEmail(String email)
            throws SQLException {

        String sql =
            "SELECT id, name, email, password_hash " +
            "FROM users WHERE email = ?";

        try (Connection connection = Database.open();
             PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return mapUser(result);
                }
            }
        }

        return null;
    }

    
    // Find user by ID.
    public User findById(long id)
            throws SQLException {

        String sql =
            "SELECT id, name, email, password_hash " +
            "FROM users WHERE id = ?";

        try (Connection connection = Database.open();
             PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return mapUser(result);
                }
            }
        }

        return null;
    }

    
    // Create a new user.
    public User create(String name,
                       String email,
                       String passwordHash)
            throws SQLException {

        String sql =
            "INSERT INTO users " +
            "(name, email, password_hash) " +
            "VALUES (?, ?, ?)";

        try (Connection connection = Database.open();
             PreparedStatement statement =
                 connection.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
                 )) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, passwordHash);

            int rows = statement.executeUpdate();

            if (rows != 1) {
                throw new SQLException(
                    "Failed to create user."
                );
            }

            try (ResultSet keys =
                     statement.getGeneratedKeys()) {

                if (keys.next()) {
                    long newId = keys.getLong(1);

                    return new User(
                        newId,
                        name,
                        email,
                        passwordHash
                    );
                }
            }

            throw new SQLException(
                "Could not retrieve the new user ID."
            );
        }
    }

    
    // Convert database row to user.
    private User mapUser(ResultSet result)
            throws SQLException {

        return new User(
            result.getLong("id"),
            result.getString("name"),
            result.getString("email"),
            result.getString("password_hash")
        );
    }
}
