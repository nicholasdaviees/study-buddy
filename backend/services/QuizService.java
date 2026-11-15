public class QuizService {

    private TaskManager taskManager; // our quiz generation is running in the background
    private static final String TRUE_FALSE_TYPE = "true-false"; // this is one quiz type that is supported, stores the exact text to request the quiz
    private static final String MULTIPLE_CHOICE_TYPE = "multiple-choice"; // this is the other quiz type that is supported
    private MaterialService materialService; // provides access to uploaded study material
    private LlmService llmService; // sends uploaded material to local llm for quiz generation
    
    // this connects the quizservice to the other backednd services
    public QuizService(MaterialService materialService, LlmService llmService, TaskManager taskManager) {
        this.materialService = materialService; // saves the material service for getting uploaded text 
        this.llmService = llmService; // saves the llmservice for generating quizzes
        this.taskManager = taskManager; // saves task manager for background processing
    }
    // Places a quiz-generation task in the background task manager.
    public String startQuizGeneration(String materialId, String questionType) {

        if (materialId == null || materialId.trim().isEmpty()) { // checks if material id was given
            throw new IllegalArgumentException("Need Material ID.");
        }
        if (!isSupportedQuestionType(questionType)) {
            throw new IllegalArgumentException("The question type must be true/false or multiple-choice."); // this is checking if the question type was provided and is true/false or multiple-choice

        }
        if (taskManager == null) {
            throw new IllegalArgumentException("Taskmanager is currently available."); // checks if taskmanager is available
        }
        return taskManager.submitTask("QUIZ_GENERATION", () -> generateQuiz(materialId, questionType)); // this all sends the quiz task to taskmanager
    }

    // Generates multiple-choice or true/false questions from uploaded material.
    public String generateQuiz(String materialId, String questionType) {
       
       
        if (materialId == null || materialId.trim().isEmpty()) { // this makes sure that the material ID was provided
            throw new IllegalArgumentException("Need Material ID");
        }
        if (!isSupportedQuestionType(questionType)) { // this makes sure that only true/false or multiple-choice quizzies are made
            throw new IllegalArgumentException("Question type must be true/false or multiple-choice."); // so this tells the user that it must be multiple-choice, or true/false
        }
        String uploadedMaterial = materialService.getMaterialText(materialId); // gets the text that was uploaded from the pdf

        if (uploadedMaterial == null || uploadedMaterial.trim().isEmpty()) { // prevents the llm from making questions without any provided pdf
            throw new IllegalArgumentException("Uploaded material is unavailable");
    }
        return llmService.generateStudyMaterial(uploadedMaterial, questionType); // asks the llm to create the quiz from the uploaded pdf
    }

    private boolean isSupportedQuestionType(String questionType) {
        return TRUE_FALSE_TYPE.equals(questionType) || MULTIPLE_CHOICE_TYPE.equals(questionType); // this is checking whether the quiz type the user asked for is supported
        
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
