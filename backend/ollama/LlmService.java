import java.net.http.HttpClient;
import java.time.Duration;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.function.Consumer;

public class LlmService {

    private final String ollamaBaseUrl;
    private final String modelName;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper; // for dealing with JSON text in a safe manner
    private final PromptBuilder promptBuilder;

    public LlmService() {
        this.ollamaBaseUrl = "http://localhost:11434";
        this.modelName = "qwen3:4b";

        // Starts/builds HTTP client builder with connection timout at 5 secs
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        this.objectMapper = new ObjectMapper();
        this.promptBuilder = new PromptBuilder();
    }

    // Sends request to LLM
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
            int rc = response.statusCode();
            if (rc == 200){
                return response.body();
            }
            else {
                throw new RuntimeException("Ollama returned HTTP status " + rc);
            }
            
        } 
        catch (IOException e) {
            throw new RuntimeException("Could not connect to Ollama", e);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ollama request was interrupted", e);
        }
    }

    // Builds the JSON request body, so the prompt can be appropriately received by the LLM
    private String buildGenerateRequestBody(String prompt) {
        try {
            ObjectNode request = objectMapper.createObjectNode(); // create JSON object

            // Populate object:
            request.put("model", modelName);
            request.put("prompt", prompt);
            request.put("stream", false);
            request.put("think", false);
            request.put("format", "json");

            return objectMapper.writeValueAsString(request); // convert Java JSON object to JSON text
        }
        catch (IOException e) {
            throw new RuntimeException("Could not serialize Ollama request body", e);
        }
    }

    // Makes request to LLM to generate study material or answer a user's question
    // Also validates outputted JSON from LLM
    private String requestAndValidate(String prompt, Consumer<String> responseValidator) {
        String jsonRequestBody = buildGenerateRequestBody(prompt); // Build request body
        final int maxAttempts = 2;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                String ollamaResponseJson = sendGenerateRequest(jsonRequestBody);
                /*
                Ollama responds with JSON structured like:
                    {
                        "model": "qwen3:4b",
                        "response": "{\"flashcards\":[]}",
                        "done": true
                    }
                We want to grab the generated response field
                */
                JsonNode ollamaResponseNode = objectMapper.readTree(ollamaResponseJson); // parse Ollama's JSON envelope
                JsonNode generatedResponseNode = ollamaResponseNode.get("response"); // extract the model-generated response

                if (generatedResponseNode == null || !generatedResponseNode.isTextual()) {
                    throw new RuntimeException("Ollama response did not contain generated text");
                }

                String generatedJson = generatedResponseNode.asText(); // extract generated JSON text

                // Validate returned json from LLM
                // Retry generation one more time if validation fails, otherwise return llmResponse or error if applicable
                responseValidator.accept(generatedJson);
                return generatedJson;
            } catch (IOException e) {
                if (attempt == maxAttempts) {
                    throw new RuntimeException("Ollama returned invalid JSON after two attempts", e);
                }
            } catch (RuntimeException e) {
                if (attempt == maxAttempts) {
                    throw new RuntimeException("Ollama response failed validation after two attempts", e);
                }
            }
        }

        throw new RuntimeException("Ollama request failed");
    }

    // Sends a request to generate a quiz, true/false questions, or flashcards
    public String generateStudyMaterial(String materialText, String studyMaterialType) {
        // Validate material text
        if (materialText == null || materialText.isBlank()) {
            throw new IllegalArgumentException("Study material cannot be blank");
        }

        if (studyMaterialType == null || studyMaterialType.isBlank()) {
            throw new IllegalArgumentException("Study material type is required");
        }

        // Pull prompt based on study material type
        String prompt;
        if ("flashcards".equals(studyMaterialType)) {
            prompt = promptBuilder.buildFlashcardPrompt(materialText);
        } else if ("multiple-choice".equals(studyMaterialType)) {
            prompt = promptBuilder.buildMultipleChoicePrompt(materialText);
        } else if ("true-false".equals(studyMaterialType)) {
            prompt = promptBuilder.buildTrueFalsePrompt(materialText);
        } else {
            throw new IllegalArgumentException("Unsupported study material type: " + studyMaterialType);
        }

        // Pass a Consumer lambda to requestAndValidate, which executes it AFTER Ollama returns
        // The lambda calls the appropriate validator function based on the material type
        JsonResponseValidator validator = new JsonResponseValidator();
        return requestAndValidate(prompt, response -> {
            if ("flashcards".equals(studyMaterialType)) {
                validator.validateFlashcardJson(response);
            } else {
                validator.validateQuizJson(response, studyMaterialType);
            }
        });
    }

    // Answers a user's question using the uploaded material
    public String answerQuestion(String materialText, String userQuestion) {
        // Validate material text/user question
        if (materialText == null || materialText.isBlank()) {
            throw new IllegalArgumentException("Study material cannot be blank");
        }

        if (userQuestion == null || userQuestion.isBlank()) {
            throw new IllegalArgumentException("A user question is required");
        }

        // Pull prompt based on study material type
        String prompt = promptBuilder.buildAnswerPrompt(materialText, userQuestion);

        // Pass a Consumer lambda to requestAndValidate, which executes it AFTER Ollama returns
        // The lambda calls the appropriate validator function based on the material type
        JsonResponseValidator validator = new JsonResponseValidator();
        return requestAndValidate(prompt, response -> validator.validateAnswerJson(response));
    }

    // Checks whether the local LLM is reachable
    public boolean isAvailable() {
        return false;
    }
}
