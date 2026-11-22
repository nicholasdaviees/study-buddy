package services;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Suppose to handle students questions.
public class QuestionService implements AutoCloseable {

    // The max of question accepted
    private static final int MAX_QUESTION_LENGTH = 2000;

    // The max amount of work that can be executed.
    private static final int MAX_WORK = 3;

    // Loads the material
    private final MaterialLoader materialLoader;

    // For generating the answer
    private final AnswerGenerator answerGenerator;

    // This is suppose to run the tasks in the background.
    private final ExecutorService taskManager = Executors.newFixedThreadPool(MAX_WORK);

    // This is were the task is going to be store.
    private final Map<String, CompletableFuture<String>> tasks = new ConcurrentHashMap<>();

    // Helps to load the material and part of generating the answer.
    public QuestionService( MaterialLoader materialLoader, AnswerGenerator answerGenerator) {

    this.materialLoader = java.util.Objects.requireNonNull(materialLoader);

    this.answerGenerator = java.util.Objects.requireNonNull(answerGenerator);
    }
    
    // Only collects the material.
    @FunctionalInterface
    public interface MaterialLoader{
        String loadMaterial(String materialId) throws Exception;
        }

    // How Ollama will receive the material and question. 
    @FunctionalInterface
    public interface AnswerGenerator {
        String generateAnswer( String materialText, String question) throws Exception;
        }

    // Places a question-answering task in the background task manager.
    public String startQuestionAnswering(long userId, String materialId, String question) {

    // Reject invalid question or material.
    validateQuestion(question);
    validateMaterialId(materialId);

    // Generates a unique ID for the task.
    String taskId = UUID.randomUUID().toString();

    // Runs startQuestionAnswering.
    CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {

        try {
            return answerQuestion( materialId, question);

            } catch (Exception exception) {
            
            throw new CompletionException(exception);
            }
    }, taskManager);

    tasks.put(taskId, future);

    return taskId;
}

// Answers a question using only the selected material.
public String answerQuestion(long userId, String materialId, String question) {
    validateQuestion(question);
    validateMaterialId(materialId);

    try {

        // Retrieves the content of the material.
        String materialText = materialLoader.loadMaterial(materialId);
        
        if(materialText == null || materialText.isBlank()) {
        
        return createUnsupportedMaterialResponse();
        }

        String answer = answerGenerator.generateAnswer( materialText, question.trim());

        if (answer == null || answer.isBlank()) {
            return createUnsupportedMaterialResponse();
            }

        return answer.trim();

    } catch (Exception exception) {

    throw new IllegalStateException("Question answering failed.", exception);
    }
}

// Rejects blank or otherwise invalid user questions.
public void validateQuestion(String question) {

    if (question == null || question.isBlank()) {
        throw new IllegalArgumentException("Please enter a question.");
        }
    
    if(question.length() > MAX_QUESTION_LENGTH) {
        throw new IllegalArgumentException("Question must be 2000 characters or fewer.");
        }
    }

    // Checks if the students didn't select the material.
    private void validateMaterialId(String materialId) {

    if(materialId == null || materialId.isBlank()) {
        throw new IllegalArgumentException("Please select study material.");
        }
    }

    // Creates the response used when the answer is absent from the material.
    public String createUnsupportedMaterialResponse() {
        return "I couldn't find an answer to that question " + "in the selected study material.";
        }

        // Checks backgound task status.
        public TaskResult getTaskResult(long userId, String taskId) {

        CompletableFuture<String> future = tasks.get(taskId);

        if(future == null) {
            return new TaskResult("NOT_FOUND", null);
            }
        
        try {
            return new TaskResult("COMPLETED", future.join());
            } catch (CompletionException exception) {
            return new TaskResult("FAILED", null);
            }
        }
        
        public record TaskResult(String status, String answer){
        }
        
        // Stops the background task manager.
        @Override
        public void close() {
            taskManager.shutdown();
        }
    }
