package validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonResponseValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Validates required fields in multiple-choice or true/false quiz JSON
    public void validateQuizJson(String quizJson, String questionType) {
        JsonNode root = parseJsonObject(quizJson);

        if (!"multiple-choice".equals(questionType) && !"true-false".equals(questionType)) {
            throw new IllegalArgumentException("Unsupported quiz type: " + questionType);
        }

        JsonNode questions = requireNonEmptyArray(root, "questions");
        for (JsonNode question : questions) {
            if (!question.isObject()) {
                throw new IllegalArgumentException("Each quiz question must be an object");
            }

            requireText(question, "question");
            requireText(question, "type");

            if (!questionType.equals(question.get("type").asText())) {
                throw new IllegalArgumentException("Quiz question has the wrong type");
            }

            JsonNode answer = question.get("answer");
            if (answer == null) {
                throw new IllegalArgumentException("Quiz question is missing answer");
            }

            if ("true-false".equals(questionType)) {
                if (!answer.isBoolean()) {
                    throw new IllegalArgumentException("True/false answer must be a boolean");
                }
            } else {
                validateMultipleChoiceQuestion(question, answer);
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

            requireText(flashcard, "question");
            requireText(flashcard, "answer");
        }
    }

    // Validates required fields in an answer JSON response.
    public void validateAnswerJson(String answerJson) {
        JsonNode root = parseJsonObject(answerJson);

        JsonNode found = root.get("found");
        if (found == null || !found.isBoolean()) {
            throw new IllegalArgumentException("Answer response must contain boolean found");
        }

        requireText(root, "answer");
        requireText(root, "source");
    }

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

    private JsonNode requireNonEmptyArray(JsonNode object, String fieldName) {
        JsonNode array = object.get(fieldName);
        if (array == null || !array.isArray() || array.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must be a non-empty array");
        }
        return array;
    }

    private void requireText(JsonNode object, String fieldName) {
        JsonNode field = object.get(fieldName);
        if (field == null || !field.isTextual() || field.asText().isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must be a non-empty string"
            );
        }
    }

    private void validateMultipleChoiceQuestion(JsonNode question, JsonNode answer) {
        JsonNode choices = requireNonEmptyArray(question, "choices");
        if (choices.size() != 4) {
            throw new IllegalArgumentException("Multiple-choice questions need four choices");
        }

        for (JsonNode choice : choices) {
            if (!choice.isTextual() || choice.asText().isBlank()) {
                throw new IllegalArgumentException("Every choice must be a non-empty string");
            }
        }

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
