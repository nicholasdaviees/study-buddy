import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UploadValidatorTest {

    private final UploadValidator validator = new UploadValidator();

    public static void main(String[] args) throws IOException {
        UploadValidatorTest test = new UploadValidatorTest();
        Path file = Files.createTempFile("upload-test-", ".pdf");

        try {
            test.acceptsValidPdf(file);
            test.rejectsMissingFile(file);
            test.rejectsNonPdf(file);
            test.rejectsOversizedPdf(file);
            test.rejectsEmptyPdf(file);
            System.out.println("All upload validator tests passed.");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    public void acceptsValidPdf(Path file) {
        validator.validateFile(file.toString(), "notes.PDF", "application/pdf", 1);
        validator.validateFile(
                file.toString(), "notes.pdf", "application/pdf", 10_485_760);
    }

    public void rejectsMissingFile(Path file) {
        expectRejection("Please select a PDF file.",
                () -> validator.validateFile(null, null, null, 0));

        Path missingPath = file.resolve("missing.pdf");
        expectRejection("The uploaded file could not be found.",
                () -> validator.validateFile(
                        missingPath.toString(), "missing.pdf", "application/pdf", 1));
    }

    public void rejectsNonPdf(Path file) {
        expectRejection("Only PDF files are allowed.",
                () -> validator.validateFile(
                        file.toString(), "notes.txt", "application/pdf", 1));

        expectRejection("Only PDF files are allowed.",
                () -> validator.validateFile(
                        file.toString(), "notes.pdf", "text/plain", 1));
    }

    public void rejectsOversizedPdf(Path file) {
        expectRejection("The PDF must be 10 MB or smaller.",
                () -> validator.validateFile(
                        file.toString(), "notes.pdf", "application/pdf", 10_485_761));
    }

    public void rejectsEmptyPdf(Path file) {
        expectRejection("The uploaded file is empty.",
                () -> validator.validateFile(
                        file.toString(), "notes.pdf", "application/pdf", 0));
    }

    private void expectRejection(String expectedMessage, Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException error) {
            if (!expectedMessage.equals(error.getMessage())) {
                throw new AssertionError("Unexpected message: " + error.getMessage(), error);
            }
            return;
        }

        throw new AssertionError("Expected rejection: " + expectedMessage);
    }
}