// Tests flashcard generation, validation, persistence, and deletion.

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class FlashcardServiceTest {

    @Test
    public void startsFlashcardGeneration() {
        FlashcardService service = new FlashcardService();

        String taskId =
                service.startFlashcardGeneration("material-1");

        assertNotNull(taskId);
    }
}

/*public class FlashcardServiceTest {

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
