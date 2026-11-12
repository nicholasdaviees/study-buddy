import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;

private final TaskManager taskManager;
private final UploadValidator uploadValidator = new UploadValidator();

public class MaterialService(TaskManager taskManager) {
  this.taskManager = Objects.requireNonNull(taskManager, "taskManager");
}

public String startPdfProcessing(
        String filePath,
        String fileName,
        String contentType,
        long fileSizeBytes) {

    uploadValidator.validateFile(filePath, fileName, contentType, fileSizeBytes);

    return taskManager.submitTask("PDF_PROCESSING",
            () -> processPdf(filePath, fileName, contentType, fileSizeBytes));
}

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
    try (PDDocument document = Loader.loadPDF(new File(filePath))) {
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

public void requireReadyMaterial(String materialId) {
    if (!"READY".equals(getMaterialStatus(materialId))) {
        throw new IllegalStateException("The material is not ready for generation.");
    }
}
 