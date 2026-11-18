public final class User {
        public final long userID;
        public final String name;
        public final String email;
        public final String password;

        public User(long userID, String name, String email, String password) {
            this.userID = userID;
            this.name = name;
            this.email = email;
            this.password = password;
        }
    }
