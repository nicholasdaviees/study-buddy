import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

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
       long userID = 10;
        UserDAO userDAO = new UserDAO();

        if (userDAO.getUserByID(userID) == null) {
            System.out.println("User ID 10 does not exist.");
            return;
        }
        Path pdfPath = Paths.get(
            "C:/StuddyBuddy/Project Proposal(PL)-1.pdf"
        );

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
        System.out.println(
            "Their document IDs: "
            + documentsDAO.getDocumentIDsByUserID(userID)
        );
    }
}

    
