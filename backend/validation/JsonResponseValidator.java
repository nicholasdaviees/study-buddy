package validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** @brief This class validates Ollama generated JSON for quizzes, flashcards, and answers
 *
 * 1. For validating quizzes:
 * @see JsonResponseValidator#validateQuizJson(String quizJson, String questionType)
 *    @param quizJson -> Ollama generated multiple-choice or true-false quiz JSON
 *    @param questionType -> multiple-choice or true-false
 *    @return no value. Throws an exception when the JSON is invalid
 *
 * 2. For validating flashcards:
 *    @see JsonResponseValidator#validateFlashcardJson(String flashcardJson)
 *    @param flashcardJson -> Ollama generated flashcard JSON
 *    @return no value. Throws an exception when the JSON is invalid
 *
 * 3. For validating answers:
 *    @see JsonResponseValidator#validateAnswerJson(String answerJson)
 *    @param answerJson -> Ollama generated answer JSON
 *    @return no value. Throws an exception when the JSON is invalid
 */
public class JsonResponseValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /*****************************BEGIN MAIN FUNCTIONS************************************/
    // Validates required fields in multiple-choice or true/false quiz JSON
    // Also checks syntax errors
    public void validateQuizJson(String quizJson, String questionType) {
        JsonNode root = parseJsonObject(quizJson);

        // Reject invalid quiz type
        if (!"multiple-choice".equals(questionType) && !"true-false".equals(questionType)) {
            throw new IllegalArgumentException("Unsupported quiz type: " + questionType);
        }

        JsonNode questions = requireNonEmptyArray(root, "questions");
        for (JsonNode question : questions) {
            if (!question.isObject()) {
                throw new IllegalArgumentException("Each quiz question must be an object");
            }

            // Require non-empty question and type
            requireText(question, "question");
            requireText(question, "type");

            if (!questionType.equals(question.get("type").asText())) {
                throw new IllegalArgumentException("Quiz question has the wrong type");
            }

            JsonNode answer = question.get("answer");
            if (answer == null) {
                throw new IllegalArgumentException("Quiz question is missing answer");
            }

            // Validate true-false answer type
            if ("true-false".equals(questionType)) {
                if (!answer.isBoolean()) {
                    throw new IllegalArgumentException("True/false answer must be a boolean");
                }
            }
            // Validate multiple-choice answers and choices
            else {
                // Require exactly four choices
                JsonNode choices = requireNonEmptyArray(question, "choices");
                if (choices.size() != 4) {
                    throw new IllegalArgumentException("Multiple-choice questions need four choices");
                }

                // Require each choice to be a non-empty string
                for (JsonNode choice : choices) {
                    if (!choice.isTextual() || choice.asText().isBlank()) {
                        throw new IllegalArgumentException("Every choice must be a non-empty string");
                    }
                }

                // Require the answer to match one of the choices
                boolean answerMatchesChoice = false;
                if (answer.isTextual() && !answer.asText().isBlank()) {
                    for (JsonNode choice : choices) {
                        if (answer.asText().equals(choice.asText())) {
                            answerMatchesChoice = true;
                            break;
                        }
                    }
                }

                if (!answerMatchesChoice) {
                    throw new IllegalArgumentException("The correct answer must match one of the choices");
                }
            }
        }
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

            // Require non-empty question and answer
            requireText(flashcard, "question");
            requireText(flashcard, "answer");
        }
    }

    // Validates that the required fields in answer JSON are filled out
    // Also checks syntax errors
    public void validateAnswerJson(String answerJson) {
        JsonNode root = parseJsonObject(answerJson);

        JsonNode found = root.get("found");
        if (found == null || !found.isBoolean()) {
            throw new IllegalArgumentException("Answer response must contain boolean found");
        }

        // Require a non-empty answer
        requireText(root, "answer");
    }
    /*****************************END MAIN FUNCTIONS************************************/

    /*****************************BEGIN HELPER FUNCTIONS************************************/
    // Parse JSON
    private JsonNode parseJsonObject(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("JSON response cannot be blank");
        }

        try {
            JsonNode root = objectMapper.readTree(json);
            if (root == null || !root.isObject()) {
                throw new IllegalArgumentException("JSON response must contain an object");
            }
            return root;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON response is malformed", e);
        }
    }

    // Require a non-empty array field
    private JsonNode requireNonEmptyArray(JsonNode object, String fieldName) {
        JsonNode array = object.get(fieldName);
        if (array == null || !array.isArray() || array.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must be a non-empty array");
        }
        return array;
    }

    // Require a non-empty string field
    private void requireText(JsonNode object, String fieldName) {
        JsonNode field = object.get(fieldName);
        if (field == null || !field.isTextual() || field.asText().isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must be a non-empty string"
            );
        }
    }
    /*****************************END HELPER FUNCTIONS************************************/
}
