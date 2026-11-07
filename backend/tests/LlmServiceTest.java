package tests;

import ollama.LlmService;
import validation.JsonResponseValidator;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// Tests communication with the local LLM
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LlmServiceTest {

    private final String testMaterial = """
                A queue is a data structure that follows first-in, first-out ordering.
                A stack follows last-in, first-out ordering.
                """;

    // Verifies that the service reports whether Ollama is reachable
    @Order(1)
    @Test
    public void checksLlmAvailability() {
        LlmService service = new LlmService();
        assumeFalse(service.isOllamaReady(), "Ollama is already running. This test starts it. Please stop Ollama before running this test.");

        Process ollama = null;
        try {
            System.out.println("\nStarting testing of Ollama start availability...");
            ollama = new ProcessBuilder("ollama", "serve")
                    .redirectErrorStream(true)
                    .start();

            System.out.println("Starting Ollama service...");
            assertTrue(waitForReadiness(service, ollama, true, 30), "Ollama did not become ready after starting");
        } catch (Exception e) {
            throw new AssertionError("Could not start Ollama", e);
        } finally {
            System.out.println("Starting testing of Ollama stop availability...");
            stopProcess(ollama);
        }

        System.out.println("Stopping Ollama service...");
        assertTrue(waitForReadiness(service, null, false, 10), "Ollama remained ready after being stopped");

        try {
            System.out.println("Restarting Ollama for rest of the tests...");
            ollama = new ProcessBuilder("ollama", "serve")
                    .redirectErrorStream(true)
                    .start();

            assertTrue(waitForReadiness(service, ollama, true, 30), "Ollama did not become ready after starting");
            System.out.println("Restart success");
        } catch (Exception e) {
            throw new AssertionError("Could not start Ollama", e);
        }
    }

    // Tests multiple-choice quiz generation requests
    @Order(2)
    @Test
    public void generatesMultipleChoiceStudyMaterial() {
        LlmService service = new LlmService();
        assumeTrue(service.isOllamaReady(), "Ollama is not running");
        System.out.println("\nStarting testing of multiple-choice quiz generation...");
        String multipleChoice = service.generateStudyMaterial(testMaterial, "multiple-choice");
        JsonResponseValidator validator = new JsonResponseValidator();
        validator.validateQuizJson(multipleChoice, "multiple-choice");
        System.out.println("Testing of multiple-choice quiz generation success\n"); 
    }

    // Tests true/false generation requests
    @Order(3)
    @Test
    public void generatesTrueFalseStudyMaterial() {
        LlmService service = new LlmService();
        assumeTrue(service.isOllamaReady(), "Ollama is not running");
        System.out.println("\nStarting testing of true-false quiz generation...");
        String trueFalse = service.generateStudyMaterial(testMaterial, "true-false");
        JsonResponseValidator validator = new JsonResponseValidator();
        validator.validateQuizJson(trueFalse, "true-false");
        System.out.println("Testing of true-false quiz generation success\n"); 
    }

    // Tests flashcard generation requests
    @Order(4)
    @Test
    public void generatesFlashcardStudyMaterial() {
        LlmService service = new LlmService();
        assumeTrue(service.isOllamaReady(), "Ollama is not running");
        System.out.println("\nStarting testing of flashcard generation...");
        String flashcards = service.generateStudyMaterial(testMaterial, "flashcards");
        JsonResponseValidator validator = new JsonResponseValidator();
        validator.validateFlashcardJson(flashcards);
        System.out.println("Testing of flashcard generation success\n"); 
    }

    // Tests answer generation requests
    @Order(5)
    @Test
    public void answersQuestionFromUploadedMaterial() {
        LlmService service = new LlmService();
        assumeTrue(service.isOllamaReady(), "Ollama is not running");
        System.out.println("\nStarting testing of answer generation...");
        String answer = service.answerQuestion(testMaterial, "What ordering does a queue use?");
        JsonResponseValidator validator = new JsonResponseValidator();
        validator.validateAnswerJson(answer);
        System.out.println("Testing of answer generation success\n"); 
    }

    private boolean waitForReadiness(LlmService service, Process process, boolean expected, int timeoutSeconds) {
        long deadline = System.nanoTime() + timeoutSeconds * 1_000_000_000L;
        while (System.nanoTime() < deadline) {
            if (service.isOllamaReady() == expected) {
                return true;
            }
            if (process != null && !process.isAlive()) {
                return false;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return service.isOllamaReady() == expected;
    }

    private void stopProcess(Process process) {
        if (process == null || !process.isAlive()) {
            return;
        }

        process.destroy();
        try {
            if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                process.destroyForcibly();
                process.waitFor();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }
    }

    // Verifies Ollama failures become safe application errors.
    public void handlesLlmConnectionFailure() {
    }
}
