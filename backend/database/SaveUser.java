import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

//this is now just the testing file, SaveUser is old name
public class SaveUser {

    public static void main(String[] args) throws SQLException, IOException {
        //Commenting out userDAO method testing for now
        /* 
        UserDAO userDAO = new UserDAO();
        long testUserID = 8;

        
       
            userDAO.saveUser("test3", "test@test.com", "pass");
            System.out.println("User saved successfully.");
        

       
        User user = userDAO.getUserByID(testUserID);

        if (user == null) {
            System.out.println("No user found with that ID.");
        } else {
            System.out.println("ID: " + user.userID);
            System.out.println("Name: " + user.name);
            System.out.println("Email: " + user.email);
            System.out.println("Password: " + user.password);
        }

        
        boolean deleted = userDAO.deleteUserByID(1001);

        System.out.println(deleted
            ? "User ID 1001 deleted successfully."
            : "No user found with ID 1001.");
        */
       //commenting out doc saving tests
       /* 
       long userID = 10;
        UserDAO userDAO = new UserDAO();

        if (userDAO.getUserByID(userID) == null) {
            System.out.println("User ID 10 does not exist.");
            return;
        }
        Path pdfPath = Paths.get("C:/StuddyBuddy/Project Proposal(PL)-1.pdf");

        if (Files.size(pdfPath) > DocumentsDAO.MAX_PDF_BYTES) {
            throw new IOException("The PDF exceeds the upload size limit.");
        }

        byte[] pdfData = Files.readAllBytes(pdfPath);

        DocumentsDAO documentsDAO = new DocumentsDAO();
        documentsDAO.saveDocument(
            userID,
            pdfPath.getFileName().toString(),
            pdfData
        );

        System.out.println("PDF saved successfully for user ID 10.");
        System.out.println("Their document IDs: "+ documentsDAO.getDocumentIDsByUserID(userID));
        */

        //comenting out retrieval tests for now
        /* 

        long docID = 1; 
        long userID = 10;

        DocumentsDAO documentsDAO = new DocumentsDAO();
        Documents document = documentsDAO.getDocumentByID(docID, userID);

        if (document == null) {
            System.out.println("No matching document found for this user.");
            return;
        }

        System.out.println("Document ID: " + document.docID);
        System.out.println("File name: " + document.fileName);
        System.out.println("File size: " + document.fileSizeBytes + " bytes");
        //System.out.println(java.util.Arrays.toString(document.pdfData));

        
        Path outputPath = Paths.get(
            "C:/StuddyBuddy/Retrieved Proposal.pdf"
        );

        Files.write(outputPath, document.pdfData);
        System.out.println("Retrieved PDF saved to: " + outputPath);
    
        */
       //commenting out document tests
       /* 
       DocumentsDAO documentsDAO = new DocumentsDAO();
        long userID = 10;

        System.out.println("Document IDs for user " + userID + ": " + documentsDAO.getDocumentIDsByUserID(userID));

        long docIDToDelete = 1;

        boolean documentDeleted =
            documentsDAO.deleteDocumentByID(docIDToDelete, userID);

        System.out.println(documentDeleted
            ? "Document deleted successfully."
            : "No matching document found for this user.");

        System.out.println("Remaining document IDs: " + documentsDAO.getDocumentIDsByUserID(userID));
        */

        //commenting out saving quiz test

        /*
        QuizDAO quizDAO = new QuizDAO();

         
        List<AnswerOption> multipleChoiceOptions = Arrays.asList(
            new AnswerOption(0L, 0L, "3", 1, false),
            new AnswerOption(0L, 0L, "4", 2, true),
            new AnswerOption(0L, 0L, "5", 3, false)
        );

        Question multipleChoiceQuestion = new Question(
            0L, 0L,
            "What is 2 + 2?",
            "MULTIPLE_CHOICE",
            1,
            multipleChoiceOptions
        );


        List<AnswerOption> trueFalseOptions = Arrays.asList(
            new AnswerOption(0L, 0L, "True", 1, false),
            new AnswerOption(0L, 0L, "False", 2, true)
        );

        Question trueFalseQuestion = new Question(
            0L, 0L,
            "2 + 2 = 5.",
            "TRUE_FALSE",
            2,
            trueFalseOptions
        );


        List<Question> quizQuestions = Arrays.asList(
            multipleChoiceQuestion,
            trueFalseQuestion
        );

        quizDAO.saveQuiz(10L, "Math practice quiz", quizQuestions);

        System.out.println("Quiz saved successfully.");
        System.out.println("Quiz IDs for user 10: " + quizDAO.getQuizIDsByUserID(10)
        );
        */

        QuizDAO quizDAO = new QuizDAO();

long quizIDToLoad = 1;
long quizOwnerID = 10;

//commenting out quiz retrieval test
/* 

Quiz loadedQuiz = quizDAO.getSavedQuiz(quizIDToLoad,quizOwnerID);

if (loadedQuiz == null) {
    System.out.println("No matching quiz found for this user.");
} else {
    System.out.println("Quiz ID: " + loadedQuiz.quizID);
    System.out.println("Title: " + loadedQuiz.title);

    for (Question loadedQuestion : loadedQuiz.questions) {
        System.out.println(
            "\nQuestion " + loadedQuestion.questionOrder
            + ": " + loadedQuestion.questionText
        );
        System.out.println("Type: " + loadedQuestion.questionType);

        for (AnswerOption loadedOption : loadedQuestion.options) {
            System.out.println(
                loadedOption.optionOrder + ". "
                + loadedOption.optionText
                + (loadedOption.isCorrect ? " (correct)" : "")
            );
        }
    }
}


        */

        //commenting out delete quiz tests
        /* 
        boolean quizDeleted = quizDAO.deleteQuiz(quizIDToLoad,quizOwnerID);
        System.out.println(quizDeleted ? "Quiz deleted successfully.": "No matching quiz found for this user.");
        System.out.println("Remaining quiz IDs: " + quizDAO.getQuizIDsByUserID(quizOwnerID));

        */
        //commenting out save flashcard test
        /* 
        FlashcardsDAO flashcardsDAO = new FlashcardsDAO();
        long flashcardTestUserID = 10L;

        flashcardsDAO.saveFlashcard(
            flashcardTestUserID,
            "What is 2 + 2?",
            "4"
        );

        System.out.println("Flashcard saved successfully.");

        System.out.println(
            "Their flashcard IDs: "
            + flashcardsDAO.getFlashcardIDsByUserID(flashcardTestUserID)
        );
        */
        //commenting out flashcard retrieval test
         
        FlashcardsDAO flashcardsDAO = new FlashcardsDAO();

        long cardIDToLoad = 1;
        long flashcardOwnerID = 10;
        /*
        Flashcards flashcard = flashcardsDAO.getFlashcardByID(
            cardIDToLoad,
            flashcardOwnerID
        );

        if (flashcard == null) {
            System.out.println("No matching flashcard found for this user.");
        } else {
            System.out.println("Card ID: " + flashcard.cardID);
            System.out.println("User ID: " + flashcard.userID);
            System.out.println("Front: " + flashcard.frontText);
            System.out.println("Back: " + flashcard.backText);
        }
        */

        boolean flashcardDeleted = flashcardsDAO.deleteFlashcardByID(cardIDToLoad,flashcardOwnerID);

        System.out.println(flashcardDeleted
            ? "Flashcard deleted successfully."
            : "No matching flashcard found for this user.");

        System.out.println("Remaining flashcard IDs: " + flashcardsDAO.getFlashcardIDsByUserID(flashcardOwnerID));

        }
        
      
    
}
    


    
