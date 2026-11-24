package servlets;

import services.QuestionService;
import services.MaterialService;
import services.TaskManager;
import ollama.LlmService;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

// Initialize the question and answering part.
@WebListener
public class QuestionServiceConfig implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent event) {

            // Manages background PDF-processing.
            TaskManager taskManager = new TaskManager();

            // Uploads PDF are processed and stored
            MaterialService materialService = new MaterialService(taskManager);
            event.getServletContext().setAttribute("materialService", materialService);
              event.getServletContext().setAttribute("taskManager", taskManager);

    // Retrives only material
    QuestionService.MaterialLoader materialLoader = (userId, materialId) ->
                    materialService.getMaterialText(userId, materialId);

    // Connects to Ollama server
    LlmService llmService = new LlmService();
    QuestionService.AnswerGenerator answerGenerator = (materialText, question) ->
                    llmService.answerQuestion(materialText, question);
     QuestionService questionService = new QuestionService(materialLoader, answerGenerator);
    event.getServletContext().setAttribute("questionService", questionService);
    }
  
  @Override
    public void contextDestroyed(ServletContextEvent event) {
      // Stops question-answering threads.
      Object questionService = event.getServletContext().getAttribute("questionService");
      if (questionService instanceof QuestionService service) {
            service.close();
        }
              // Stop background PDF-processing threads.
              Object taskManager = event.getServletContext().getAttribute("taskManager");
      if (taskManager instanceof TaskManager manager) {
            manager.shutdown();
        }
    }
}
