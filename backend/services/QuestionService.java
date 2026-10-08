public class QuestionService {

    // Places a question-answering task in the background task manager.
    public String startQuestionAnswering(String materialId, String question) {
        return null;
    }

    // Answers a question using only the selected material.
    public String answerQuestion(String materialId, String question) {
        return null;
    }

    // Rejects blank or otherwise invalid user questions.
    public void validateQuestion(String question) {
    }

    // Creates the response used when the answer is absent from the material.
    public String createUnsupportedMaterialResponse() {
        return null;
    }
}
