import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class MaterialServiceTest {

    public static void main(String[] args) throws Exception {
        startsPdfProcessing();
        System.out.println("PDF processing test passed.");
    }

    public static void startsPdfProcessing() throws Exception {
        Path pdf = Files.createTempFile("study-buddy-", ".pdf");

        try {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    content.newLineAtOffset(50, 700);
                    content.showText("Study Buddy notes");
                    content.endText();
                }

                document.save(pdf.toFile());
            }

            CapturingTaskManager tasks = new CapturingTaskManager();
            MaterialService service = new MaterialService(tasks) {
                @Override
                public String saveMaterial(String fileName, String extractedText, String status) {
                    if (!"notes.pdf".equals(fileName)
                            || !"Study Buddy notes".equals(extractedText)
                            || !"READY".equals(status)) {
                        throw new AssertionError("Unexpected material passed to saveMaterial.");
                    }

                    return "material-1";
                }
            };

            String taskId = service.startPdfProcessing(
                    pdf.toString(), "notes.pdf", "application/pdf", Files.size(pdf));

            if (!"task-1".equals(taskId) || tasks.pendingTask == null) {
                throw new AssertionError("Expected a task ID and queued PDF work.");
            }

            String materialId = tasks.pendingTask.call();
            if (!"material-1".equals(materialId)) {
                throw new AssertionError("Expected the background task to return the material ID.");
            }
        } finally {
            Files.deleteIfExists(pdf);
        }
    }

    private static class CapturingTaskManager extends TaskManager {
        private Callable<String> pendingTask;

        @Override
        public String submitTask(String taskType, Callable<String> task) {
            pendingTask = task;
            return "task-1";
        }
    }
}