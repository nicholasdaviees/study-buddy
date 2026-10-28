
package servlets;

import services.AuthService;
import database.User;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

// Sends request to /app
@WebServlet("/app")
public class AccountServlet extends HttpServlet {

    private final AuthService authService =
            new AuthService();
            
    // Handle account page request
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        // Avoid caching private account information.
        response.setHeader("Cache-Control", "no-store");

        // Get the existing session.
        HttpSession session = request.getSession(false);

        if (session == null ||
                !(session.getAttribute("userId") instanceof Long)) {

            response.sendRedirect(
                    request.getContextPath() + "/"
            );
            return;
        }

        try {
            // Get the logged-in student's ID.
            Long userId =
                    (Long) session.getAttribute("userId");

            // Find the student in MySQL.
            User user = authService.findById(userId);

            if (user == null) {
                session.invalidate();

                response.sendRedirect(
                        request.getContextPath() + "/"
                );
                return;
            }

            // Make account information available to JSP.
            request.setAttribute(
                    "accountName", user.getName()
            );

            request.setAttribute(
                    "accountEmail", user.getEmail()
            );

            // Display the account page.
            request.getRequestDispatcher(
                    "/WEB-INF/account.jsp"
            ).forward(request, response);

        } catch (SQLException exception) {
            log("Failed to load student account", exception);

            response.sendError(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Account information is temporarily unavailable."
            );
        }
    }
}


