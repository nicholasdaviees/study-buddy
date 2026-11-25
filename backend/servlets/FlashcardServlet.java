package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import services.FlashcardService;


@WebServlet("/api/flashcards/*")
public class FlashcardServlet extends HttpServlet {

    private final ObjectMapper json = new ObjectMapper(); // Converts Java objects into JSON responses
    private FlashcardService flashcardService; // Provides flashcard generation, retrieval, and deletion

    // Gets the FlashcardService that was registered by the application
    @Override
    public void init() throws ServletException {
        Object service =
                getServletContext().getAttribute("flashcardService");

        if (!(service instanceof FlashcardService)) {
            throw new ServletException(
                    "FlashcardService was not registered."
            );
        }

        flashcardService = (FlashcardService) service;
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
