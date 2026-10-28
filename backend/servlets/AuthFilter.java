
package servlets;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter("/app")
public class AuthFilter implements Filter {
    // Intercept request to /app
    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        // Convert the general request into HTTP.
        HttpServletRequest httpRequest =
                (HttpServletRequest) request;

        HttpServletResponse httpResponse =
                (HttpServletResponse) response;

        // Check for an existing session.
        HttpSession session =
                httpRequest.getSession(false);

        // Check whether the session contains a user ID.
        boolean loggedIn =
                session != null
                && session.getAttribute("userId") instanceof Long;

        if (!loggedIn) {

            // Redirect unauthenticated students to login.
            httpResponse.sendRedirect(
                    httpRequest.getContextPath() + "/"
            );

            return;
        }

        // Continue to the protected Servlet.
        chain.doFilter(request, response);
    }
}

