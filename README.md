# study-buddy

Study Buddy is a web application designed to help students study their class material in a simpler and more interactive way. Instead of only reading through notes and slides, students can use their own class material to create study resources like flashcards and practice quizzes

## Features

Upload text-based PDFs up to a maximum size of 21 MB.
Generate flashcards / multiple choice or true-false quizzes from uploaded PDF.
The study buddy AI can answer almost any question the user has about their content, and all answers are based on the user's uploaded material.
Users can discard any uploaded material, and individually discard any flashcard / quiz question.

## Getting Started

To deploy study buddy on your own, please follow these instructions: 
[Contributing Guidelines](https://github.com/nicholasdaviees/study-buddy/tree/main/docs)

### Prerequisites

**Hardware Requirements:**

Processor: 64-bit CPU
Memory: 16GB RAM+ to run the local LLM model
Storage: 25GB extra space, mainly for the local LLM model
Network: 2-5Mb/s+ network speed

**Software Requirements:**

Operating System: 64-bit macOS, Windows, or Linux
Browser: Up-to-date Chrome, Firefox, Edge, or Safari
Java JDK 21: Compiles and runs servlets, services, and worker threads
Apache Tomcat: Serves the frontend and runs the servlets
MySQL: Stores user login info, flashcards/quizzes, and uploaded material
JDBC: Used to connect the Java services to the MySQL database
Apache PDFBox: Validates PDF and extracts readable text
Ollama: Runs the local LLM model and exposes its HTTP interface

### Bugs / Limitations

Max limit of PDF: 21 MB
AI generation speeds may depend on computer's ram and cpu.
Local model can sometimes hallucinate on the answers it gives. However, hallucinations can be reduced by switching to a larger model, depending on your computer power. 
