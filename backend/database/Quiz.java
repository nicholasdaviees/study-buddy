
import java.util.List;

public final class Quiz {
    public final long quizID;
    public final long userID;
    public final String title;
    public final List<Question> questions;

    public Quiz(long quizID, long userID, String title, List<Question> questions) {
        this.quizID = quizID;
        this.userID = userID;
        this.title = title;
        this.questions = questions;
    }
}

