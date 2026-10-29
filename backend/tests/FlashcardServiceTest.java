// Tests flashcard generation, validation, persistence, and deletion.

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertNotNull;


public class FlashcardServiceTest {

        @Test
        public void generatesFlashcardsFromUploadedMaterial() {

            MaterialService materialService = new MaterialService() {
                @Override 
                public String getMaterialText(String materialId) {
                    return "This is sample study material";
                }
            };

            LlmService llmService = new LlmService() {
                @Override 
                public String generateStudyMaterial(String materialText, String studyMaterialType) {
                    return "{\"flashcards\":[{\"question\":\"Test question\",\"answer\":\"Test answer\"}]}";
                }
                };

                FlashcardService service = new FlashcardService(materialService, llmService);
                String result = service.generateFlashcards("material-1");
                assertEquals("{\"flashcards\":[{\"question\":\"Test question\",\"answer\":\"Test answer\"}]}", result);
             }

        @Test
        public void rejectsBlankMaterialId() {
            FlashcardService service = new FlashcardService(null, null);
            assertThrows(IllegalArgumentException.class, () -> service.generateFlashcards(""));
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
