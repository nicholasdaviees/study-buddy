package database;

import java.util.List;

public final class Quiz {
    public final long quizID;
    public final long userID;
    public final long materialID;
    public final String title;
    public final List<Question> questions;

    public Quiz(long quizID, long userID, long materialID, String title, List<Question> questions) {

        this.quizID = quizID;
        this.userID = userID;
        this.materialID = materialID;
        this.title = title;
        this.questions = questions;
    }
}