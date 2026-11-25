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

}