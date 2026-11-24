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
import services.TaskManager;

// Endpoint: GET /studybuddy/api/tasks/{taskId}
@WebServlet("/api/tasks/*")
public class TaskStatusServlet extends HttpServlet {

    private final ObjectMapper json = new ObjectMapper();
    private TaskManager taskManager;

    @Override
    public void init() throws ServletException {
        Object manager = getServletContext().getAttribute("taskManager");
        if (!(manager instanceof TaskManager)) {
            throw new ServletException("TaskManager was not registered.");
        }
        taskManager = (TaskManager) manager;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setHeader("Cache-Control", "no-store");

        String path = request.getPathInfo();
        if (path == null || path.length() <= 1 || path.indexOf('/', 1) >= 0) {
            writeJson(response, 400, Map.of("error", "Task ID is required."));
            return;
        }

        try {
            JsonNode task = json.readTree(taskManager.getTask(path.substring(1)));
            writeJson(response, "NOT_FOUND".equals(task.path("status").asText()) ? 404 : 200, task);
        } catch (IllegalArgumentException error) {writeJson(response, 400, Map.of("error", error.getMessage()));}
    }

    private void writeJson(HttpServletResponse response, int status, Object body)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        json.writeValue(response.getWriter(), body);
    }
}
