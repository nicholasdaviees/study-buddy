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

// Wait for uploaded file to finish processing and returns material ID.
async function waitForUploadedMaterial(taskId) {
    const maxAttempts = 60;
    for (let attempt = 0; attempt < maxAttempts; attempt++) {
        const response = await fetch("/studybuddy/api/tasks/" + encodeURIComponent(taskId),
    {
        method: "GET",
        credentials: "same-origin",
        cache: "no-store"
    });
    
    const result = await response.json().catch(() => null);

    if (response.status === 401) {
        throw new Error("Please log in again.");
    }

    if(!response.ok) {
        throw new Error(result?.error || "Unable to check PDF processing.");
    }

    if (result?.status === "COMPLETE") {
        if (!result.resultId) {
            throw new Error("No material ID was returned.");
        }

        return result.resultId;
    }

    if ( result?.status === "FAILED") {
        throw new Error("Study buddy could not process this PDF.");
    }

    if(result?.status !== "QUEUED" && result?.status !== "PROCESSING") {
        throw new Error("Unexpected PDF processing status.");
    }
    
    await new Promise(resolve => setTimeout(resolve, 1000));
    }

    throw new Error("PDF processing is taking longer than expected.");
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
    const statusElement = document.getElementById("question-status");
    if (statusElement) {
        statusElement.textContent = errorMessage;
 }
}

// Updates the interface for queued, processing, complete, or failed work.
function displayTaskStatus(taskStatus) {
     const statusElement = document.getElementById("question-status");
     if (statusElement) {
        statusElement.textContent = errorMessage;
 }
}

     // Finds the pdf file.
    const pdfUploadInput = document.getElementById("pdf-upload");

    if (pdfUploadInput) {
        pdfUploadInput.addEventListener("change", async () => {
            const file = pdfUploadInput.files[0];
            const fileStatus = document.getElementById("file-status");
            const materialInput = document.getElementById("question-material-id");
            const selectedMaterialName = document.getElementById("selected-material-name");

            if (!file) {
                return;
            }
            if (materialInput) {
            materialInput.value = "";
        }
             if (selectedMaterialName) {
            selectedMaterialName.textContent = "No study material selected.";
        }

        try {
            if (fileStatus) {
                fileStatus.textContent = "Uploading " + file.name + "...";
            }
            const taskId = await uploadPdf(file);

            const materialId = await waitForUploadedMaterial(taskId);

            if (materialInput) {
                materialInput.value = materialId;
            }

            if (selectedMaterialName) {
                selectedMaterialName.textContent = "Selected study material: " + file.name;
            }

            if (fileStatus) {
                fileStatus.textContent = "PDF ready! You can now ask question about " + file.name + ".";
            }
        } catch (error) {
            if (fileStatus) {
                fileStatus.textContent = error.message;
            }
        }
        });
    }
   // Flashcard Display
    let flashcards = [];

    let currentFlashcardIndex = 0;

    const revealAnswerButton = document.getElementById("reveal-answer-btn");
    const previousCardButton = document.getElementById("previous-card-btn");
    const nextCardButton = document.getElementById("next-card-btn");

    function displayFlashcard() {
        if (flashcards.length === 0) {
            return;
        }
        const card = flashcards[currentFlashcardIndex];

        document.getElementById("flashcard-count").textContent = "Card " + (currentFlashcardIndex + 1) + " of " + flashcards.length;

        document.getElementById("flashcard-question").textContent = card.question;
        
        const answerElement = document.getElementById("flashcard-answer");

        answerElement.textContent = card.answer;
        answerElement.hidden = true;

    revealAnswerButton.textContent = "Reveal Answer";
}

if (revealAnswerButton) {
    revealAnswerButton.addEventListener("click", () => {

        if (flashcards.length === 0) {
            return;
        }

        const answerElement = document.getElementById("flashcard-answer");

        answerElement.hidden = !answerElement.hidden;

        revealAnswerButton.textContent = answerElement.hidden ? "Reveal Answer" : "Hide Answer";
    });
}

if (previousCardButton) {
    previousCardButton.addEventListener("click", () => {

        if (currentFlashcardIndex > 0) {
            currentFlashcardIndex--;
            displayFlashcard();
        }
    });
}

if (nextCardButton) {
    nextCardButton.addEventListener("click", () => {

        if (currentFlashcardIndex < flashcards.length - 1) {
            currentFlashcardIndex++;
            displayFlashcard();
        }
    });
}
// Generate flashcards.
const flashcardButton = document.getElementById("flashcard-btn");

