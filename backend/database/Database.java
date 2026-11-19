import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {

    private Database() {
    }

    public static Connection open() throws SQLException {
        return DriverManager.getConnection(
            requiredEnv("DB_URL"),
            requiredEnv("DB_USER"),
            requiredEnv("DB_PASSWORD")
        );
    }

    private static String requiredEnv(String key) {
        String value = System.getenv(key);

        if (value == null || value.isEmpty()) {
            throw new IllegalStateException(
                "Missing environment variable: " + key
            );
        }

        return value;
    }
}
