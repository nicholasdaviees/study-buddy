// Endpoint: POST /studybuddy/api/quizzes
public class QuizServlet {

    private QuizService quizService; // performs quiz generation, retrieving, and deleting quizzes

    public QuizServlet(QuizService quizService) { // connects this servelt to quizservice
        this.quizService = quizService;
    }
    // Starts true/false generation through QuizService.
    public String doPost(String materialId, String questionType) {

        return quizService.startQuizGeneration(materialId, questionType);
    }

    // Returns saved quiz questions through QuizService.
    public String doGet(String materialId) {
        return quizService.getSavedQuizzes(materialId);
    }

    // Deletes one saved quiz question through QuizService.
    public void doDelete(String quizId) { // this stays as void because it is performing an action, rather than sending something back to the user
        quizService.deleteQuiz(quizId);
    }
}
