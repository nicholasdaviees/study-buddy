import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Locale;

public class UploadValidator {

    // Checks that the file exists, is a PDF, and is within the size limit.
    public void validateFile(
            String filePath,
            String fileName,
            String contentType,
            long fileSizeBytes) {
        if (filePath == null || fileName.isBlank()
             || fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Please select a PDF file.");
            }
        Path path;
        try{
            path = Path.of(filePath);
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("The uploaded file could not be found.");
        }
        if (fileSizeBytes <= 0) {
            throw new IllegalArgumentException("The uploaded file is empty.");
        }
        if (fileSizeBytes > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("The PDF must be 10 MB or smaller.");
        }
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")
                || !"application/pdf".equalsIgnoreCase(contentType)) {
                    throw new IllegalArgumentException("Only PDF files are allowed.");
        }
    }
}
