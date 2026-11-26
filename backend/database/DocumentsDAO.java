package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DocumentsDAO {

    public static final int MAX_PDF_BYTES = 20 * 1024 * 1024;

    public long saveDocument(long userID, String fileName, byte[] pdfData)
            throws SQLException {
        return saveDocument(userID, fileName, pdfData, null);
    }

    public long saveDocument(long userID, String fileName, byte[] pdfData,
            String extractedText) throws SQLException {

        if (fileName == null || fileName.trim().isEmpty()
                || fileName.length() > 255) {
            throw new IllegalArgumentException(
                "Filename must contain 1 to 255 characters."
            );
        }

        if (pdfData == null || pdfData.length < 5
                || pdfData[0] != '%' || pdfData[1] != 'P'
                || pdfData[2] != 'D' || pdfData[3] != 'F'
                || pdfData[4] != '-') {
            throw new IllegalArgumentException(
                "The file must have a PDF header (%PDF-)."
            );
        }

        if (pdfData.length > MAX_PDF_BYTES) {
            throw new IllegalArgumentException(
                "PDF exceeds the 20 MiB upload limit."
            );
        }

        String sql =
            "INSERT INTO documents (userID, fileName, pdfData, extractedText, fileSizeBytes) "
            + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, userID);
            statement.setString(2, fileName);
            statement.setBytes(3, pdfData);
            statement.setString(4, extractedText);
            statement.setLong(5, pdfData.length);

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Failed to save the document.");
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Document ID was not generated.");
                }
                return keys.getLong(1);
            }
        }
    }
    
    //untested
    public Documents getDocumentByID(long docID, long userID)
            throws SQLException {

        String sql =
            "SELECT docID, userID, fileName, pdfData, extractedText, fileSizeBytes "
            + "FROM documents WHERE docID = ? AND userID = ?";

        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, docID);
            statement.setLong(2, userID);

            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return null;
                }

                return new Documents(
                    results.getLong("docID"),
                    results.getLong("userID"),
                    results.getString("fileName"),
                    results.getBytes("pdfData"),
                    results.getString("extractedText"),
                    results.getLong("fileSizeBytes")
                );
            }
        }
    }

    public List<Long> getDocumentIDsByUserID(long userID)
            throws SQLException {

        String sql =
            "SELECT docID FROM documents WHERE userID = ? ORDER BY docID";

        List<Long> documentIDs = new ArrayList<Long>();

        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userID);

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    documentIDs.add(results.getLong("docID"));
                }
            }
        }

        return documentIDs;
    }

    public boolean deleteDocumentByID(long docID, long userID)
            throws SQLException {

        String sql =
            "DELETE FROM documents WHERE docID = ? AND userID = ?";

        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, docID);
            statement.setLong(2, userID);

            return statement.executeUpdate() == 1;
        }
    }
}

