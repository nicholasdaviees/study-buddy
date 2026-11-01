// Tests flashcard generation, validation, persistence, and deletion.

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertNotNull;

// tests flashcard generation and verifies uploaded material
public class FlashcardServiceTest {

        // verifies uploaded material is sent to the LLM and that generated flashcard JSON is returned
        @Test
        public void generatesFlashcardsFromUploadedMaterial() {

            // creates fake materialservice for testing
            MaterialService materialService = new MaterialService() {
                @Override 
                public String getMaterialText(String materialId) {

                    // returns sample material, havent accessed database yet
                    return "This is sample study material";
                }
            };

            // creates fake llm for testing
            LlmService llmService = new LlmService() {
                @Override 
                public String generateStudyMaterial(String materialText, String studyMaterialType) {

                    // confirms correct material was sent to llm
                    assertEquals("This is sample study material", materialText);

                    // confirms flashcard generation was requested
                    assertEquals("flashcards", studyMaterialType);

                    // returns the sample JSON insteading of reaching out to the LLM
                    return "{\"flashcards\":[{\"question\":\"Test question\",\"answer\":\"Test answer\"}]}";
                }
                };

                // creates flashcard service using fake dependencies like the material service and llmservice
                FlashcardService service = new FlashcardService(materialService, llmService);

                // generates flashcard using sample id
                String result = service.generateFlashcards("material-1");

                // confirms that the expected JSOn was returned
                assertEquals("{\"flashcards\":[{\"question\":\"Test question\",\"answer\":\"Test answer\"}]}", result);
             }

        // verifies generation rejected when uploading empty material id
        @Test
        public void rejectsBlankMaterialId() {

            // dependencies null because method should reject invalid material id before using either dependency
            FlashcardService service = new FlashcardService(null, null);

            // confirms illegal argument exception thrown when material id empty
            assertThrows(IllegalArgumentException.class, () -> service.generateFlashcards(""));
        }

        // verifies that the generation was rejected when uploaded material is missing 
        @Test 
        public void rejectsMissingUploadedMaterial() {

            // fake material service that returns empty material
            MaterialService materialService = new MaterialService() {
                @Override 
                public String getMaterialText(String materialId) {
                    return "";
            }
        };

        // LLM not needed because the request should be rejected BEFORE the llm is called; if invalid id then llm doesnt know which material to use
        FlashcardService service = new FlashcardService(materialService, null);

        // confirms missing material causes an exception error
        assertThrows(IllegalStateException.class, () -> service.generateFlashcards("material-1"));

}
}
    
/* 
    @Test
    public void startsFlashcardGeneration() {
        FlashcardService service = new FlashcardService();

        String taskId =
                service.startFlashcardGeneration("material-1");

        assertNotNull(taskId);
    }
}

public class FlashcardServiceTest {

    // Verifies flashcard generation returns a task ID.
    public void startsFlashcardGeneration() {
    }

    // Verifies generation is blocked before material is ready.
    public void preventsGenerationBeforeMaterialIsReady() {
    }

    // Verifies valid flashcard JSON is saved.
    public void validatesAndSavesValidFlashcards() {
    }

    // Verifies invalid JSON is retried once and then fails safely.
    public void retriesInvalidFlashcardJsonOnce() {
    }

    // Verifies generated flashcards are based on uploaded material.
    public void verifiesFlashcardsUseUploadedMaterial() {
    }

    // Verifies saved flashcards can be retrieved after a page refresh.
    public void retrievesSavedFlashcards() {
    }

    // Verifies one flashcard can be deleted without deleting unrelated cards.
    public void deletesRequestedFlashcard() {
    }
}
*/
