public class QuizService {

    // Places a quiz-generation task in the background task manager.
    public String startQuizGeneration(String materialId, String questionType) {
        return null;
    }

    // Generates multiple-choice or true/false questions from uploaded material.
    public String generateQuiz(String materialId, String questionType) {
        return null;
    }

    // Validates generated JSON and saves valid quiz questions.
    public void validateAndSaveQuiz(String materialId, String quizJson, String questionType) {
    }

    // Returns saved quiz questions for the selected material.
    public String getSavedQuizzes(String materialId) {
        return null;
    }

    // Deletes one saved quiz question.
    public void deleteQuiz(String quizId) {
    }
}
