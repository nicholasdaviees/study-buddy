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

    // Returns saved quiz questions through QuizService.
    public String doGet(String materialId) {
        return quizService.getSavedQuizzes(materialId);
    }

    // Deletes one saved quiz question through QuizService.
    public void doDelete(String quizId) { // this stays as void because it is performing an action, rather than sending something back to the user
        quizService.deleteQuiz(quizId);
    }
}
