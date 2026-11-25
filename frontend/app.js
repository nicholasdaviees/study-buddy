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

// Displays the answer returned for a student question.
function displayAnswer(answerJson) {

    const answerElement = document.getElementById("question-answer");

    if(!answerElement) {
        return;
    }

    try {

        // Convert Ollama's JSON string.
        const result = typeof answerJson.answer === "string" ? JSON.parse(answerJson.answer): answerJson.answer;
        
        // Display answer text.
        if(result.found && result.answer) {
            answerElement.textContent = result.answer;
        } else {
            answerElement.textContent = "I could not find an answer";
        }
    } catch (error) {
        answerElement.textContent = answerJson.answer || "No answer available.";
    }
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

       if (result?.status === "PROCESSING") {
        displayTaskStatus("Study Buddy is answering your question...");
    } else {
        throw new Error("Unexpected question task status.");
    }

    showError("Study Buddy is answering your question...");

    // Pause.
    await new Promise(resolve => setTimeout(resolve, intervalMilliseconds));
    }

    throw new Error("The answer is taking longer than expected. Please try again later.");

}

// Calls the quiz servlet for saved quiz questions.
async function loadQuizzes(materialId) {
     if (!materialId) {
        throw new Error("Please select a PDF first.");
    }
    const response = await fetch(
        "/studybuddy/api/quizzes?materialId=" + encodeURIComponent(materialId),
        {
            method: "GET",
            credentials: "same-origin",
            cache: "no-store"
        }
    );
    const result = await response.json().catch(() => null);

     if (!response.ok) {
        throw new Error(result?.error || "Could not load saved quizzes.");
    }

    if (!result || !Array.isArray(result.questions)) {
        throw new Error("Invalid saved quiz response.");
    }
    
    return result.questions;
}
// Show saved quizzes.
async function showSavedQuizzes(materialId) {
    const savedQuestions = await loadQuizzes(materialId);
    if (savedQuestions.length === 0) {
        alert("No saved quizzes were found.");
        return;
    }
    quizQuestions = savedQuestions;
    quizAnswers = new Array(quizQuestions.length).fill(null);
    currentQuestionIndex = 0;
    const scoreElement = document.getElementById("quiz-score");

    if (scoreElement) {
        scoreElement.textContent = "Score: 0 out of " + quizQuestions.length + " (0 answered).";
    }

    displayQuizQuestion();

    document.getElementById("quiz-section").scrollIntoView({
        behavior: "smooth"
    });
}
// Calls the flashcard servlet for saved flashcards.
async function loadFlashcards(materialId) {
 if (!materialId) {
        throw new Error("Please select a PDF first.");
    }
     const response = await fetch("/studybuddy/api/flashcards?materialId=" + encodeURIComponent(materialId),
        {
            method: "GET",
            credentials: "same-origin",
            cache: "no-store"
        }
    );
     const result = await response.json().catch(() => null);

     if (!response.ok) {
        throw new Error(result?.error || "Could not load saved flashcards.");
    }

     if (!result || !Array.isArray(result.flashcards)) {
        throw new Error("Invalid saved flashcard response.");
    }
     return result.flashcards;
}

// Show saved flashcard.
async function showSavedFlashcards(materialId) {
    const savedCards = await loadFlashcards(materialId);

    if (savedCards.length === 0) {
        alert("No saved flashcards were found.");
        return;
    }

    flashcards = savedCards;
    currentFlashcardIndex = 0;
    displayFlashcard();
    document.getElementById("flashcard-section").scrollIntoView({
        behavior: "smooth"
    });
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
        statusElement.textContent = taskStatus;
 }
}

const askButton = document.getElementById("ask-btn");
    if (askButton) {
        askButton.addEventListener("click", async () => {
            const materialInput = document.getElementById("question-material-id");
            const questionInput = document.getElementById("student-question");

            const materialId = materialInput.value;
            const question = questionInput.value;

            askButton.disabled = true;
            
            document.getElementById("question-answer").textContent = "";

            try {
                await askQuestion(materialId, question);
            } catch (error) {
                showError(error.message);
            } finally {
                askButton.disabled = false;
            }
        });
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

              uploadedMaterials.push({
    id: materialId,
    name: file.name,
    size: file.size
});
saveMaterialsToBrowser();
displaySavedMaterials();

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
                credentials: "same-origin",
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
    throw new Error("Flashcard generation is taking longer than expected.");
}   
// Save flashcard button.
const saveFlashcardButton = document.getElementById("save-flashcard-btn");

