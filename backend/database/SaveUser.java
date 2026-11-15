import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.ResultSet;

public class SaveUser {

    public static final class User {
        public final long userID;
        public final String name;
        public final String email;
        public final String password;

        public User(long userID, String name, String email, String password) {
            this.userID = userID;
            this.name = name;
            this.email = email;
            this.password = password;
        }
    }

public static void saveUser(
        long userID, String name, String email, String password)
        throws SQLException {

    String sql =
        "INSERT INTO users (userID, name, email, password) VALUES (?, ?, ?, ?)";

    try (Connection connection = DriverManager.getConnection(
                requiredEnv("DB_URL"),
                requiredEnv("DB_USER"),
                requiredEnv("DB_PASSWORD"));
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setLong(1, userID);
        statement.setString(2, name);
        statement.setString(3, email);
        statement.setString(4, password);
        statement.executeUpdate();
    }
}

    private static String requiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException(
                "Missing " + key + ". Copy .env.example to .env, fill it in, "
                + "and run the Save User configuration in VS Code."
            );
        }
        return value;
    }
    
    public static boolean deleteUserByID(long userID) throws SQLException {
        String sql = "DELETE FROM users WHERE userID = ?";

        try (Connection connection = DriverManager.getConnection(
                    requiredEnv("DB_URL"),
                    requiredEnv("DB_USER"),
                    requiredEnv("DB_PASSWORD"));
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userID);
            return statement.executeUpdate() == 1;
        }
    }
    public static User getUserByID(long userID) throws SQLException {
    String sql = "SELECT userID, name, email, password FROM users WHERE userID = ?";

    try (Connection connection = DriverManager.getConnection(
                requiredEnv("DB_URL"),
                requiredEnv("DB_USER"),
                requiredEnv("DB_PASSWORD"));
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

    


    public static void main(String[] args) throws SQLException {
   

        saveUser(6, "test3", "test@test.com", "pass");
        System.out.println("User saved successfully.");
        boolean deleted = deleteUserByID(1001);
        System.out.println(deleted ? "User deleted successfully." : "No user found with that ID.");
        User user = getUserByID(6);
        System.err.println(user.name);
        System.err.println(user.userID);
        System.err.println(user.email);
        System.err.println(user.password);


    }
}

