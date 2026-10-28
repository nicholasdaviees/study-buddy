
package services;

import org.mindrot.jbcrypt.BCrypt;

//Handles password security
public final class PasswordUtil {

    private static final int WORK_FACTOR = 12;

    //prevent object creation
    private PasswordUtil() {
        // Prevent instantiation.
    }

    // Hash a password during signup.
    public static String hash(String password) {

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(
                    "Password cannot be empty."
            );
        }

        return BCrypt.hashpw(
                password,
                BCrypt.gensalt(WORK_FACTOR)
        );
    }

    // Verify password
    public static boolean verify(
            String password,
            String storedHash
    ) {

        if (password == null || storedHash == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(password, storedHash);

        } catch (IllegalArgumentException exception) {
            // Invalid or corrupted stored hash.
            return false;
        }
    }
}

