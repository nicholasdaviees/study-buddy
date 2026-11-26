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
import jakarta.servlet.http.HttpSession;
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
            String taskId = quizService.startQuizGeneration(
                    userId(request), materialId, questionType);
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
            Quiz quiz = quizService.getSavedQuiz(userId(request), quizId);
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

            quizService.deleteQuiz(userId(request), quizId);

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);  // 204 mean the quiz was deleted successfully

        } catch (NumberFormatException error) {
            writeJson(response,400, Map.of("error", "The quiz ID must be a number."));

        } catch (IllegalArgumentException error) {
            writeJson(response, 404, Map.of("error", error.getMessage()));

        } catch (IllegalStateException error) {
            writeJson(response, 500, Map.of("error", error.getMessage()));
        }
    }

    private String getQuizId(HttpServletRequest request) { // gets the quiz ID from the URL or query parameters
        
        String path = request.getPathInfo();

        if (path != null && path.length() > 1) {
            return path.substring(1);
        }

        String quizId = request.getParameter("quizID");

        if (quizId == null || quizId.isBlank()) {
            quizId = request.getParameter("quizId");
        }

        return quizId;
    }

    private long userId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object userId = session == null ? null : session.getAttribute("userId");
        if (!(userId instanceof Long id)) {
            throw new IllegalArgumentException("Please log in first.");
        }
        return id;
    }
 
    private void writeJson(HttpServletResponse response, int status, Object body) // sends a JSON response
    throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        json.writeValue(response.getWriter(), body);
    }
}