if (saveFlashcardButton) {
    saveFlashcardButton.addEventListener("click", async () => {
        
        if (flashcards.length === 0) {
            alert("Please generate flashcards first.");
            return;
        }

         const currentCard = flashcards[currentFlashcardIndex];
        
        saveFlashcardButton.disabled = true;
        saveFlashcardButton.textContent = "Saving...";

        try {
            await saveFlashcard(currentCard);
            alert("Flashcard saved successfully!");
        } catch (error) {
            alert(error.message);
        } finally {
            saveFlashcardButton.disabled = false;
            saveFlashcardButton.textContent = "Save Flashcard";
        }
    });
}


// Save the flashcard
async function saveFlashcard(card) {
    const response = await fetch("/studybuddy/api/flashcards/save", {
        method: "POST",
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            question: card.question,
            answer: card.answer
        })
    });

    const result = await response.json().catch(() => null);

    if (!response.ok) {
        throw new Error(result?.error || "Could not save flashcard.");
    }
    return result;
}
// Delete Flashcard button.
const deleteFlashcardButton = document.getElementById("delete-flashcard-btn");

if (deleteFlashcardButton) {
    deleteFlashcardButton.addEventListener("click", async () => {
        if (flashcards.length === 0) {
            alert("There are no flashcards to delete.");
            return;
        }
        const currentCard = flashcards[currentFlashcardIndex];
        const confirmed = confirm("Are you sure you want to delete this flashcard?");
          if (!confirmed) {
            return;
        }

        deleteFlashcardButton.disabled = true;
        deleteFlashcardButton.textContent = "Deleting...";

        try {

            if (currentCard.id != null) {
                await deleteSavedFlashcard(currentCard.id);
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
        } catch (error) {
            alert(error.message);
        } finally {
            deleteFlashcardButton.disabled = false;
            deleteFlashcardButton.textContent = "Delete Flashcard";
        }
    });
}

// Delete save flashcard.
async function deleteSavedFlashcard(cardId) {
    if (cardId == null) {
        throw new Error("This flashcard has not been saved yet.");
    }
    const response = await fetch(
        "/studybuddy/api/flashcards/" + encodeURIComponent(cardId),
        {
            method: "DELETE",
            credentials: "same-origin"
        }
    );
    if (!response.ok) {
        const result = await response.json().catch(() => null);

        throw new Error(result?.error || "Could not delete flashcard.");
    }
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
            
    quizQuestions = await waitForGeneratedQuiz(result.taskId);
    currentQuestionIndex = 0;
    quizAnswers = new Array(quizQuestions.length).fill(null);
    displayQuizQuestion();
    document.getElementById("quiz-section").scrollIntoView({
    behavior: "smooth"
});
        } catch (error) {
            alert(error.message);
        } finally {
            quizButton.disabled = false;
            quizButton.textContent = "Generate Practice Quiz";
        }
    });
}

// Quiz Display
let quizQuestions = [];
let currentQuestionIndex = 0;
let selectedQuizAnswer = null;

// Quiz score
let quizAnswers = [];

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

                quizAnswers[currentQuestionIndex] = selectedQuizAnswer;

        if (selectedQuizAnswer === question.correctAnswer) {
    feedback.textContent = "Correct!";
} else {
    feedback.textContent = "Incorrect. Try again!";
}

let correctCount = 0;

for (let i = 0; i < quizQuestions.length; i++) {
    if (quizAnswers[i] !== null &&
        quizAnswers[i] === quizQuestions[i].correctAnswer) {
        correctCount++;
    }
}
const answeredCount = quizAnswers.filter(
    answer => answer !== null
).length;

const scoreElement = document.getElementById("quiz-score");

