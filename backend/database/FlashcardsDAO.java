package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FlashcardsDAO {

    public void saveFlashcard(
            long userID, String frontText, String backText)
            throws SQLException {

        if (frontText == null || frontText.trim().isEmpty()) {
            throw new IllegalArgumentException("Front text cannot be blank.");
        }

        if (backText == null || backText.trim().isEmpty()) {
            throw new IllegalArgumentException("Back text cannot be blank.");
        }

        String sql =
            "INSERT INTO flashcards (userID, frontText, backText) VALUES (?, ?, ?)";

        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userID);
            statement.setString(2, frontText);
            statement.setString(3, backText);

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Failed to save the flashcard.");
            }
        }
    }

    public Flashcards getFlashcardByID(long cardID, long userID)
            throws SQLException {

        String sql =
            "SELECT cardID, userID, frontText, backText FROM flashcards "
            + "WHERE cardID = ? AND userID = ?";

        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cardID);
            statement.setLong(2, userID);

            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return null;
                }

                return new Flashcards(
                    results.getLong("cardID"),
                    results.getLong("userID"),
                    results.getString("frontText"),
                    results.getString("backText")
                );
            }
        }
    }
    //untested
    public List<Long> getFlashcardIDsByUserID(long userID)
            throws SQLException {

        String sql =
            "SELECT cardID FROM flashcards WHERE userID = ? ORDER BY cardID";

        List<Long> cardIDs = new ArrayList<>();

        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userID);

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    cardIDs.add(results.getLong("cardID"));
                }
            }
        }

        return cardIDs;
    }

    public boolean deleteFlashcardByID(long cardID, long userID)
            throws SQLException {

        String sql =
            "DELETE FROM flashcards WHERE cardID = ? AND userID = ?";

        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cardID);
            statement.setLong(2, userID);

            return statement.executeUpdate() == 1;
        }
    }
}
