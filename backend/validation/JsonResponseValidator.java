package validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonResponseValidator {

    // Validates required fields in multiple-choice or true/false quiz JSON.
    public void validateQuizJson(String quizJson, String questionType) {
    }

    // Validates that the required fields in flashcard JSON are filled out
    // Also checks syntax errors
    public void validateFlashcardJson(String flashcardJson) {
        JsonNode root = parseJsonObject(flashcardJson); // parse string into Jackson JSON object
        JsonNode flashcards = requireNonEmptyArray(root, "flashcards");

        for (JsonNode flashcard : flashcards) {
            if (!flashcard.isObject()) {
                throw new IllegalArgumentException("Each flashcard must be an object");
            }

            requireText(flashcard, "question");
            requireText(flashcard, "answer");
        }
    }

    // Validates required fields in an answer JSON response.
    public void validateAnswerJson(String answerJson) {
    }
}
