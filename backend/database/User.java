
package database;

// Represents a student account.
public class User {

    private final long id;
    private final String name;
    private final String email;
    private final String passwordHash;

    // Constructor: creates a User object.
    public User(long id, String name,
                String email, String passwordHash) {

        // Save the provide information
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Returns the student's unique ID.
    public long getId() {
        return id;
    }

    // Returns the student's full name.
    public String getName() {
        return name;
    }

    // Returns the student's email.
    public String getEmail() {
        return email;
    }

    // Used internally to verify login passwords.
    public String getPasswordHash() {
        return passwordHash;
    }
}

