package servlets;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.FlashcardService;


@WebServlet("/api/flashcards/*")
public class FlashcardServlet extends HttpServlet {

    private final ObjectMapper json = new ObjectMapper(); // converts Java objects into JSON responses
    private FlashcardService flashcardService; // provides flashcard generation, retrieval, and deletion

    // gets the FlashcardService that was registered by the application
    @Override
    public void init() throws ServletException {
    Object service = getServletContext().getAttribute("flashcardService");

        if (!(service instanceof FlashcardService)) {
            throw new ServletException("FlashcardService was not registered.");
        }

        flashcardService = (FlashcardService) service;
    }

    @Override  // starts flashcard generation for a material ID
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException {

        String materialId = request.getParameter("materialId");

        if (materialId == null || materialId.isBlank()) { // this also supports a JSON request body such as {"materialId":"material-1"}
            JsonNode body = json.readTree(request.getInputStream());

            if (body != null && body.has("materialId")) {
                materialId = body.get("materialId").asText();
            }
        }

        try {
            String taskId = flashcardService.startFlashcardGeneration(materialId);
            writeJson(response, 202, Map.of("taskId", taskId));  // 202 means the task was accepted for background processing

        } catch (IllegalArgumentException error) {
            writeJson(response, 400, Map.of("error", error.getMessage()));

        } catch (IllegalStateException error) {
            writeJson(response, 503, Map.of("error", error.getMessage()));
        }
    }

    @Override // returns the saved flashcards for the current user
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
            String materialId = request.getParameter("materialId");

        try {
            String savedFlashcards = flashcardService.getSavedFlashcards(materialId);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(savedFlashcards);

        } catch (IllegalStateException error) {
            writeJson(response, 500, Map.of("error", error.getMessage())
            );
        }
    }

















    // Returns saved flashcards through FlashcardService.
    public String doGet(String materialId) {
        return flashcardService.getSavedFlashcards(materialId);
    }
    // Deletes one saved flashcard because of flashcardid and not materialid through FlashcardService.
    public void doDelete(long cardID) {
        flashcardService.deleteFlashcard(cardID);
    }
}
