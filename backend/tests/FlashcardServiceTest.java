// Tests flashcard generation, validation, persistence, and deletion.

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import services.MaterialService;
import services.FlashcardService;
import services.TaskManager;
import ollama.LlmService;
//import static org.junit.jupiter.api.Assertions.assertNotNull;

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



// tests flashcard generation and verifies uploaded material
public class FlashcardServiceTest {

        // verifies uploaded material is sent to the LLM and that generated flashcard JSON is returned
        @Test
        public void generatesFlashcardsFromUploadedMaterial() {

            // creates fake materialservice for testing
            MaterialService materialService = new MaterialService(new TaskManager()) {
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
                FlashcardService service = new FlashcardService(materialService, llmService, new TaskManager(), null, 1L);

                // generates flashcard using sample id
                String result = service.generateFlashcards("material-1");

                // confirms that the expected JSOn was returned
                assertEquals("{\"flashcards\":[{\"question\":\"Test question\",\"answer\":\"Test answer\"}]}", result);
             }

        // verifies generation rejected when uploading empty material id
        @Test
        public void rejectsBlankMaterialId() {

            // dependencies null because method should reject invalid material id before using either dependency
            FlashcardService service = new FlashcardService(null, null, null, null, 1L);

            // confirms illegal argument exception thrown when material id empty
            assertThrows(IllegalArgumentException.class, () -> service.generateFlashcards(""));
        }

        // verifies that the generation was rejected when uploaded material is missing 
        @Test 
        public void rejectsMissingUploadedMaterial() {

            // fake material service that returns empty material
            MaterialService materialService = new MaterialService(new TaskManager()) {
                @Override 
                public String getMaterialText(String materialId) {
                    return "";
            }
        };

        // LLM not needed because the request should be rejected BEFORE the llm is called; if invalid id then llm doesnt know which material to use
        FlashcardService service = new FlashcardService(materialService, null, new TaskManager(), null, 1L);

        // confirms missing material causes an exception error
        assertThrows(IllegalStateException.class, () -> service.generateFlashcards("material-1"));

}
        @Test 
        public void startsFlashcardGeneration() {
            // creates faketaskamanager so it doesnt depend on real taskmanager yet
            TaskManager fakeTaskManager = new TaskManager() {
                @Override 
                public String submitTask(String taskType, java.util.concurrent.Callable<String> task) {

                return "task-1";
                }
            };
        }
}
    
