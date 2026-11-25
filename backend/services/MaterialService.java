package services;

import database.Documents;
import database.DocumentsDAO;
import services.TaskManager;
import validation.UploadValidator;
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
    private final DocumentsDAO documentsDAO;
    private final UploadValidator uploadValidator = new UploadValidator();

    public MaterialService(TaskManager taskManager) {
        this.taskManager = Objects.requireNonNull(taskManager, "taskManager");
        this.documentsDAO = new DocumentsDAO();
    }

    // Queues a temporary upload for processing and returns the task ID.
    public String startPdfProcessing(
            long userId,
            String filePath,
            String fileName,
            String contentType,
            long fileSizeBytes) {

        if (userId <= 0) {
            throw new IllegalArgumentException("A valid user ID is required.");
        }
        uploadValidator.validateFile(filePath, fileName, contentType, fileSizeBytes);
        Path temporaryFile = Path.of(filePath);

        return taskManager.submitTask("PDF_PROCESSING", () -> {
            try {
                return processPdf(userId, filePath, fileName, contentType, fileSizeBytes);
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
            long userId,
            String filePath,
            String fileName,
            String contentType,
            long fileSizeBytes) {

        if (userId <= 0) {
            throw new IllegalArgumentException("A valid user ID is required.");
        }
        uploadValidator.validateFile(filePath, fileName, contentType, fileSizeBytes);

        String extractedText = extractTextFromPdf(filePath);
        try {
            return String.valueOf(documentsDAO.saveDocument(userId, fileName,
                    Files.readAllBytes(Path.of(filePath)), extractedText));
        } catch (IOException | java.sql.SQLException error) {
            throw new IllegalStateException("The uploaded material could not be saved.", error);
        }
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

    public String getMaterialText(long userId, String materialId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("A valid user ID is required.");
        }
        try {
            Documents document = documentsDAO.getDocumentByID(
                    Long.parseLong(materialId), userId);
            if (document == null || document.extractedText == null
                    || document.extractedText.isBlank()) {
                throw new IllegalArgumentException("Material was not found.");
            }
            return document.extractedText;
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("Material ID is invalid.", error);
        } catch (java.sql.SQLException error) {
            throw new IllegalStateException("Material could not be loaded.", error);
        }
    }

    public void updateMaterialStatus(String materialId, String status) {
        throw new UnsupportedOperationException("Material status updates are pending database integration.");
    }
}
