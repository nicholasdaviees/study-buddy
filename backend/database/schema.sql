CREATE DATABASE IF NOT EXISTS StudyBuddy CHARACTER SET utf8mb4;

USE StudyBuddy;

CREATE TABLE IF NOT EXISTS users (
    userID BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL
    
);
CREATE TABLE  IF NOT EXISTS documents (
    docID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    userID BIGINT NOT NULL,
    fileName VARCHAR(255) NOT NULL,
    pdfData LONGBLOB NOT NULL,
    fileSizeBytes BIGINT UNSIGNED NOT NULL,
    

    CONSTRAINT fk_documents_user
        FOREIGN KEY (userID)
        REFERENCES users(userID)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS quizzes (
    quizID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    userID BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,

    CONSTRAINT fk_quizzes_user
        FOREIGN KEY (userID) REFERENCES users(userID)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS quiz_questions (
    questionID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    quizID BIGINT NOT NULL,
    questionText TEXT NOT NULL,
    questionType ENUM('MULTIPLE_CHOICE', 'TRUE_FALSE') NOT NULL,
    questionOrder INT UNSIGNED NOT NULL,

    CONSTRAINT uq_quiz_question_order
        UNIQUE (quizID, questionOrder),

    CONSTRAINT chk_quiz_question_order
        CHECK (questionOrder >= 1),

    CONSTRAINT fk_quiz_questions_quiz
        FOREIGN KEY (quizID) REFERENCES quizzes(quizID)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS question_options (
    optionID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    questionID BIGINT NOT NULL,
    optionText TEXT NOT NULL,
    optionOrder INT UNSIGNED NOT NULL,
    isCorrect BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uq_question_option_order
        UNIQUE (questionID, optionOrder),

    CONSTRAINT chk_question_option_order
        CHECK (optionOrder >= 1),

    CONSTRAINT chk_question_option_correct
        CHECK (isCorrect IN (0, 1)),

    CONSTRAINT fk_question_options_question
        FOREIGN KEY (questionID) REFERENCES quiz_questions(questionID)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
