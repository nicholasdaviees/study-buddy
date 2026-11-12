// Sends a selected PDF to the upload servlet
// Sends a selected PDF to the upload servlet and returns its task ID.
async function uploadPdf(file) {
    const maxFileSizeBytes = 10 * 1024 * 1024;

    if (!file || file.size === 0) {
        throw new Error("Please select a nonempty PDF file.");
    }

    if (!file.name.toLowerCase().endsWith(".pdf")) {
        throw new Error("Only PDF files are allowed.");
    }

    if (file.size > maxFileSizeBytes) {
        throw new Error("The PDF must be 10 MB or smaller.");
    }

    const formData = new FormData();
    formData.append("file", file);

    let response;
    try {
        response = await fetch("/studybuddy/api/material", {
            method: "POST",
            body: formData
        });
    } catch {
        throw new Error("Could not reach the server. Please try again.");
    }

    const result = await response.json().catch(() => null);

    if (!response.ok) {
        throw new Error(result?.error || `Upload failed (HTTP ${response.status}).`);
    }

    if (response.status !== 202
            || typeof result?.taskId !== "string"
            || !result.taskId.trim()) {
        throw new Error("The server did not return a task ID.");
    }

    return result.taskId;
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
