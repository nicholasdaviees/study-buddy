import java.sql.SQLException;
//this is now just the testing file, SaveUser is old name
public class SaveUser {

    public static void main(String[] args) throws SQLException {
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
    }
}
