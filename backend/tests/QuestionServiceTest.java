package tests;

import services.QuestionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

// Test for QuestionService.
public class QuestionServiceTest {
    private QuestionService questionService;

    // Example of student and material.
    private static final long USER_ID = 1L;
    private static final String MATERIAL_ID = "101";

    // Preparing the tests.
    @BeforeEach
    void setUp() {
        QuestionService.MaterialLoader materialLoader = (userId, materialId) -> "Encapsulation hides an object's internal state.";
        QuestionService.AnswerGenerator answerGenerator = (materialText, question) -> "Encapsulation protects internal data.";
        questionService = new QuestionService(materialLoader, answerGenerator);
    }

    // Runs each test.
    @AfterEach
    void tearDown() {
        questionService.close();
    }

    // First test is blank questions.
    @Test
    void rejectsBlankQuestion() {
        assertThrows(IllegalArgumentException.class, () -> questionService.validateQuestion(""));
         assertThrows(IllegalArgumentException.class,() -> questionService.validateQuestion("   "));
         assertThrows(IllegalArgumentException.class,() -> questionService.validateQuestion(null));
    }

    // Second test is the question length.
    @Test
    void rejectsQuestionsLongerThan2000Characters() {
        String longQuestion = "A".repeat(2001);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> questionService.validateQuestion(longQuestion));
        assertEquals("Question must be 2000 characters or fewer.",exception.getMessage());
    }

    // Third test is valid questions.
    @Test
    void acceptsValidQuestion() {
        assertDoesNotThrow(() -> questionService.validateQuestion("What is encapsulation?"));
        assertDoesNotThrow(() -> questionService.validateQuestion("A".repeat(2000)));
    }

    // Fourth test is invalid user or material IDs.
    @Test
    void rejectsInvalidStudentOrMaterial() {
        assertThrows(IllegalArgumentException.class,() -> questionService.answerQuestion(0L, MATERIAL_ID, "What is encapsulation?"));
        assertThrows(IllegalArgumentException.class,() -> questionService.answerQuestion(USER_ID, "", "What is encapsulation?"));
    }

    // Fifth test is to make sure the question is answered.
    @Test
    void returnsAnswerFromGenerator() {
        String answer = questionService.answerQuestion(USER_ID, MATERIAL_ID, "What is encapsulation?");
        assertEquals("Encapsulation protects internal data.", answer);
    }
    
    // Sixth test for unsupported material.
    @Test
    void returnsFallbackWhenMaterialIsEmpty() {
        try (QuestionService emptyMaterialService = new QuestionService((userId, materialId) -> "", (text, question) -> "Should not be called")) {
            String answer = emptyMaterialService.answerQuestion(USER_ID, MATERIAL_ID, "What is encapsulation?");
            assertEquals(emptyMaterialService.createUnsupportedMaterialResponse(), answer);
        }
    }

    // Seventh test is for empty response.
    @Test
    void returnsFallbackWhenGeneratorIsBlank() {
        try (QuestionService blankAnswerService = new QuestionService((userId, materialId) -> "Study material text",(text, question) -> "   ")) {
            String answer = blankAnswerService.answerQuestion(USER_ID, MATERIAL_ID, "What is encapsulation?");
            assertEquals(blankAnswerService.createUnsupportedMaterialResponse(),answer);
        }
    }

    // Eighth test is for completed task.
    @Test
    void completedTaskReturnsAnswer() throws Exception {
        String taskId = questionService.startQuestionAnswering(USER_ID, MATERIAL_ID, "What is encapsulation?");
        QuestionService.TaskResult result = waitForTask(questionService, USER_ID, taskId);
        assertEquals("COMPLETED", result.status());
        assertEquals("Encapsulation protects internal data.", result.answer());
    }

    // Ninth test is for a failed task.
    @Test
    void failedTaskReturnsFailedStatus() 
        throws Exception {
            try (QuestionService failingService = new QuestionService((userId, materialId) -> "Study material text",(text, question) -> {
                throw new IllegalStateException("Simulated AI failure");
            })) 
            {
            String taskId = failingService.startQuestionAnswering(USER_ID, MATERIAL_ID, "What is encapsulation?");
            QuestionService.TaskResult result = waitForTask(failingService, USER_ID, taskId
            );
            assertEquals("FAILED", result.status());
            assertNull(result.answer());
        }
    }
    // Tenth test is for unknown task ID.
    @Test
    void unknownTaskReturnsNotFound() {
        QuestionService.TaskResult result = questionService.getTaskResult(USER_ID, "nonexistent-task");
        assertEquals("NOT_FOUND", result.status());
        assertNull(result.answer());
    }
}   
