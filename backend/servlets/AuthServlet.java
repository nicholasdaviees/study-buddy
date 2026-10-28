

package servlets;

import database.User;
import services.AuthService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// Handles authentication request from the frontend
@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private static final Logger LOGGER =
        Logger.getLogger(AuthServlet.class.getName());

    private final ObjectMapper mapper = new ObjectMapper();
    private final AuthService authService = new AuthService();

    // Handles POST requests.
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        // Prevent sensitive responses from being cached.
        response.setHeader("Cache-Control", "no-store");

        String action = request.getPathInfo();

        try {
            if ("/signup".equals(action)) {
                signup(request, response);

            } else if ("/login".equals(action)) {
                login(request, response);

            } else if ("/logout".equals(action)) {
                logout(request, response);

            } else {
                sendError(response, 404, "Endpoint not found.");
            }

        } catch (JsonProcessingException exception) {
            sendError(response, 400, "Invalid JSON request.");

        } catch (SQLIntegrityConstraintViolationException exception) {
            sendError(response, 409,
                "An account with this email already exists.");

        } catch (IllegalArgumentException exception) {
            sendError(response, 400, exception.getMessage());

        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                "Authentication database error", exception);

            sendError(response, 503,
                "Authentication is temporarily unavailable.");
        }
    }

    // Handles GET /api/auth/me.
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setHeader("Cache-Control", "no-store");

        if (!"/me".equals(request.getPathInfo())) {
            sendError(response, 404, "Endpoint not found.");
            return;
        }

        HttpSession session = request.getSession(false);

        if (session == null ||
            !(session.getAttribute("userId") instanceof Long)) {

            sendError(response, 401, "Not logged in.");
            return;
        }

        try {
            Long userId =
                (Long) session.getAttribute("userId");

            User user = authService.findById(userId);

            if (user == null) {
                session.invalidate();

                sendError(response, 401,
                    "Session is no longer valid.");
                return;
            }

            sendJson(response, 200, Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail()
            ));

        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                "Unable to load current user", exception);

            sendError(response, 503,
                "Authentication is temporarily unavailable.");
        }
    }

    // Creates a new student account.
    private void signup(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, SQLException {

        AuthRequest data = mapper.readValue(
            request.getInputStream(),
            AuthRequest.class
        );

        if (data == null) {
            throw new IllegalArgumentException(
                "Please provide account information."
            );
        }

        User user = authService.signup(
            data.name(),
            data.email(),
            data.password(),
            data.confirmPassword()
        );

        createSession(request, user);

        sendJson(response, 201, Map.of(
            "message", "Account created successfully.",
            "userId", user.getId()
        ));
    }

    // Authenticates an existing student.
    private void login(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, SQLException {

        AuthRequest data = mapper.readValue(
            request.getInputStream(),
            AuthRequest.class
        );

        if (data == null) {
            throw new IllegalArgumentException(
                "Please enter your login information."
            );
        }

        User user = authService.login(
            data.email(),
            data.password()
        );

        if (user == null) {
            sendError(response, 401,
                "Incorrect email or password.");
            return;
        }

        createSession(request, user);

        sendJson(response, 200, Map.of(
            "message", "Login successful.",
            "userId", user.getId()
        ));
    }

    // Logs out the current student.
    private void logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        sendJson(response, 200, Map.of(
            "message", "Logged out successfully."
        ));
    }

    // Creates a new session after authentication.
    private void createSession(
            HttpServletRequest request,
            User user
    ) {

        // Invalidate any previous session.
        HttpSession previousSession =
            request.getSession(false);

        if (previousSession != null) {
            previousSession.invalidate();
        }

        HttpSession session = request.getSession(true);

        // Store only the student ID.
        session.setAttribute("userId", user.getId());

        // One hour of inactivity.
        session.setMaxInactiveInterval(3600);
    }

    // Returns data to JavaScript as JSON.
    private void sendJson(
            HttpServletResponse response,
            int status,
            Object body
    ) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        mapper.writeValue(response.getWriter(), body);
    }

    // Returns an error in the format expected by auth.js.
    private void sendError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        sendJson(response, status,
            Map.of("error", message));
    }

    // Represents the JSON sent by your frontend.
    public record AuthRequest(
        String name,
        String email,
        String password,
        String confirmPassword
    ) {}
}

