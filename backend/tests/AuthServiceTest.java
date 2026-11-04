
package tests;

// The class we are testing.
import services.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// unit tests for auth service.
public class AuthServiceTest {

    // The authentication service being tested.
    private AuthService authService;


    // Tests
    @BeforeEach
    void setUp() {
        authService = new AuthService();
    }


    // Test empty name.
    @Test
    void signupRejectsEmptyName() {

        IllegalArgumentException exception =
            assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(
                    "",
                    "student@example.com",
                    "Password123",
                    "Password123"
                )
            );

        assertEquals(
            "Please enter your full name.",
            exception.getMessage()
        );
    }


    // Test the length of the name.
    @Test
    void signupRejectsLongName() {

        // Create a name containing 121 characters.
        String longName = "A".repeat(121);

        IllegalArgumentException exception =
            assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(
                    longName,
                    "student@example.com",
                    "Password123",
                    "Password123"
                )
            );

        assertEquals(
            "Name must be 120 characters or fewer.",
            exception.getMessage()
        );
    }


    // Tests for invalid emails.
    @Test
    void signupRejectsInvalidEmail() {

        IllegalArgumentException exception =
            assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(
                    "Test Student",
                    "not-an-email",
                    "Password123",
                    "Password123"
                )
            );

        assertEquals(
            "Please enter a valid email address.",
            exception.getMessage()
        );
    }


    // Tests for short passwords.
    @Test
    void signupRejectsShortPassword() {

        IllegalArgumentException exception =
            assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(
                    "Test Student",
                    "student@example.com",
                    "1234",
                    "1234"
                )
            );

        assertEquals(
            "Password must be at least 8 characters.",
            exception.getMessage()
        );
    }


    // Tests the length of the password.
    @Test
    void signupRejectsLongPassword() {

        // 73 ASCII characters occupy 73 UTF-8 bytes.
        String longPassword = "A".repeat(73);

        IllegalArgumentException exception =
            assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(
                    "Test Student",
                    "student@example.com",
                    longPassword,
                    longPassword
                )
            );

        assertEquals(
            "Password is too long.",
            exception.getMessage()
        );
    }


    // Tests for the confirmation.
    @Test
    void signupRejectsMismatchedPasswords() {

        IllegalArgumentException exception =
            assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(
                    "Test Student",
                    "student@example.com",
                    "Password123",
                    "Different123"
                )
            );

        assertEquals(
            "Passwords do not match.",
            exception.getMessage()
        );
    }


    // Tests for missing information.
    @Test
    void loginRejectsMissingCredentials() throws Exception {

        assertNull(
            authService.login("", "Password123")
        );

        assertNull(
            authService.login("student@example.com", "")
        );

        assertNull(
            authService.login(null, "Password123")
        );
    }


    // Tests for invalid user.
    @Test
    void findByIdRejectsInvalidId() throws Exception {

        assertNull(authService.findById(0));

        assertNull(authService.findById(-1));
    }
}
