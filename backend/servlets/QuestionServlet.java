package servlets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import services.QuestionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// Communicate with the frontend.
@WebServlet("/api/questions/*")
public class QuestionServlet extends HttpServlet {

    // To record errors.
    private static final Logger LOGGER = Logger.getLogger(QuestionServlet.class.getName());

    // Converts JSON to objects.
    private final ObjectMapper mapper = new ObjectMapper();

    private QuestionService questionService;

    // Gets the QuestionService when starting.
    @Override
    public void init() throws ServletException {

        Object service = getServletContext().getAttribute("questionService");

        if (service instanceof QuestionService) {
            questionService = (QuestionService) service;
        } else {

            LOGGER.warning("QuestionService has not been configured.");
            }
        }

    // Handles requests.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

            // Doesn't cache question response.
            response.setHeader("Cache-Control", "no-store");

            // Gest the ID from the login session.
            Long userId = getLoggedInUserId(request);
            
            if (userId == null) {
            sendError(response, 401, "Please log in first.");
            return;
            }

            if (questionService == null) {
            sendError(response, 503, "Question answering is not configured.");
            return;
            }

            // 
            String action = request.getPathInfo();

            try {

                if ("/start".equals(action)) {

                // Answering in the background
                QuestionRequest data = readQuestion(request);

                String taskId = questionService.startQuestionAnswering(userId, data.materialId(), data.question());

                sendJson(response, 202, Map.of(
                    "taskId", taskId,
                    "status",
                    "PROCESSING"
                    ));

                } else if ("/answer". equals(action)) {
                    
                    // The answer will be immediately with the HTTP request.
                    QuestionRequest data = readQuestion(request);

                    String answer = questionService.answerQuestion(userId, data.materialId(), data.question());
                    sendJson(response, 200, Map.of(
                        "status",
                        "COMPLETED",
                        "answer", answer
                        ));
                } else {
                sendError(response, 404, "Question endpoint not found.");
                }

                } catch (JsonProcessingException exception) {
                    
                    // Malformed JSON.
                    sendError(response, 400, "Invalid JSON request.");
                
                } catch (IllegalArgumentException exception) {

                    // Reject invalid inputs.
                    sendError(response, 400, exception.getMessage());

                } catch (IllegalStateException exception) {
                    
                    // The material couldn't load.
                    LOGGER.log(Level.SEVERE, "Question answering failed", exception);
                    sendError (response, 503, "Unable to answer the question right now.");
                    }
                }

    // Check for the task status.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
            response.setHeader("Cache-Control", "no-store");

            // Requires login account.
            Long userId = getLoggedInUserId(request);

            if (userId == null) {
                sendError(response, 401, "Please log in first.");
                return;
            }

            if (questionService == null) {
                sendError(response, 503, "Question answering is not available yet.");
                return;
            }

            if (!"/status".equals(request.getPathInfo())) {
                sendError(response, 404, "Question endpoint not found.");
                return;
            }

            String taskId = request.getParameter("taskId");

            if (taskId == null || taskId.isBlank()) {
                sendError(response, 400, "Task ID is required.");
                return;
            }

            // Check if the task belongs to the account.
            QuestionService.TaskResult result = questionService.getTaskResult(userId, taskId);

            if ("NOT_FOUND".equals(result.status())) {
                sendError(response, 404, "Task not found.");
                return;
            }

            if ("COMPLETED".equals(result.status())) {
                sendJson(response, 200, Map.of(
                    "taskId", taskId,
                    "status", result.status(),
                    "answer", result.answer()
                    ));
                    return;
            }

            sendJson(response, 200, Map.of(
            "taskId", taskId,
            "status", result.status()
            ));
        }
    
    // Check the login session.
    private Long getLoggedInUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object userId = session.getAttribute("userId");

        if (userId instanceof Long) {
            return (Long) userId;
        }

        return null;
    }

    // Converts the frontend JSON into a question.
    private QuestionRequest readQuestion(HttpServletRequest request)
        throws IOException {

            QuestionRequest data = mapper.readValue(request.getInputStream(), QuestionRequest.class);

            if (data == null) {
                throw new IllegalArgumentException("Please provide a question and material ID.");
            }

            return data;
        }

    // Sends JSON back to the frontend.
    private void sendJson(HttpServletResponse response, int status, Object body)
        throws IOException {
            response.setStatus(status);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            mapper.writeValue(response.getWriter(), body);
        }

    // Sends errors in JSON format.
    private void sendError(HttpServletResponse response, int status, String message)
        throws IOException {
            sendJson(response, status, Map.of(
                "error", message
                ));
            }
    
    // To represent the frontend.
    public record QuestionRequest(String materialId, String question){
    }
}
