public class QuizService {

    private MaterialService materialService; // provides access to uploaded study material
    private LlmService llmService; // sends uploaded material to local llm for quiz generation
    
    // this connects the quizservice to the other backednd services
    public QuizService(MaterialService materialService, LlmService llmService) {
        this.materialService = materialService; // saves the material service for getting uploaded text 
        this.llmService = llmService; // saves the llmservice for generating quizzes

    // Places a quiz-generation task in the background task manager.
    public String startQuizGeneration(String materialId, String questionType) {
        return null;
    }

    // Generates multiple-choice or true/false questions from uploaded material.
    public String generateQuiz(String materialId, String questionType) {
       
       
        if (materialId == null || materialId.trim().isEmpty()) { // this makes sure that the material ID was provided
            throw new IllegalArgumentException("Need Material ID")
        }
        if (questionType == null || questionType.trim().isEmpty()) { // this makes sure that the quiz type was provided
            throw new IllegalArgumentException("Question type is required.") // so this tells the program if its multiple-choice, or true/false
        }
        String uploadedMaterial = materialService.getMaterialText(materialId); // gets the text that was uploaded from the pdf

        if (uploadedMaterial == null || uploadedMaterial.trim().isEmpty()) { // prevents the llm from making questions without any provided pdf
            throw new IllegalArgumentException("Uploaded material is unavailable");
    }
        return llmService.generateStudyMaterial(uploadedMaterial, questionType) // asks the llm to create the quiz from the uploaded pdf
    }

    // Validates generated JSON and saves valid quiz questions.
    public void validateAndSaveQuiz(String materialId, String quizJson, String questionType) {
    }

    // Returns saved quiz questions for the selected material.
    public String getSavedQuizzes(String materialId) {
        return null;
    }

    // Deletes one saved quiz question.
    public void deleteQuiz(String quizId) {
    }
}
