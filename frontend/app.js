// Sends a selected PDF to the upload servlet
function uploadPdf(file) {
}

// Calls the quiz servlet for multiple-choice or true/false questions.
function generateQuiz(materialId, questionType) {
}

// Calls the flashcard servlet.
function generateFlashcards(materialId) {
}

// Calls the question servlet.
function askQuestion(materialId, question) {
}

// Polls the task-status servlet until work completes or fails.
function pollTaskStatus(taskId, intervalMilliseconds) {
}

// Calls the quiz servlet for saved quiz questions.
function loadQuizzes(materialId) {
}

// Calls the flashcard servlet for saved flashcards.
function loadFlashcards(materialId) {
}

// Displays saved or newly generated quiz questions.
function displayQuiz(quizJson) {
}

// Displays saved or newly generated flashcards.
function displayFlashcards(flashcardJson) {
}

// Displays the answer returned for a student question.
function displayAnswer(answerJson) {
}

// Calls the quiz servlet to delete one quiz question.
function deleteQuiz(quizId) {
}

// Calls the flashcard servlet to delete one flashcard.
function deleteFlashcard(flashcardId) {
}

// Displays a user-friendly error message.
function showError(errorMessage) {
}

// Updates the interface for queued, processing, complete, or failed work.
function displayTaskStatus(taskStatus) {
}