if (scoreElement) {
     if (answeredCount === quizQuestions.length) {
        const percentage = Math.round((correctCount / quizQuestions.length) * 100);
 scoreElement.textContent = "Quiz Complete! Final Score: " + correctCount + " out of " + quizQuestions.length + " (" + percentage + "%)";
    } else {
         scoreElement.textContent = "Score: " + correctCount + " out of " + quizQuestions.length + " (" + answeredCount + " answered).";
    }
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

            quizQuestions = await waitForGeneratedQuiz(result.taskId);
            quizAnswers = new Array(quizQuestions.length).fill(null);
            currentQuestionIndex = 0;
            displayQuizQuestion();
            document.getElementById("quiz-section").scrollIntoView({
    behavior: "smooth"
});


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

// Display uploaded study material.
// Rememver saved material
let uploadedMaterials = JSON.parse(
    localStorage.getItem("studyBuddyMaterials") || "[]"
);
function saveMaterialsToBrowser() {
    localStorage.setItem("studyBuddyMaterials", JSON.stringify(uploadedMaterials));
}

// Delete saved material from backend.
async function deleteSavedMaterial(materialId) {
     if (!materialId) {
        throw new Error("No material was selected for deletion.");
    }

    const response = await fetch("/studybuddy/api/material/" + encodeURIComponent(materialId),
        {
            method: "DELETE",
            credentials: "same-origin"
        }
    );
    if (!response.ok) {
        const result = await response.json().catch(() => null);

        throw new Error(result?.error || "Could not delete the material from the server.");
    }
}

displaySavedMaterials();
function displaySavedMaterials() {
    const materialsList = document.getElementById("saved-materials-list");

    if (!materialsList) {
        return;
    }
     materialsList.innerHTML = "";
     if (uploadedMaterials.length === 0) {
        materialsList.textContent = "No study materials uploaded yet.";
        return;
    }

    uploadedMaterials.forEach(material => {

        const materialDiv = document.createElement("div");
        materialDiv.className = "saved-material";
        const fileName = document.createElement("h3");
        fileName.textContent = material.name;
        const fileSize = document.createElement("p");
        fileSize.textContent = "File Size: " + (material.size / (1024 * 1024)).toFixed(2) + " MB";
        const openButton = document.createElement("button");
        openButton.type = "button";
        openButton.className = "open-material-btn";
        openButton.textContent = "Open Material";
         openButton.addEventListener("click", () => {
            const materialInput = document.getElementById("question-material-id");
            const selectedMaterialName = document.getElementById("selected-material-name");
            if (materialInput) {
        materialInput.value = material.id;
    }
            if (selectedMaterialName) {
        selectedMaterialName.textContent = "Selected study material: " + material.name;
    }
            alert(material.name + " is now selected for studying.");
        });
        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.className = "delete-material-btn";
        deleteButton.textContent = "Delete Material";
        deleteButton.addEventListener("click", async () => {
        const confirmed = confirm("Are you sure you want to remove " + material.name + "?");
            if (!confirmed) {
        return;
    }
        deleteButton.disabled = true;
        deleteButton.textContent = "Deleting...";

        try {
            await deleteSavedMaterial(material.id);
            uploadedMaterials = uploadedMaterials.filter(item => item.id !== material.id);
            saveMaterialsToBrowser();
            const materialInput = document.getElementById("question-material-id");
            const selectedMaterialName = document.getElementById("selected-material-name");

            if (materialInput && materialInput.value === material.id) {
            materialInput.value = "";

            if (selectedMaterialName) {
                selectedMaterialName.textContent = "No study material selected.";
            }
        }
            displaySavedMaterials();
            alert("Material deleted successfully!");

        }catch (error) {
            alert(error.message);
        } finally {
        deleteButton.disabled = false;
        deleteButton.textContent = "Delete Material";
    }
});
        materialDiv.appendChild(fileName);
        materialDiv.appendChild(fileSize);
        materialDiv.appendChild(openButton);
        materialDiv.appendChild(deleteButton);
        materialsList.appendChild(materialDiv);
           });
}
// Load saved flashcard button
const loadFlashcardsButton = document.getElementById("load-flashcards-btn");
if (loadFlashcardsButton) {
    loadFlashcardsButton.addEventListener("click", async () => {
        const materialInput = document.getElementById("question-material-id");
        const materialId = materialInput ? materialInput.value : "";

        if (!materialId) {
            alert("Please select a PDF first.");
            return;
        }

        loadFlashcardsButton.disabled = true;
        loadFlashcardsButton.textContent = "Loading...";

        try {
            await showSavedFlashcards(materialId);
        } catch (error) {
            alert(error.message);
        } finally {
             loadFlashcardsButton.disabled = false;
            loadFlashcardsButton.textContent = "Load Saved Flashcards";
        }
    });
}

// Load the saved quiz button.
 const loadQuizzesButton = document.getElementById("load-quizzes-btn");

 if (loadQuizzesButton) {
    loadQuizzesButton.addEventListener("click", async () => {
        const materialInput = document.getElementById("question-material-id");
        const materialId = materialInput ? materialInput.value : "";

        if (!materialId) {
             alert("Please select a PDF first.");
             return;
         }

        loadQuizzesButton.disabled = true;
        loadQuizzesButton.textContent = "Loading...";

        try {
            await showSavedQuizzes(materialId);
        } catch (error) {
            alert(error.message);
        } finally {
            loadQuizzesButton.disabled = false;
            loadQuizzesButton.textContent = "Load Saved Quizzes";
        }
    });
}


