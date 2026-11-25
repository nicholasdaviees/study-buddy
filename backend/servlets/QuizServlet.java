package servlets;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import database.Quiz;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.QuizService;

@WebServlet("/api/quizzes/*") // handles quiz generation, retrieval, and deletion.
public class QuizServlet extends HttpServlet {

    private final ObjectMapper json = new ObjectMapper(); // converts java objects to JSON
    private QuizService quizService;

    @Override // gets quizservice from the app. context
    public void init() throws ServletException {
        Object service = getServletContext().getAttribute("quizService");

        if (!(service instanceof QuizService)) {
            throw new ServletException("QuizService was not registered.");
        }

        quizService = (QuizService) service;
    }

    @Override // starts quiz generation
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    
        String materialId = request.getParameter("materialId");
        String questionType = request.getParameter("questionType");

        if (materialId == null || questionType == null) { // also supports JSON request bodies
            JsonNode body = json.readTree(request.getInputStream());

            if (body != null && body.has("materialId")) {
                materialId = body.get("materialId").asText();
            }

            if (body != null && body.has("questionType")) {
                questionType = body.get("questionType").asText();
            }
        }

        try {
            String taskId = quizService.startQuizGeneration(materialId, questionType);
            writeJson(response, 202, Map.of("taskId", taskId));

        } catch (IllegalArgumentException error) {
            writeJson(response, 400, Map.of("error", error.getMessage()));

        } catch (IllegalStateException error) {
            writeJson(response, 503, Map.of("error", error.getMessage()));
        }
    }

    @Override // gets one saved quiz
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {

        String quizIdText = getQuizId(request);
        if (quizIdText == null || quizIdText.isBlank()) {
            writeJson(response, 400, Map.of("error", "A quiz ID is required."));
            return;
        }

        try {
            long quizId = Long.parseLong(quizIdText);
            Quiz quiz = quizService.getSavedQuiz(quizId);
            if (quiz == null) {
                writeJson(response, 404, Map.of("error", "Quiz not found."));
                return;
            }

            writeJson(response, 200, quiz);

        } catch (NumberFormatException error) {
            writeJson(response,400, Map.of("error", "The quiz ID must be a number."));

        } catch (IllegalStateException error) {
            writeJson(response, 500, Map.of("error", error.getMessage()));
        }
    }

    @Override // deletes a saved quiz
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
    throws IOException {

        String quizIdText = getQuizId(request);

        if (quizIdText == null || quizIdText.isBlank()) {
            writeJson(response, 400, Map.of("error", "A quiz ID is required."));
            return;
        }

        try {
            long quizId = Long.parseLong(quizIdText);

            quizService.deleteQuiz(quizId);

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);  // 204 mean the quiz was deleted successfully

        } catch (NumberFormatException error) {
            writeJson(response,400, Map.of("error", "The quiz ID must be a number."));

        } catch (IllegalArgumentException error) {
            writeJson(response, 404, Map.of("error", error.getMessage()));

        } catch (IllegalStateException error) {
            writeJson(response, 500, Map.of("error", error.getMessage()));
        }
    }






// Endpoint: POST /studybuddy/api/quizzes
public class QuizServlet { // this class handles the save, delete, and get operations for quizzes

    private QuizService quizService; // performs quiz generation, retrieving, and deleting quizzes

    public QuizServlet(QuizService quizService) { // connects this servelt to quizservice
        this.quizService = quizService;
    }
    // Starts true/false or multiple-choice generation through QuizService.
    public String doPost(String materialId, String questionType) {

        return quizService.startQuizGeneration(materialId, questionType);
    }

    
    public Quiz doGet(long quizID) { // gets a quiz by its database id
        return quizService.getSavedQuiz(quizID); // asks quizservice to retrieve quiz
    }

    // Deletes one saved quiz question through its databaseid
    public void doDelete(long quizId) { // this stays as void because it is performing an action, rather than sending something back to the user
        quizService.deleteQuiz(quizId); // quizService deletes the quiz
    }
}
