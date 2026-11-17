
// Variables for the home page
const pdfUpload = document.getElementById("pdf-upload");
const fileStatus = document.getElementById("file-status");

const flashcardBtn = document.getElementById("flashcard-btn");
const quizBtn = document.getElementById("quiz-btn");
const truefalseBtn = document.getElementById("truefalse-btn");
const askBtn = document.getElementById("ask-btn");

let selectedFile = null;
let materialId = null;


// Disable buttons until a PDF has been processed
flashcardBtn.disabled = true;
quizBtn.disabled = true;
truefalseBtn.disabled = true;
askBtn.disabled = true;


// Event listeners

pdfUpload.addEventListener("change", function() {
    selectedFile = pdfUpload.files[0];
    materialId = null;

    flashcardBtn.disabled = true;
    quizBtn.disabled = true;
    truefalseBtn.disabled = true;
    askBtn.disabled = true;

    if (!selectedFile) {
        fileStatus.textContent = "No file selected.";
    }
    else if (!selectedFile.name.toLowerCase().endsWith(".pdf")) {
        showError("Please select a PDF file.");
        pdfUpload.value = "";
        selectedFile = null;
    }
    else if (selectedFile.size === 0) {
        showError("The selected file is empty.");
        pdfUpload.value = "";
        selectedFile = null;
    }
    else {
        fileStatus.style.color = "";
        fileStatus.textContent = "Selected: " + selectedFile.name;

        uploadPdf(selectedFile);
    }
});


flashcardBtn.addEventListener("click", function() {
    generateFlashcards(materialId);
});


quizBtn.addEventListener("click", function() {
    generateQuiz(materialId, "multiple-choice");
});


truefalseBtn.addEventListener("click", function() {
    generateQuiz(materialId, "true-false");
});


askBtn.addEventListener("click", function() {
    let question = prompt("What would you like to ask Study Buddy?");

    if (question && question.trim() !== "") {
        askQuestion(materialId, question.trim());
    }
});


// Functions for communicating with the backend

// Sends a selected PDF to the upload servlet.
function uploadPdf(file) {
    // Need to connect to the upload servlet.
    fileStatus.textContent = "Ready to upload: " + file.name;
}


// Calls the quiz servlet for multiple-choice or true/false questions.
function generateQuiz(materialId, questionType) {
    // Need servlet URL and question type.
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
    fileStatus.textContent = errorMessage;
    fileStatus.style.color = "darkred";
}


// Updates the interface for queued, processing, complete, or failed work.
function displayTaskStatus(taskStatus) {
    fileStatus.style.color = "";

    if (taskStatus === "queued") {
        fileStatus.textContent = "Waiting to process...";
    }
    else if (taskStatus === "processing") {
        fileStatus.textContent = "Processing your material...";
    }
    else if (taskStatus === "complete") {
        fileStatus.textContent = "Processing complete!";
        fileStatus.style.color = "darkgreen";
    }
    else if (taskStatus === "failed") {
        showError("Unable to process your material.");
    }
}
