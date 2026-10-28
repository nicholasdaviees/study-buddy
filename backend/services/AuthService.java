
package services;

// User is the student account.
import database.User;
// UserDao handles reading and writing user data
import database.UserDao;

import java.sql.SQLException;
import java.util.Locale;

public class AuthService {

    // This class will handle MySQL operations.
    private final UserDao userDao = new UserDao();


   
    // create a new student account
    public User signup(
            String name,
            String email,
            String password,
            String confirmPassword
    ) throws SQLException {

        // Check the student's name.
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter your full name."
            );
        }

        if (name.trim().length() > 120) {
            throw new IllegalArgumentException(
                    "Name must be 120 characters or fewer."
            );
        }

        // Check and normalize the email address.
        String cleanEmail = normalizeEmail(email);

        if (cleanEmail.length() > 255
                || !cleanEmail.matches(
                    "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
                )) {

            throw new IllegalArgumentException(
                    "Please enter a valid email address."
            );
        }

        // Check the password.
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters."
            );
        }

        // BCrypt in the chosen library uses only
        // the first 72 UTF-8 bytes of a password.
        if (password.getBytes(
                java.nio.charset.StandardCharsets.UTF_8
            ).length > 72) {

            throw new IllegalArgumentException(
                    "Password is too long."
            );
        }

        // Check that both passwords match.
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException(
                    "Passwords do not match."
            );
        }

        // Check whether the email already exists.
        User existingUser = userDao.findByEmail(cleanEmail);

        if (existingUser != null) {
            throw new IllegalArgumentException(
                    "An account with this email already exists."
            );
        }

        // Hash the password before saving it.
        String passwordHash = PasswordUtil.hash(password);

        // Save the new student using the DAO.
        return userDao.create(
                name.trim(),
                cleanEmail,
                passwordHash
        );
    }

    
    // Log in to an existing student account
    public User login(
            String email,
            String password
    ) throws SQLException {

        // Reject empty login information.
        if (email == null || email.isBlank()
                || password == null || password.isEmpty()) {
            return null;
        }

        String cleanEmail = normalizeEmail(email);

        // Look for the account in MySQL.
        User user = userDao.findByEmail(cleanEmail);

        if (user == null) {
            return null;
        }

        // Compare the entered password with
        // the saved BCrypt hash.
        boolean passwordMatches = PasswordUtil.verify(
                password,
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            return null;
        }

        // Correct email and password.
        return user;
    }

    // FInd the current student by user ID.
    public User findById(long userId) throws SQLException {

        if (userId <= 0) {
            return null;
        }

        return userDao.findById(userId);
    }

    // Way to organize emails and stored
    private String normalizeEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter your email address."
            );
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}


