import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DocumentsDAO {

    public static final int MAX_PDF_BYTES = 20 * 1024 * 1024;

    public void saveDocument(long userID, String fileName, byte[] pdfData)
            throws SQLException {

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
            "INSERT INTO documents (userID, fileName, pdfData, fileSizeBytes) "
            + "VALUES (?, ?, ?, ?)";

        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userID);
            statement.setString(2, fileName);
            statement.setBytes(3, pdfData);
            statement.setLong(4, pdfData.length);

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Failed to save the document.");
            }
        }
    }
