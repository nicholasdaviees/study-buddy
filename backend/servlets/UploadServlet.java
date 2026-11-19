import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/api/material")
@MultipartConfig(
        maxFileSize = UploadValidator.MAX_FILE_SIZE_BYTES,
        maxRequestSize = UploadValidator.MAX_FILE_SIZE_BYTES + 64 * 1024
)
public class UploadServlet extends HttpServlet {

    private final ObjectMapper json = new ObjectMapper();
    private MaterialService materialService;

    @Override
    public void init() throws ServletException {
        Object service = getServletContext().getAttribute("materialService");
        if (!(service instanceof MaterialService)) {
            throw new ServletException("MaterialService was not registered.");
        }
        materialService = (MaterialService) service;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Part file;
        try {
            file = request.getPart("file");
        } catch (IllegalStateException error) {
            writeJson(response, 413, "error", "The PDF must be 10 MB or smaller.");
            return;
        } catch (ServletException error) {
            writeJson(response, 400, "error", "Send a PDF using the file upload field.");
            return;
        }

        if (file == null || file.getSubmittedFileName() == null || file.getSize() == 0) {
            writeJson(response, 400, "error", "Please select a nonempty PDF file.");
            return;
        }

        Path temporaryFile = null;
        boolean submitted = false;
        String taskId;

        try {
            temporaryFile = Files.createTempFile("studybuddy-upload-", ".pdf");
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, temporaryFile, StandardCopyOption.REPLACE_EXISTING);
            }

            taskId = materialService.startPdfProcessing(
                    temporaryFile.toString(),
                    file.getSubmittedFileName(),
                    file.getContentType(),
                    file.getSize());

            if (taskId == null || taskId.isBlank()) {
                throw new IllegalStateException("TaskManager did not return a task ID.");
            }
            submitted = true;
        } catch (IllegalArgumentException error) {
            writeJson(response, 400, "error", error.getMessage());
            return;
        } catch (IOException | RuntimeException error) {
            getServletContext().log("Unable to start PDF processing.", error);
            writeJson(response, 500, "error", "Could not start PDF processing.");
            return;
        } finally {
            if (!submitted && temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException error) {
                    getServletContext().log("Unable to delete temporary PDF.", error);
                }
            }
        }

        writeJson(response, 202, "taskId", taskId);
    }

    private void writeJson(
            HttpServletResponse response, int status, String key, String value)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        json.writeValue(response.getWriter(), Map.of(key, value));
    }
}