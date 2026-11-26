package database;

public final class Flashcards {
    public final long cardID;
    public final long userID;
    public final long materialID;
    public final String frontText;
    public final String backText;

    public Flashcards(long cardID, long userID, long materialID, String frontText, String backText) {

        this.cardID = cardID;
        this.userID = userID;
        this.materialID = materialID;
        this.frontText = frontText;
        this.backText = backText;
    }
}
