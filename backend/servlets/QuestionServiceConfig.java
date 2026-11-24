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
            TaskManager taskManager = new TaskManager();
