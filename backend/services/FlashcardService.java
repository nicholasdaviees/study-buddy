package services;

import ollama.LlmService;
import java.sql.SQLException;
import database.FlashcardsDAO;
import org.json.JSONArray;
import org.json.JSONObject;
import database.Flashcards;
import java.util.List;


public class FlashcardService {

    private MaterialService materialService; // provides access to uploaded study material
    private LlmService llmService; // sends uploaded material to local llm for flashcard generation
    private TaskManager taskManager;
    private FlashcardsDAO flashcardsDAO; // connects this to the flashcard database methods

    //creates flashcard generator with required services it depends on
    public FlashcardService(MaterialService materialService, LlmService llmService,
            TaskManager taskManager, FlashcardsDAO flashcardsDAO) {
        this.materialService = materialService; // saves the material service for getting uploaded text 
        this.llmService = llmService; // saves the llmservice for generating flashcards
        this.taskManager = taskManager; // saves the task manager for running work in background
        this.flashcardsDAO = flashcardsDAO;
    }

    // Places a flashcard-generation task in the background task manager.
    public String startFlashcardGeneration(long userId, String materialId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("A valid user ID is required.");
        }
        
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
        return taskManager.submitTask("FLASHCARD_GENERATION", () -> generateFlashcards(userId, materialId));
    }

    // Retrieves user-owned material and requests flashcard JSON.
    public String generateFlashcards(long userId, String materialId) {

        // prevents generating flashcards when no material/user id present
        if (userId <= 0 || materialId == null || materialId.trim().isEmpty()) {
            throw new IllegalArgumentException("A valid user ID and material ID are required.");
        }

        // retrives extracted text for the selected material
        String uploadedMaterial = materialService.getMaterialText(userId, materialId);

        // prevents LLm from receiving empty material
        if (uploadedMaterial == null || uploadedMaterial.trim().isEmpty()) {
            throw new IllegalStateException("Uploaded material unavailable.");
        }

        // requests flashcard using only material given
        return llmService.generateStudyMaterial(uploadedMaterial, "flashcards");
        
    }

    // Validates generated JSON and saves valid flashcards.
    public void validateAndSaveFlashcards(long userId, long materialID, String flashcardJson) {
        if (flashcardJson == null || flashcardJson.trim().isEmpty()) {
            throw new IllegalArgumentException("Flashcard JSON cannot be empty."); // this makes sure the LLM returned the JSON

        }
        JSONObject flashcardObject = new JSONObject(flashcardJson); // converts the text into a JSON object
        
        JSONArray flashcards = flashcardObject.getJSONArray("flashcards"); // gets the flashcard array

        try {
            for (int i = 0; i < flashcards.length(); i++) { // loops through every flashcard
                JSONObject flashcard = flashcards.getJSONObject(i); // one flashcard

                String frontText = flashcard.getString("question"); // the front shows the question

                String backText = flashcard.getString("answer"); // back of card shows answer

                flashcardsDAO.saveFlashcard(userId, materialID, frontText, backText); // saves the flashcard
            }
        } catch (SQLException error) {
            throw new IllegalStateException("Flashcards couldn't be saved.", error); // throws error if database couldnt save flashcards
        }


    }

    // Returns saved flashcards for the selected material.
    public String getSavedFlashcards(long userId, long materialID) {
        JSONArray flashcardArray = new JSONArray(); // creates json array that holds the saved flashcards

        try {
            List<Long> cardIDs = flashcardsDAO.getFlashcardIDsByUserIDAndMaterialID(userId, materialID); // gets id of ALL flashcards tied to user

            for (long cardID : cardIDs) { // loops through every flashcard id
                Flashcards flashcard = flashcardsDAO.getFlashcardByID(cardID, userId); // gets a flashcard from the database

                if (flashcard != null) {
                    JSONObject flashcardObject = new JSONObject(); // creates JSON object for this flashcard

                    flashcardObject.put("id", flashcard.cardID); // adds the database ID to the JSON

                    flashcardObject.put("question", flashcard.frontText); // adds the front of the flashcard

                    flashcardObject.put("answer", flashcard.backText); // adds the back of the flashcard

                    flashcardArray.put(flashcardObject); // adds this flashcard to the array


                }
            }
        } catch (SQLException error) {
            throw new IllegalStateException("Flashcards couldn't be retrieved.", error); //sends message if flashcard couldnt be retrieved from database
        }

        JSONObject response = new JSONObject(); // creates final response object

        response.put("flashcards", flashcardArray); // puts all flashcards in the flashcard array

        return response.toString(); // returns JSON response as a text
    }

    // Deletes one saved flashcard.
    public void deleteFlashcard(long userId, long cardID) {
        try {
            boolean deleted = flashcardsDAO.deleteFlashcardByID(cardID, userId); // deletes one flashcard

            if (!deleted) {
                throw new IllegalArgumentException("Flashcard wasn't found."); // reports error if it wasn't found

            }
        } catch (SQLException error) {
            throw new IllegalStateException("Flashcard couldn't be deleted.", error); // sends message if flashcard couldnt be deleted from database
        }
    }
}
