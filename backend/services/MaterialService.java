import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;

public class MaterialService {

    private static final Logger LOGGER =
            Logger.getLogger(MaterialService.class.getName());

    private final TaskManager taskManager;
    private final UploadValidator uploadValidator = new UploadValidator();

    public MaterialService(TaskManager taskManager) {
        this.taskManager = Objects.requireNonNull(taskManager, "taskManager");
    }

    // Queues a temporary upload for processing and returns the task ID.
    public String startPdfProcessing(
            String filePath,
            String fileName,
            String contentType,
            long fileSizeBytes) {

        uploadValidator.validateFile(filePath, fileName, contentType, fileSizeBytes);
        Path temporaryFile = Path.of(filePath);

        return taskManager.submitTask("PDF_PROCESSING", () -> {
            try {
                return processPdf(filePath, fileName, contentType, fileSizeBytes);
            } finally {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException error) {
                    LOGGER.log(Level.WARNING, "Unable to delete temporary PDF.", error);
                }
            }
        });
    }

    // Extracts the text, saves the material, and returns the material ID.
    public String processPdf(
            String filePath,
            String fileName,
            String contentType,
            long fileSizeBytes) {

        uploadValidator.validateFile(filePath, fileName, contentType, fileSizeBytes);

        String extractedText = extractTextFromPdf(filePath);
        return saveMaterial(fileName, extractedText, "READY");
    }

    private String extractTextFromPdf(String filePath) {
        try (PDDocument document = Loader.loadPDF(Path.of(filePath).toFile())) {
            String text = new PDFTextStripper().getText(document).strip();

            if (text.isEmpty()) {
                throw new IllegalArgumentException(
                        "The PDF has no extractable text. Try a PDF with selectable text.");
            }

            return text;
        } catch (InvalidPasswordException error) {
            throw new IllegalArgumentException("The PDF is password protected.", error);
        } catch (IOException error) {
            throw new IllegalArgumentException("The uploaded PDF could not be read.", error);
        }
    }

    public String getMaterialStatus(String materialId) {
        throw new UnsupportedOperationException("Material status lookup is pending database integration.");
    }

    public void requireReadyMaterial(String materialId) {
        if (!"READY".equals(getMaterialStatus(materialId))) {
            throw new IllegalStateException("The material is not ready for generation.");
        }
    }

    public String saveMaterial(String fileName, String extractedText, String status) {
        throw new UnsupportedOperationException("Saving materials is pending database integration.");
    }

    public String getMaterialText(String materialId) {
        throw new UnsupportedOperationException("Material text lookup is pending database integration.");
    }

    public void updateMaterialStatus(String materialId, String status) {
        throw new UnsupportedOperationException("Material status updates are pending database integration.");
    }
}