import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

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
