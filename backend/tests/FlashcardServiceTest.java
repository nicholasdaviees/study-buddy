// Tests flashcard generation, validation, persistence, and deletion.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import database.Flashcards;
import database.FlashcardsDAO;
import ollama.LlmService;
import services.FlashcardService;
import services.MaterialService;
import services.TaskManager;

public class FlashcardServiceTest { // tests flashcard generation, validation, saving, retrieving, and deleting

    @Test
    public void generatesFlashcardsFromUploadedMaterial() {
    MaterialService materialService = new MaterialService(new TaskManager()) {
                    @Override
                    public String getMaterialText(String materialId) {
                        return "This is sample study material";
                    }
                };

        LlmService llmService = new LlmService() {
            @Override
            public String generateStudyMaterial(String materialText, String studyMaterialType) {
                
                assertEquals("This is sample study material", materialText);
                assertEquals("flashcards", studyMaterialType);
                return "{\"flashcards\":[{\"question\":\"Test question\",\"answer\":\"Test answer\"}]}";
            }
        };

        FlashcardService service = new FlashcardService(materialService, llmService, new TaskManager(), null, 1L);

        String result = service.generateFlashcards("material-1");

        JSONObject response = new JSONObject(result);
        JSONArray cards = response.getJSONArray("flashcards");

        assertEquals(1, cards.length());
        assertEquals("Test question", cards.getJSONObject(0).getString("question"));
        assertEquals("Test answer", cards.getJSONObject(0).getString("answer"));
    }




    @Test // checks if material uploaded is blank
    public void rejectsBlankMaterialId() {
        FlashcardService service = new FlashcardService(null, null, null, null, 1L);
        assertThrows(IllegalArgumentException.class, () -> service.generateFlashcards(""));
    }

    
    @Test // checks if material is missing
    public void rejectsMissingUploadedMaterial() {
    MaterialService materialService = new MaterialService(new TaskManager()) {
        
        @Override
        public String getMaterialText(String materialId) {
            
            return "";
        
        }
    };

        FlashcardService service = new FlashcardService(materialService, null, new TaskManager(), null, 1L);

        assertThrows(IllegalStateException.class, () -> service.generateFlashcards("material-1"));
    }

    @Test // tests flashcard generation
    public void startsFlashcardGeneration() {
    AtomicReference<Callable<String>> submittedTask = new AtomicReference<>(); // uses an atomic reference to be able to inspect task after it ran
    TaskManager fakeTaskManager = new TaskManager() {
    
        @Override
        public String submitTask(String taskType, Callable<String> task) {

            assertEquals("FLASHCARD_GENERATION", taskType);
            submittedTask.set(task);
            return "task-1";
                    }
                };

        FlashcardService service =  new FlashcardService(null, null, fakeTaskManager, null, 1L);

        String taskId = service.startFlashcardGeneration("material-1");
        assertEquals("task-1", taskId);
        assertNotNull(submittedTask.get());
    }

    @Test // test for verifying flashcards
    public void validatesAndSavesFlashcards() {
        RecordingFlashcardsDAO fakeDAO = new RecordingFlashcardsDAO();
        FlashcardService service = new FlashcardService(null, null, null, fakeDAO, 25L);
        String flashcardJson = "{\"flashcards\":[{\"question\":\"What is Java?\",\"answer\":\"A programming language\"},{\"question\":\"What is HTML?\",\"answer\":\"A markup language\"}]}";
        service.validateAndSaveFlashcards("material-1", flashcardJson);

        assertEquals(2, fakeDAO.savedCards.size());
        assertEquals(25L, fakeDAO.savedCards.get(0).userID);
        assertEquals("What is Java?", fakeDAO.savedCards.get(0).frontText);
        assertEquals("A programming language", fakeDAO.savedCards.get(0).backText);
    }

    @Test // tests if flashcard has proper json
    public void rejectsInvalidFlashcardJson() {
        FlashcardService service = new FlashcardService(null, null, null, new RecordingFlashcardsDAO(), 1L);

        assertThrows(RuntimeException.class, () -> service.validateAndSaveFlashcards( "material-1", "not valid JSON"));
    }

    @Test // tests if flashcards are retrieved
    public void retrievesSavedFlashcards() {
        
        RecordingFlashcardsDAO fakeDAO = new RecordingFlashcardsDAO();
        fakeDAO.addCard(new Flashcards(1L, 10L, "Question one", "Answer one"));
        fakeDAO.addCard(new Flashcards(2L, 10L, "Question two", "Answer two"));

        FlashcardService service = new FlashcardService(null, null, null, fakeDAO, 10L);

        String result = service.getSavedFlashcards("material-1");

        JSONObject response = new JSONObject(result);
        JSONArray cards = response.getJSONArray("flashcards");

        assertEquals(2, cards.length());
        assertEquals("Question one", cards.getJSONObject(0).getString("question"));
        assertEquals("Answer two", cards.getJSONObject(1).getString("answer"));
    }

    @Test // tests if flashcards were deleted
    public void deletesRequestedFlashcard() {
        RecordingFlashcardsDAO fakeDAO = new RecordingFlashcardsDAO();
        fakeDAO.addCard(new Flashcards(7L, 10L, "Question", "Answer"));
        FlashcardService service = new FlashcardService(null, null, null, fakeDAO, 10L);

        service.deleteFlashcard(7L);

        assertEquals(7L, fakeDAO.deletedCardID);
        assertEquals(0, fakeDAO.cards.size());
    }

    @Test // tests if flashcard isnt missing when being deleted
    public void rejectsDeletingMissingFlashcard() {
        RecordingFlashcardsDAO fakeDAO = new RecordingFlashcardsDAO();

        FlashcardService service = new FlashcardService(null, null, null, fakeDAO, 10L);

        assertThrows(IllegalArgumentException.class, () -> service.deleteFlashcard(99L));
    }


    private static class RecordingFlashcardsDAO extends FlashcardsDAO { // Fake DAO that stores cards in memory instead of using MySQL

        private final List<Flashcards> savedCards = new ArrayList<>();

        private final Map<Long, Flashcards> cards = new HashMap<>();

        private long deletedCardID = -1L;

        void addCard(Flashcards card) {
            cards.put(card.cardID, card);
        }

        @Override
        public void saveFlashcard(long userID, String frontText, String backText) 
        
        throws SQLException {

            long newID = savedCards.size() + 1;

            Flashcards card = new Flashcards(newID, userID, frontText, backText);

            savedCards.add(card);
            cards.put(newID, card);
        }

        @Override
        public List<Long> getFlashcardIDsByUserID(long userID) throws SQLException {

            List<Long> IDs = new ArrayList<>();

            for (Flashcards card : cards.values()) {
                if (card.userID == userID) {
                    IDs.add(card.cardID);
                }
            }

            return IDs;
        }

        @Override
        public Flashcards getFlashcardByID(long cardID, long userID) throws SQLException {

            Flashcards card = cards.get(cardID);

            if (card == null || card.userID != userID) {
                return null;
            }

            return card;
        }

        @Override
        public boolean deleteFlashcardByID(
                long cardID,
                long userID)
                throws SQLException {

            Flashcards card = cards.get(cardID);

            if (card == null || card.userID != userID) {
                return false;
            }

            deletedCardID = cardID;
            cards.remove(cardID);
            return true;
        }
    }
}