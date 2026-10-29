public class FlashcardService {

    private MaterialService materialService;
    private LlmService llmService;

    public FlashcardService(MaterialService materialService, LlmService llmService) {
        this.materialService = materialService;
        this.llmService = llmService;
    }

    // Places a flashcard-generation task in the background task manager.
    public String startFlashcardGeneration(String materialId) {
        return null;
    }

    // Generates flashcards from uploaded material only.
    public String generateFlashcards(String materialId) {
        if (materialId == null || materialId.trim().isEmpty()) {
            throw new IllegalArgumentException("Need Material ID.");
        }
        String uploadedMaterial = materialService.getMaterialText(materialId);
        if (uploadedMaterial == null || uploadedMaterial.trim().isEmpty()) {
            throw new IllegalStateException("Uploaded material unavailable.");
        }

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
