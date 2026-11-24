package servlets;

import database.FlashcardsDAO;
import database.QuizDAO;
import ollama.LlmService;
import services.FlashcardService;
import services.MaterialService;
import services.QuestionService;
import services.QuizService;
import services.TaskManager;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

// Initializes shared application services.
@WebListener
public class ApplicationConfig implements ServletContextListener {
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
                    materialService.getMaterialText(materialId);

     LlmService llmService = new LlmService();
     event.getServletContext().setAttribute("flashcardService",
             new FlashcardService(materialService, llmService, taskManager,
                     new FlashcardsDAO()));
     event.getServletContext().setAttribute("quizService",
             new QuizService(materialService, llmService, taskManager,
                     new QuizDAO()));

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
