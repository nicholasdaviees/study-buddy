// Tests quiz and true/false generation, validation, persistence, and deletion.
package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;

import database.Question;
import database.Quiz;
import database.QuizDAO;
import ollama.LlmService;
import services.MaterialService;
import services.QuizService;
import services.TaskManager;

public class QuizServiceTest {

    @Test // tests starting multiple choice quizzes
    public void startsMultipleChoiceGeneration() {
        TaskManager fakeTaskManager = new TaskManager() {
            @Override
            public String submitTask(String taskType, Callable<String> task) {

                assertEquals("QUIZ_GENERATION", taskType);
                return "multiple-choice-task";
            }
        };

        QuizService service = new QuizService(null, null, fakeTaskManager, null);

        String taskId = service.startQuizGeneration(1L, "material-1", "multiple-choice");

        assertEquals("multiple-choice-task", taskId);
    }

    @Test // test for true false choices
    public void startsTrueFalseGeneration() {
        TaskManager fakeTaskManager = new TaskManager() {

            @Override
            public String submitTask(String taskType, Callable<String> task) {

                return "true-false-task";
            }
        };

        QuizService service = new QuizService(null, null, fakeTaskManager, null);

        String taskId = service.startQuizGeneration(1L, "material-1", "true-false");

        assertEquals("true-false-task", taskId);
    }

    @Test // tests if material has been uploaded and processed
    public void preventsGenerationBeforeMaterialIsReady() {
        MaterialService materialService = new MaterialService(new TaskManager()) {
        
            @Override
            public String getMaterialText(long userId, String materialId) {
                    
                return "";
            }
        };

        QuizService service = new QuizService(materialService, null, null, null);

        assertThrows(IllegalStateException.class, () -> service.generateQuiz(1L, "material-1", "multiple-choice"));
    }

    @Test // tests to make sure quiz is using the chosen uploaded material
    public void verifiesQuizUsesUploadedMaterial() {
        MaterialService materialService = new MaterialService(new TaskManager()) {
            
            @Override
            public String getMaterialText(long userId, String materialId) {
                    
                return "Sample class notes";
            }
        };

        LlmService llmService = new LlmService() {
            @Override
            public String generateStudyMaterial(String materialText, String questionType) {

                assertEquals("Sample class notes", materialText);

                assertEquals("multiple-choice", questionType);

                return "{\"questions\":[{\"question\":\"What is Java?\",\"choices\":[\"Language\",\"Animal\"],\"answer\":\"Language\"}]}";
            }
        };

        QuizService service = new QuizService(materialService, llmService, null, null);

        String result = service.generateQuiz(1L, "material-1", "multiple-choice");

        assertEquals(true, result.contains("What is Java?"));
    }

    @Test // tests if quiz was saved and verified
    public void validatesAndSavesValidQuiz() {
        RecordingQuizDAO fakeDAO = new RecordingQuizDAO();

        QuizService service = new QuizService(null, null, null, fakeDAO);

        String quizJson = "{\"questions\":[{\"question\":\"What is Java?\",\"choices\":[\"Language\",\"Animal\"],\"answer\":\"Language\"}]}";

        service.saveGeneratedQuiz(25L, 1L, "Java Quiz", quizJson, "multiple-choice");

        assertEquals(25L, fakeDAO.savedUserID);
        assertEquals("Java Quiz", fakeDAO.savedTitle);
        assertEquals(1, fakeDAO.savedQuestions.size());

        Question savedQuestion = fakeDAO.savedQuestions.get(0);

        assertEquals("What is Java?", savedQuestion.questionText);

        assertEquals("MULTIPLE_CHOICE", savedQuestion.questionType);

        assertEquals(2, savedQuestion.options.size());
    }

    @Test // tests if quiz has valid JSON
    public void rejectsInvalidQuizJson() {
    
        QuizService service = new QuizService(null, null, null, new RecordingQuizDAO());

        assertThrows(RuntimeException.class, () -> service.saveGeneratedQuiz(1L, 1L, "Broken Quiz", "not valid JSON", "multiple-choice"));
    }

    @Test // tests if saved quiz could be retrieved
    public void retrievesSavedQuiz() throws SQLException {
        RecordingQuizDAO fakeDAO = new RecordingQuizDAO();

        Quiz savedQuiz = new Quiz(10L, 25L, 1L, "Saved Quiz", new ArrayList<>());

        fakeDAO.quizToReturn = savedQuiz;

        QuizService service = new QuizService(null, null, null, fakeDAO);

        Quiz result = service.getSavedQuiz(25L, 10L);

        assertSame(savedQuiz, result);
        assertEquals(10L, fakeDAO.requestedQuizID);
        assertEquals(25L, fakeDAO.requestedUserID);
    }

    @Test // tests if quiz was deleted
    public void deletesRequestedQuiz() throws SQLException {
        RecordingQuizDAO fakeDAO = new RecordingQuizDAO();
        fakeDAO.deleteResult = true;

        QuizService service = new QuizService(null, null, null, fakeDAO);

        service.deleteQuiz(25L, 10L);

        assertEquals(10L, fakeDAO.deletedQuizID);
        assertEquals(25L, fakeDAO.deletedUserID);
    }

    @Test // tests if quiz isnt missing
    public void rejectsMissingQuiz() throws SQLException {
        RecordingQuizDAO fakeDAO = new RecordingQuizDAO();
        fakeDAO.deleteResult = false;

        QuizService service = new QuizService(null, null, null, fakeDAO);

        assertThrows(IllegalArgumentException.class, () -> service.deleteQuiz(25L, 99L));
    }

    private static class RecordingQuizDAO extends QuizDAO { // fake DAO that stores values in memory instead of using MySQL, like in flashcardservice test

        private long savedUserID;
        private String savedTitle;
        private List<Question> savedQuestions;

        private long requestedQuizID;
        private long requestedUserID;
        private Quiz quizToReturn;

        private long deletedQuizID;
        private long deletedUserID;
        private boolean deleteResult;

        @Override
        public void saveQuiz(
                long userID,
                long materialID,
                String title,
                List<Question> questions)
                throws SQLException {
            
                    savedUserID = userID;
                    savedTitle = title;
                    savedQuestions = questions;
        }

        @Override
        public Quiz getSavedQuiz(long quizID, long userID) throws SQLException {

            requestedQuizID = quizID;
            requestedUserID = userID;
            return quizToReturn;
        }

        @Override
        public boolean deleteQuiz(long quizID, long userID) throws SQLException {

            deletedQuizID = quizID;
            deletedUserID = userID;
            return deleteResult;
        }
    }
}