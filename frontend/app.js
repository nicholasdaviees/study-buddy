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
async function askQuestion(materialId, question) {

    if (!materialId || !String(materialId).trim()) {
        throw new Error("Select study material.");
    }

    if (!question || !question.trim()) {
        throw new Error("Enter a question.");
    }

    if (question.length > 2000) {
        throw new Error("Question must be 2000 characters or fewer.");
    }

    displayTaskStatus("Starting your question...");

    // Communicates with QuestionServlet to read.
    const response = await fetch("api/questions/start", {
        method: "POST",
        headers: { "Content-Type": "application/json"},
        credentials: "same-origin",
        body: JSON.stringify({
            materialId: String(materialId).trim(),
            question: question.trim()
        })
    });

    const result = await response.json().catch(() => null);

    if (response.status === 401) {
        throw new Error("Session expired, Please log in again.");
    }

    if(!response.ok) {
        throw new Error(result?.error || "Unable to start question answering.");
    }

    if (typeof result?.taskId !== "string" || !result.taskId.trim()) {
        throw new Error("Did not return task ID.");
    }

    // Wait for QuestionService to finish.
    return await pollTaskStatus(result.taskId, 1000);
}


// Polls the task-status servlet until work completes or fails.
async function pollTaskStatus(taskId, intervalMilliseconds = 1000) {
    
    // Checks up to 60 times.
    const maxAttempts = 60;

    for (let attempt = 0; attempt < maxAttempts; attempt++) {
        
        const response = await fetch("api/questions/status?taskId=" + encodeURIComponent(taskId),
    {
        method: "GET",
        credentials: "same-origin",
        cache: "no-store"
    });
    
    const result = await response.json().catch(() => null);

    if (response.status === 401) {
        throw new Error("Your session expired. Please log in again.");
    }
    
    if (!response.ok) {
        throw new Error(result?.error || "Unable to check question status.");
    }

    // When finished, it will display and return the answer.
    if (result?.status === "COMPLETED") {
            displayAnswer(result);
            return result.answer;
    }

    if (result?.status === "FAILED") {
        throw new Error("Study Buddy could not answer this question.");
    }

    if(result?.status === "PROCESSING") {
        throw new Error("Unexpected question task status.");
    }

    showError("Study Buddy is answering your question...");

    // Pause.
    await new Promise(resolve => setTimeout(resolve, intervalMilliseconds));
    }

    throw new Error("The answer is taking longer than expected. Please try again later.");

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

 // Finds the PDF file.
    const pdfUploadInput = document.getElementById("pdf-upload");

    if (pdfUploadInput) {
        pdfUploadInput.addEventListener("change", async () => {
            const file = pdfUploadInput.files[0];
            const fileStatus = document.getElementById("file-status");

            if (!file) {
                return;
            }
        try {
            if (fileStatus) {
                fileStatus.textContent = "Uploading" + file.name + "...";
            }
            const taskId = await uploadPdf(file);

            if (fileStatus) {
                fileStatus.textContent = "PDF upload accepted. Processing task: " + taskId;
            }
        } catch (error) {
            if (fileStatus) {
                fileStatus.textContent = error.message;
            }
        }
        });
    }
