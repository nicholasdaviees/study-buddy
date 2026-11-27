package servlets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import services.FlashcardService;
import java.io.IOException;
import java.util.Map;

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
            String taskId = flashcardService.startFlashcardGeneration(
                    userId(request), materialId);
            writeJson(response, 202, Map.of("taskId", taskId));  // 202 means the task was accepted for background processing

        } catch (IllegalArgumentException error) {
            writeJson(response, 400, Map.of("error", error.getMessage()));

        } catch (IllegalStateException error) {
            writeJson(response, 503, Map.of("error", error.getMessage()));
        }
    }

    @Override // returns the saved flashcards for the user
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String materialId = request.getParameter("materialId");

    if (materialId == null || materialId.isBlank()) {
        writeJson(response, 400, Map.of("error", "A material ID is required."));
        return;
    }

    try {
        long materialID = Long.parseLong(materialId);

        String savedFlashcards = flashcardService.getSavedFlashcards(userId(request), materialID);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(savedFlashcards);

    } catch (NumberFormatException error) {
        writeJson(response, 400, Map.of("error", "The material ID must be a number."));

    } catch (IllegalArgumentException error) {
        writeJson(response, 401, Map.of("error", error.getMessage()));

    } catch (IllegalStateException error) {
        writeJson(response, 500, Map.of("error", error.getMessage()));
    }
}

    @Override // deletes one saved flashcard
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
    throws IOException {

        String cardIdText = getCardId(request);

        if (cardIdText == null || cardIdText.isBlank()) {
            writeJson(response, 400, Map.of("error", "A flashcard ID is required."));
        
            return;
    
        }

    try {
        long cardId = Long.parseLong(cardIdText);
        flashcardService.deleteFlashcard(userId(request), cardId); // deletes the card only if it belongs to the current user
        response.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204 means the card was deleted successfully

   
        } catch (NumberFormatException error) {
            writeJson(response, 400, Map.of("error", "The flashcard ID must be a number."));

    } catch (IllegalArgumentException error) {
        writeJson(response, 404, Map.of("error", error.getMessage()));

    } catch (IllegalStateException error) {
        writeJson(response, 500, Map.of("error", error.getMessage()));
    }
}

    private String getCardId(HttpServletRequest request) { // gets the flashcard ID from the URL or a query parameter
    String path = request.getPathInfo();

        if (path != null && path.length() > 1) {
            return path.substring(1);
        }

        String cardId = request.getParameter("cardID");

        if (cardId == null || cardId.isBlank()) {
            cardId = request.getParameter("flashcardId");
        }

        return cardId;
    }

    private long userId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object userId = session == null ? null : session.getAttribute("userId");
        if (!(userId instanceof Long id)) {
            throw new IllegalArgumentException("Please log in first.");
        }
        return id;
    }

    private void writeJson(HttpServletResponse response, int status, Object body) // sends a java object as a JSON response
            throws IOException {

            response.setStatus(status);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            json.writeValue(response.getWriter(), body);
    }
}















