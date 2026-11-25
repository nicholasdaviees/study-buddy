package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;

import database.Quiz;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
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
