import java.net.http.HttpClient;
import java.time.Duration;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

public class LlmService {

    private final String ollamaBaseUrl;
    private final String modelName;
    private final HttpClient httpClient;

    public LlmService() {
        this.ollamaBaseUrl = "http://localhost:11434";
        this.modelName = "qwen3:4b";

        // Starts/builds HTTP client builder with connection timout at 5 secs
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    private String sendGenerateRequest(String requestBody){
        URI endpoint = URI.create(ollamaBaseUrl + "/api/generate"); 
        // Build POST request
        HttpRequest request = HttpRequest.newBuilder()
                                .uri(endpoint).header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                                .build();
        try {
            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } 
        catch (IOException e) {
            throw new RuntimeException("Could not connect to Ollama", e);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ollama request was interrupted", e);
        }
    }

    // Sends a request to generate a quiz, true/false questions, or flashcards
    public String generateStudyMaterial(String materialText, String studyMaterialType) {
        return null;
    }

    // Answers a user's question using the uploaded material
    public String answerQuestion(String materialText, String userQuestion) {
        return null;
    }

    // Checks whether the local LLM is reachable
    public boolean isAvailable() {
        return false;
    }
}
