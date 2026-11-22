package services;

import database.AnswerOption;
import database.Question;
import database.Quiz;
import database.QuizDAO;
import ollama.LlmService;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList; 
import org.json.JSONArray;
import org.json.JSONObject;

public class QuizService {

    private TaskManager taskManager; // our quiz generation is running in the background
    private static final String TRUE_FALSE_TYPE = "true-false"; // this is one quiz type that is supported, stores the exact text to request the quiz
    private static final String MULTIPLE_CHOICE_TYPE = "multiple-choice"; // this is the other quiz type that is supported
    private MaterialService materialService; // provides access to uploaded study material
    private LlmService llmService; // sends uploaded material to local llm for quiz generation
    private QuizDAO quizDAO; // we can save, retrive, and delete quizzes from MySQL
    private long userID; // this stores the idea of the user
    
    // this connects the quizservice to the other backednd services
    public QuizService(MaterialService materialService, LlmService llmService, TaskManager taskManager, QuizDAO quizDAO, long userID) {
        this.materialService = materialService; // saves the material service for getting uploaded text 
        this.llmService = llmService; // saves the llmservice for generating quizzes
        this.taskManager = taskManager; // saves task manager for background processing
        this.quizDAO = quizDAO; // saves object access in database
        this.userID = userID; // saves current user id
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
            throw new IllegalStateException("Taskmanager is currently not available."); // checks if taskmanager is available
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
            throw new IllegalStateException("Uploaded material is unavailable");
    }
        return llmService.generateStudyMaterial(uploadedMaterial, questionType); // asks the llm to create the quiz from the uploaded pdf
    }

    private boolean isSupportedQuestionType(String questionType) {
        return TRUE_FALSE_TYPE.equals(questionType) || MULTIPLE_CHOICE_TYPE.equals(questionType); // this is checking whether the quiz type the user asked for is supported
        
    }

    
    public void saveQuiz(String title, List<Question> questions) { // saves a quiz and its questions through QuizDAO

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("The title of the quiz is required."); // this makes sure the quiz has a title

        }

        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("This quiz has to contain a question."); // this makes sure the quiz has at least 1 question
        }
        
        try {

            quizDAO.saveQuiz(userID, title, questions); // saves quiz by user's id, QuizDAO also saves questions and answer options

        } catch (SQLException error) {

            throw new IllegalStateException("The quiz couldn't be saved.", error); // throws error if quiz couldnt be saved
        }

    }
    
    public void saveGeneratedQuiz(String title, String quizJson, String questionType) { // convers the llm created JSON into objects in our database and saves them
        if (quizJson == null || quizJson.trim().isEmpty()) {
            throw new IllegalArgumentException("Quiz JSON cannot be empty."); // makes sure llm returned the JSON
        }

        if (!isSupportedQuestionType(questionType)) {
            throw new IllegalArgumentException("Unsupported quiz type."); // makes sure the quiz type is supported
        }

        JSONObject quizObject = new JSONObject(quizJson); // converts text into JSON object

        JSONArray jsonQuestions = quizObject.getJSONArray("questions"); // gets array of generated questions

        List<Question> questions = new ArrayList<>(); // makes list of database question objects

        for (int i = 0; i < jsonQuestions.length(); i++) { // loops through every generated question
            JSONObject jsonQuestion = jsonQuestions.getJSONObject(i); // gets one question form the JSON array

            String questionText = jsonQuestion.getString("question"); // gets the question text

            int questionOrder = i + 1; // question number starts at 1

            List<AnswerOption> options = new ArrayList<>(); // creates answer-optipn list

            if (TRUE_FALSE_TYPE.equals(questionType)) { // handles true/false questions
                boolean correctAnswer = jsonQuestion.getBoolean("answer"); // gets correct answer as a boolean

                options.add(new AnswerOption(0L, 0L, "True", 1, correctAnswer)); // adds the true option

                options.add(new AnswerOption(0L, 0L, "False", 2, !correctAnswer)); // adds the false option

                questions.add(new Question(0L, 0L, questionText, "TRUE_FALSE", questionOrder, options)); // makes the database question

            }

            else { // now we handle the multiple-choice questions
                
                JSONArray jsonChoices = jsonQuestion.getJSONArray("choices"); // gets choices array

                String correctAnswer = jsonQuestion.getString("answer"); // gets the correct answer test

                for (int j = 0; j < jsonChoices.length(); j++) { // loops through every choice
                    String choiceText = jsonChoices.getString(j); // gets one choice

                    boolean isCorrect = choiceText.equals(correctAnswer); // checks whether that choice was correct

                    options.add(new AnswerOption(0L, 0L, choiceText, j + 1, isCorrect)); // adds the choice to the database list


                }

                questions.add(new Question(0L, 0L, questionText, "MULTIPLE_CHOICE", questionOrder, options)); // creates the database question

            }

        }
        
        saveQuiz(title, questions); // we call the our existing database saving method

    }
    public Quiz getSavedQuiz(long quizID) { // Returns saved quiz questions frm database
        
        try {

            return quizDAO.getSavedQuiz(quizID, userID); // only get quiz if it belongs to user
        } catch (SQLException error) {
            throw new IllegalStateException("The quiz couldn't be retrieved.", error); // throws error if quiz couldn't be accessed from database 
        }
    }

    
    public void deleteQuiz(long quizID) { // Deletes one saved quiz question form database

        try { 
            boolean deleted = quizDAO.deleteQuiz(quizID, userID);  // deletes quiz only if it belongs to the user

            if (!deleted) {
                throw new IllegalArgumentException("Quiz not found."); // sends message to user that the quiz no longer exists
            }
        } catch (SQLException error) {
            throw new IllegalStateException("Quiz couldn't be deleted.", error); // sends message that the quiz couldnt be deleted from the database
        }
    }
}
