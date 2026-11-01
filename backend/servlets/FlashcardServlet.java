// Endpoints: POST, GET, and DELETE /studybuddy/api/flashcards

public class FlashcardServlet {

// handles flashcard generation, retrieval, and deletion
private FlashcardService flashcardService;

    // connects this servlet to flashcardservice
    public FlashcardServlet (FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    // the posts here send a request from the front end to the back end, so in our front page if a user clicks on "generate flashcards"
    // it will come here and use this method
    // generates flashcard and returns JSON result
    public String doPost(String materialId) {

        //returns result
        return flashcardService.startFlashcardGeneration(materialId);
    }

    // Returns saved flashcards through FlashcardService.
    public String doGet(String materialId) {
        return flashcardService.getSavedFlashcards(materialId);
    }
    // Deletes one saved flashcard because of flashcardid and not materialid through FlashcardService.
    public void doDelete(String flashcardId) {
        flashcardService.deleteFlashcard(flashcardId);
    }
}
