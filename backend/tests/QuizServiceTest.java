// Tests quiz and true/false generation, validation, persistence, and deletion.
package tests;

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

        QuizService service = new QuizService(null,null,fakeTaskManager,null,1L);

        String taskId = service.startQuizGeneration("material-1", "multiple-choice");

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

        QuizService service = new QuizService(null, null, fakeTaskManager, null, 1L);

        String taskId = service.startQuizGeneration("material-1", "true-false");

        assertEquals("true-false-task", taskId);
    }

    @Test // tests if material has been uploaded and processed
    public void preventsGenerationBeforeMaterialIsReady() {
        MaterialService materialService = new MaterialService(new TaskManager()) {
        
            @Override
            public String getMaterialText(String materialId) {
                    
                return "";
            }
        };

        QuizService service = new QuizService(materialService, null, null, null, 1L);

        assertThrows(IllegalStateException.class, () -> service.generateQuiz("material-1", "multiple-choice"));
    }

    @Test // tests to make sure quiz is using the chosen uploaded material
    public void verifiesQuizUsesUploadedMaterial() {
        MaterialService materialService = new MaterialService(new TaskManager()) {
            
            @Override
            public String getMaterialText(String materialId) {
                    
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

        QuizService service = new QuizService(materialService, llmService, null, null, 1L);

        String result = service.generateQuiz("material-1", "multiple-choice");

        assertEquals(true, result.contains("What is Java?"));
    }

    @Test // tests if quiz was saved and verified
    public void validatesAndSavesValidQuiz() {
        RecordingQuizDAO fakeDAO = new RecordingQuizDAO();

        QuizService service = new QuizService(null, null, null, fakeDAO, 25L);

        String quizJson = "{\"questions\":[{\"question\":\"What is Java?\",\"choices\":[\"Language\",\"Animal\"],\"answer\":\"Language\"}]}";

        service.saveGeneratedQuiz("Java Quiz", quizJson, "multiple-choice");

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
    
        QuizService service = new QuizService(null, null, null, new RecordingQuizDAO(), 1L);

        assertThrows(RuntimeException.class, () -> service.saveGeneratedQuiz("Broken Quiz", "not valid JSON", "multiple-choice"));
    }

    @Test // tests if saved quiz could be retrieved
    public void retrievesSavedQuiz() throws SQLException {
        RecordingQuizDAO fakeDAO = new RecordingQuizDAO();

        Quiz savedQuiz = new Quiz(10L, 25L, "Saved Quiz", new ArrayList<>());

        fakeDAO.quizToReturn = savedQuiz;

        QuizService service = new QuizService(null, null, null, fakeDAO, 25L);

        Quiz result = service.getSavedQuiz(10L);

        assertSame(savedQuiz, result);
        assertEquals(10L, fakeDAO.requestedQuizID);
        assertEquals(25L, fakeDAO.requestedUserID);
    }

    @Test // tests if quiz was deleted
    public void deletesRequestedQuiz() throws SQLException {
        RecordingQuizDAO fakeDAO = new RecordingQuizDAO();
        fakeDAO.deleteResult = true;

        QuizService service = new QuizService(null, null, null, fakeDAO, 25L);

        service.deleteQuiz(10L);

        assertEquals(10L, fakeDAO.deletedQuizID);
        assertEquals(25L, fakeDAO.deletedUserID);
    }

    @Test // tests if quiz isnt missing
    public void rejectsMissingQuiz() throws SQLException {
        RecordingQuizDAO fakeDAO = new RecordingQuizDAO();
        fakeDAO.deleteResult = false;

        QuizService service = new QuizService(null, null, null, fakeDAO, 25L);

        assertThrows(IllegalArgumentException.class, () -> service.deleteQuiz(99L));
    }












}