if (flashcardButton) {
    flashcardButton.addEventListener("click", async () => {
        const materialInput = document.getElementById("question-material-id");
        const materialId = materialInput ? materialInput.value : "";

        if (!materialId) {
            alert("Please upload a PDF first.");
            return;
        }

        flashcardButton.disabled = true;
        flashcardButton.textContent = "Generating...";

        try {
            const response = await fetch("/studybuddy/api/flashcards", {
                method: "POST",
                credential: "same-origin",
                body: new URLSearchParams({
                    materialId: materialId
                })
            });
        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || "Could not generate flashcards.");
        }
        if (!result.taskId) {
            throw new Error("No flashcard task ID was returned.");
        }

        flashcards = await waitForGeneratedFlashcards(result.taskId);
currentFlashcardIndex = 0;
displayFlashcard();
document.getElementById("flashcard-section").scrollIntoView({
    behavior: "smooth"
});
        } catch (error) {
            alert(error.message);
        } finally {
            flashcardButton.disabled = false;
            flashcardButton.textContent = "Generate Flashcards";
        }
    });
}

// Waits for the generated flashcards.
async function waitForGeneratedFlashcards(taskId) {
    const maxAttempts = 60;

     for (let attempt = 0; attempt < maxAttempts; attempt++) {
        const response = await fetch("/studybuddy/api/tasks/" + encodeURIComponent(taskId), {
                method: "GET",
                credentials: "same-origin",
                cache: "no-store"
            }
        );
        const result = await response.json();

         if (!response.ok) {
            throw new Error(result.error || "Could not check flashcard generation.");
        }

        if (result.status === "COMPLETE") {
            if (!result.resultId) {
                throw new Error("No generated flashcards were returned.");
            }
            const generated = JSON.parse(result.resultId);

            if (!Array.isArray(generated.flashcards)) {
                throw new Error("Invalid flashcard response.");
            }
            return generated.flashcards;
        }

     if (result.status === "FAILED") {
            throw new Error("Flashcard generation failed.");
        }
        await new Promise(resolve => setTimeout(resolve, 1000));
     }
}   
// Save flashcard button.
const saveFlashcardButton = document.getElementById("save-flashcard-btn");
if (saveFlashcardButton) {
    saveFlashcardButton.addEventListener("click", () => {
        if (flashcards.length === 0) {
            alert("Please generate flashcards first.");
            return;
        }

         const currentCard = flashcards[currentFlashcardIndex];

         alert("Save requested for: " + currentCard.question);
    });
}
// Delete Flashcard button.
const deleteFlashcardButton = document.getElementById("delete-flashcard-btn");

if (deleteFlashcardButton) {
    deleteFlashcardButton.addEventListener("click", () => {
        if (flashcards.length === 0) {
            alert("There are no flashcards to delete.");
            return;
        }
        const confirmed = confirm("Are you sure you want to delete this flashcard?");

          if (!confirmed) {
            return;
        }

        flashcards.splice(currentFlashcardIndex, 1);

        if (flashcards.length === 0) {
            document.getElementById("flashcard-count").textContent = "No flashcards remaining.";
            document.getElementById("flashcard-question").textContent = "Generate flashcards to study.";
            document.getElementById("flashcard-answer").textContent = "";
            document.getElementById("flashcard-answer").hidden = true;
            return;
        }

         if (currentFlashcardIndex >= flashcards.length) {
            currentFlashcardIndex = flashcards.length - 1;
        }

         displayFlashcard();

    });
}
// Quiz button.
const quizButton = document.getElementById("quiz-btn");

 if (quizButton) {
        quizButton.addEventListener("click", async () => {
            const materialInput = document.getElementById("question-material-id");
            const materialId = materialInput ? materialInput.value : "";
        if (!materialId) {
                alert("Please upload a PDF first.");
                return;
            }

            quizButton.disabled = true;
            quizButton.textContent = "Generating...";

             try {
            const response = await fetch("/studybuddy/api/quizzes", {
                method: "POST",
                credentials: "same-origin",
                body: new URLSearchParams({
                    materialId: materialId,
                    questionType: "multiple-choice"
                })
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || "Could not generate practice quiz.");
            }

             if (!result.taskId) {
                throw new Error("No quiz task ID was returned.");
            }
            
                   alert("Practice quiz generation started!");
        } catch (error) {
            alert(error.message);
        } finally {
            quizButton.disabled = false;
            quizButton.textContent = "Generate Practice Quiz";
        }
    });
}

// Quiz Display
let quizQuestion = [];
let currentQuestionIndex = 0;
let selectedQuizAnswer = null;

