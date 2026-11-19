package database;

public final class Flashcards {
    public final long cardID;
    public final long userID;
    public final String frontText;
    public final String backText;

    public Flashcards(
            long cardID,
            long userID,
            String frontText,
            String backText) {

        this.cardID = cardID;
        this.userID = userID;
        this.frontText = frontText;
        this.backText = backText;
    }
}
