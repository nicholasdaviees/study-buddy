public class FlashcardService {

    private MaterialService materialService; // provides access to uploaded study material
    private LlmService llmService; // sends uploaded material to local llm for flashcard generation
    private TaskManager taskManager;
    //creates flashcard generator with required services it depends on
    public FlashcardService(MaterialService materialService, LlmService llmService, TaskManager taskManager) {
        this.materialService = materialService;
        this.llmService = llmService;
        this.taskManager = null;
    }

    // TODO: submit flashcard generation to task manager and return generated task ID
    // Places a flashcard-generation task in the background task manager.
    public String startFlashcardGeneration(String materialId) {
        if (materialId == null || materialId.trim().isEmpty()) {

            // throws an exception that the material ID is invalid
            throw new IllegalArgumentException("Material ID is required.");
        }
        // notifies if the taskmanager has been setup yet, throws an error if it hasnt
        if (taskManager == null) {
            throw new IllegalStateException("TaskManager is not available");
        }

        // sends flashcard-generation task to taskmanager
        // lambda waits to call generateflashcards() until worker thread runs the task
        return taskManager.submitTask("FLASHCARD_GENERATION", () -> generateFlashcards(materialId));
    }

    // retrieves uploaded material and requests flashcard JSON from the llm service
    public String generateFlashcards(String materialId) {

        // prevents generating flashcards when no material id present
        if (materialId == null || materialId.trim().isEmpty()) {
            throw new IllegalArgumentException("Need Material ID.");
        }

        // retrives extracted text for the selected material
        String uploadedMaterial = materialService.getMaterialText(materialId);

        // prevents LLm from receiving empty material
        if (uploadedMaterial == null || uploadedMaterial.trim().isEmpty()) {
            throw new IllegalStateException("Uploaded material unavailable.");
        }

        // requests flashcard using only material given
        return llmService.generateStudyMaterial(uploadedMaterial, "flashcards");
        
    }

    // Validates generated JSON and saves valid flashcards.
    public void validateAndSaveFlashcards(String materialId, String flashcardJson) {
    }

    // Returns saved flashcards for the selected material.
    public String getSavedFlashcards(String materialId) {
        return null;
    }

    // Deletes one saved flashcard.
    public void deleteFlashcard(String flashcardId) {
    }
}