function displayQuizQuestion() {
    if (quizQuestions.length === 0) {
        return;
    }
    const question = quizQuestions[currentQuestionIndex];
    document.getElementById("quiz-count").textContent = "Question " + (currentQuestionIndex + 1) + " of " + quizQuestions.length;
    document.getElementById("quiz-question").textContent = question.question;
    const choicesContainer = document.getElementById("quiz-choices");
    choicesContainer.innerHTML = "";
    selectedQuizAnswer = null;
    const feedback = document.getElementById("quiz-feedback");
    feedback.hidden = true;
    feedback.textContent = "";

    question.choices.forEach((choice, index) => {
        const choiceButton = document.createElement("button");
        choiceButton.type = "button";
        choiceButton.className = "quiz-choice";
        choiceButton.textContent = choice;

        choiceButton.addEventListener("click", () => {

            selectedQuizAnswer = index;
            choicesContainer.querySelectorAll(".quiz-choice").forEach(button => {
                button.classList.remove("selected");
                });
                 choiceButton.classList.add("selected");
        });
         choicesContainer.appendChild(choiceButton);
    });
}

// Submit quiz answers.
const submitQuizButton = document.getElementById("submit-quiz-btn");

if (submitQuizButton) {
    submitQuizButton.addEventListener("click", () => {

        if (quizQuestions.length === 0) {
            alert("Please generate a practice quiz first.");
            return;
        }

         if (selectedQuizAnswer === null) {
            alert("Please select an answer first.");
            return;
        }

        const question = quizQuestions[currentQuestionIndex];
        const feedback = document.getElementById("quiz-feedback");

         if (selectedQuizAnswer === question.correctAnswer) {
            feedback.textContent = "Correct!";
        } else {
            feedback.textContent = "Incorrect. Try again!";
        }
         feedback.hidden = false;
    });
}

// Quiz navigation.
const previousQuestionButton = document.getElementById("previous-question-btn");
const nextQuestionButton = document.getElementById("next-question-btn");

if (previousQuestionButton) {
    previousQuestionButton.addEventListener("click", () => {

        if (quizQuestions.length === 0) {
            return;
        }

         if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            displayQuizQuestion();
        }
    });
}

if (nextQuestionButton) {
    nextQuestionButton.addEventListener("click", () => {

        if (quizQuestions.length === 0) {
            return;
        }
        
         if (currentQuestionIndex < quizQuestions.length - 1) {
            currentQuestionIndex++;
            displayQuizQuestion();
        }
    });
}

// Generate true/false quiz.
const trueFalseButton = document.getElementById("truefalse-btn");

if (trueFalseButton) {
    trueFalseButton.addEventListener("click", async () => {
        const materialInput = document.getElementById("question-material-id");
        const materialId = materialInput ? materialInput.value : "";

        if (!materialId) {
            alert("Please upload a PDF first.");
            return;
        }
        trueFalseButton.disabled = true;
        trueFalseButton.textContent = "Generating...";

        try {
            const response = await fetch("/studybuddy/api/quizzes", {
                method: "POST",
                credentials: "same-origin",
                body: new URLSearchParams({
                    materialId: materialId,
                    questionType: "true-false"
                })
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || "Could not generate True/False quiz.");
            }
            
            if (!result.taskId) {
                throw new Error("No quiz task ID was returned.");
            }

            alert("True/False quiz generation started!");

            } catch (error) {
            alert(error.message);

        } finally {
            trueFalseButton.disabled = false;
            trueFalseButton.textContent = "Generate True/False Quiz";
        }
    });
}

// Wait for generated quiz.
async function waitForGeneratedQuiz(taskId) {
    const maxAttempts = 60;

     for (let attempt = 0; attempt < maxAttempts; attempt++) {
        const response = await fetch("/studybuddy/api/tasks/" + encodeURIComponent(taskId), {
                method: "GET",
                credentials: "same-origin",
                cache: "no-store"
            }
        );

        const result = await response.json().catch(() => null);
    
        if (!response.ok) {
            throw new Error(result?.error || "Could not check quiz generation.");
        }
        if (result?.status === "COMPLETE") {
            if (!result.resultId) {
                throw new Error("No generated quiz was returned.");
            }

            const generated = JSON.parse(result.resultId);

            if (!Array.isArray(generated.questions)) {
                throw new Error("Invalid quiz response.");
            }

            return generated.questions;
        }

        if (result?.status === "FAILED") {
            throw new Error("Quiz generation failed.");
        }

        await new Promise(resolve => setTimeout(resolve, 1000));
    }
    throw new Error("Quiz generation is taking longer than expected.");
}



