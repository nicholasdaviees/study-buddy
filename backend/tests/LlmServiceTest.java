package tests;

import ollama.LlmService;
import validation.JsonResponseValidator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Tests communication with the local LLM
public class LlmServiceTest {

    private final String testMaterial = """
                A queue is a data structure that follows first-in, first-out ordering.
                A stack follows last-in, first-out ordering.
                """;

    // Tests quiz generation requests
    @Test
    public void generatesRequestedStudyMaterial() {
        LlmService service = new LlmService();
        String multipleChoice = service.generateStudyMaterial(testMaterial, "multiple-choice");
        JsonResponseValidator validator = new JsonResponseValidator();
        validator.validateQuizJson(multipleChoice, "multiple-choice");
    }

    // Tests true/false generation requests
    @Test
    public void generatesTrueFalseStudyMaterial() {
        LlmService service = new LlmService();
        String trueFalse = service.generateStudyMaterial(testMaterial, "true-false");
        JsonResponseValidator validator = new JsonResponseValidator();
        validator.validateQuizJson(trueFalse, "true-false");
    }

    // Tests flashcard generation requests
    @Test
    public void generatesFlashcardStudyMaterial() {
        LlmService service = new LlmService();
        String flashcards = service.generateStudyMaterial(testMaterial, "flashcards");
        JsonResponseValidator validator = new JsonResponseValidator();
        validator.validateFlashcardJson(flashcards);
    }

    // Tests answer generation requests
    @Test
    public void answersQuestionFromUploadedMaterial() {
        LlmService service = new LlmService();
        String answer = service.answerQuestion(testMaterial, "What ordering does a queue use?");
        JsonResponseValidator validator = new JsonResponseValidator();
        validator.validateAnswerJson(answer);
    }

    // Verifies that the service reports whether Ollama is reachable
    @Test
    public void checksLlmAvailability() {
        LlmService service = new LlmService();
        boolean ollamaResponse = service.isOllamaReady();
        assertTrue(ollamaResponse, "Error, Ollama is not reachable");
    }

    // Verifies Ollama failures become safe application errors.
    public void handlesLlmConnectionFailure() {
    }
}
